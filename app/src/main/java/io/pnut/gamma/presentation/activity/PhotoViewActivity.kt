package io.pnut.gamma.presentation.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.content.IntentCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.pnut.gamma.domain.model.ThumbAndFull
import io.pnut.gamma.presentation.fragment.PhotoViewItemFragment
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ActivityPhotoViewBinding


class PhotoViewActivity : BaseActivity() {
    private enum class IntentKey { Photos, Index, SharedElementId, Radius, TransitionName }

    companion object {

        fun newIntent(context: Context, url: String, sharedElementId: Int): Intent {
            val items = listOf(ThumbAndFull(url, url))

            return Intent(context, PhotoViewActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.Photos.name, ArrayList(items))
                putExtra(IntentKey.SharedElementId.name, sharedElementId)
            }
        }

        fun startActivity(
            activity: Activity?,
            item: String,
            imageView: ImageView,
            radius: Float = 0f,
            transitionName: String = ""
        ) = startActivity(
            activity,
            ThumbAndFull(item, item),
            imageView,
            radius = radius,
            transitionName = transitionName
        )

        fun startActivity(
            activity: Activity?,
            item: ThumbAndFull,
            imageView: ImageView,
            transitionName: String = "",
            radius: Float = 0f
        ) {
            val intent = photoViewInstance(
                activity,
                listOf(item),
                radius = radius,
                transitionName = transitionName
            )
            val options = ActivityOptions.makeSceneTransitionAnimation(
                activity,
                Pair.create(imageView, transitionName)
            )
            activity?.startActivity(intent, options.toBundle())
        }

        fun photoViewInstance(
            context: Context?,
            items: List<ThumbAndFull>,
            index: Int = 0,
            radius: Float = 0f,
            transitionName: String = ""
        ) =
            Intent(context, PhotoViewActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.Photos.name, ArrayList(items))
                putExtra(IntentKey.Index.name, index)
                putExtra(IntentKey.Radius.name, radius)
                putExtra(IntentKey.TransitionName.name, transitionName)
            }

    }

    private val photos by lazy {
        IntentCompat.getParcelableArrayListExtra(intent, IntentKey.Photos.name, ThumbAndFull::class.java)
    }

    private val index by lazy {
        intent.getIntExtra(IntentKey.Index.name, 0)
    }
    private val adapter by lazy {
        MediaViewPager(this, photos.orEmpty(), index)
    }

    private lateinit var binding: ActivityPhotoViewBinding

    private fun fixTopPadding() {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        val statusBarHeight = rect.top
        binding.toolbar.layoutParams = FrameLayout.LayoutParams(binding.toolbar.layoutParams).also {
            it.leftMargin = binding.toolbar.marginLeft
            it.bottomMargin = binding.toolbar.marginBottom
            it.rightMargin = binding.toolbar.marginRight
            it.topMargin = statusBarHeight
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        fixTopPadding()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupAnimation()
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportPostponeEnterTransition()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.mediaViewPager.adapter = adapter
        binding.mediaViewPager.setCurrentItem(index, false)
        TabLayoutMediator(binding.mediaviewPagerIndicator, binding.mediaViewPager) { _, _ -> }.attach()

        binding.haulerView.setOnDragDismissedListener {
            finishAfterTransition()
        }
    }


    private fun setupAnimation() {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                super.onMapSharedElements(names, sharedElements)
                val view = adapter.getItem(binding.mediaViewPager.currentItem).requireView()
                    .findViewById<View>(R.id.photoView)
                sharedElements[names[0]] = view
            }
        })
    }

    class MediaViewPager(activity: PhotoViewActivity, items: List<ThumbAndFull>, index: Int = 0) :
        FragmentStateAdapter(activity) {
        private val fragments =
            items.mapIndexed { i, it -> PhotoViewItemFragment.newInstance(it, i == index) }

        override fun createFragment(position: Int): Fragment = fragments[position]
        override fun getItemCount(): Int = fragments.size

        fun getItem(position: Int): Fragment = fragments[position]
    }
}
