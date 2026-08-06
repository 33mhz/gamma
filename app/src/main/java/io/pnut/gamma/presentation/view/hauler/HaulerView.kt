package io.pnut.gamma.presentation.view.hauler

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.animation.PathInterpolatorCompat
import io.pnut.gamma.R
import kotlin.math.abs
import kotlin.math.log10

enum class DragDirection {
    UP, DOWN
}

class HaulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    // configurable
    private var dragDismissDistance =
        context.resources.getDimensionPixelSize(R.dimen.default_drag_dismiss_distance).toFloat()
    private var dragDismissFraction = -1f
    private var dragDismissScale = 0.95f
    private var shouldScale = true
    private var dragElasticity = 0.8f

    // state
    private var totalDrag: Float = 0.toFloat()
    private var draggingDown = false
    private var draggingUp = false
    private var mLastActionEvent: Int = 0

    private var onDragDismissed: ((dragDirection: DragDirection) -> Unit) = { }

    private var isDragEnabled = true
    private var dragUpEnabled = false

    init {
        getContext().withStyledAttributes(set = attrs, attrs = R.styleable.HaulerView) {
            val distanceAvailable = hasValue(R.styleable.HaulerView_dragDismissDistance)
            val dismissFractionAvailable = hasValue(R.styleable.HaulerView_dragDismissFraction)

            if (distanceAvailable && dismissFractionAvailable) {
                throw IllegalStateException("Do not specify both dragDismissDistance and dragDismissFraction. Choose one.")
            } else if (distanceAvailable) {
                dragDismissDistance = getDimensionPixelSize(R.styleable.HaulerView_dragDismissDistance, 0).toFloat()
            } else if (dismissFractionAvailable) {
                dragDismissFraction = getFloat(R.styleable.HaulerView_dragDismissFraction, dragDismissFraction)
            }

            dragDismissScale = getFloat(R.styleable.HaulerView_dragDismissScale, dragDismissScale)
            dragUpEnabled = getBoolean(R.styleable.HaulerView_dragUpEnabled, dragUpEnabled)
            dragElasticity = getFloat(R.styleable.HaulerView_dragElasticity, dragElasticity)
        }

        shouldScale = dragDismissScale != 1f
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean =
        (nestedScrollAxes and SCROLL_AXIS_VERTICAL) != 0

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (isDragEnabled.not()) {
            return super.onNestedPreScroll(target, dx, dy, consumed)
        }
        // if we're in a drag gesture and the user reverses up the we should take those events
        val draggingDownInProgress = draggingDown && dy > 0
        val draggingUpInProgress = draggingUp && dy < 0
        if (draggingDownInProgress || draggingUpInProgress) {
            dragScale(dy)
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (isDragEnabled.not()) {
            return super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        }
        dragScale(dyUnconsumed)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        mLastActionEvent = ev.action
        return super.onInterceptTouchEvent(ev)
    }

    override fun onStopNestedScroll(child: View) {
        if (isDragEnabled.not()) {
            return super.onStopNestedScroll(child)
        }

        val totalDragNormalized = if (dragUpEnabled) abs(totalDrag) else -totalDrag
        val dragDirection = if (totalDrag > 0) DragDirection.UP else DragDirection.DOWN

        if (totalDragNormalized >= dragDismissDistance) {
            dispatchDismissCallback(dragDirection)
        } else {
            if (mLastActionEvent == MotionEvent.ACTION_DOWN) {
                translationY = 0f
                scaleX = 1f
                scaleY = 1f
            } else {
                animate()
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .setInterpolator(PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f))
                    .setListener(null)
                    .start()
            }
            totalDrag = 0f
            draggingUp = false
            draggingDown = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (dragDismissFraction > 0f) {
            dragDismissDistance = h * dragDismissFraction
        }
    }

    fun setOnDragDismissedListener(onDragDismissedListener: (dragDirection: DragDirection) -> Unit) {
        onDragDismissed = onDragDismissedListener
    }

    private fun dragScale(scroll: Int) {
        if (scroll == 0) return

        totalDrag += scroll.toFloat()

        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true
            if (shouldScale) pivotY = height.toFloat()
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true
            if (shouldScale) {
                pivotY = 0f
            }
        }
        // (0–1 where 1 = dismiss distance)
        val dragFraction = log10((1 + abs(totalDrag) / dragDismissDistance).toDouble()).toFloat()
        var dragTo = dragFraction * dragDismissDistance * dragElasticity

        if (draggingUp) {
            dragTo *= -1f
        }
        translationY = dragTo

        if (shouldScale) {
            val scale = 1 - (1 - dragDismissScale) * dragFraction
            scaleX = scale
            scaleY = scale
        }

        val downSettlePointReached = draggingDown && totalDrag >= 0
        val upSettlePointReached = draggingUp && totalDrag <= 0
        if (downSettlePointReached || upSettlePointReached) {
            totalDrag = 0f
            draggingUp = false
            draggingDown = false
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
        }
    }

    private fun dispatchDismissCallback(dragDirection: DragDirection) {
        (context as? Activity)?.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
        onDragDismissed.invoke(dragDirection)
    }
}
