package io.pnut.gamma.util

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.text.format.DateFormat
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.pnut.gamma.R
import com.google.android.material.R as Rm
import com.google.android.material.snackbar.Snackbar
import io.pnut.gamma.domain.entity.PnutResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun Snackbar.showAsError() {
    val view = this.view
    val bgColor = ContextCompat.getColor(view.context, R.color.colorError)
    view.setBackgroundColor(bgColor)
    val textView = view.findViewById<TextView>(Rm.id.snackbar_text)
    textView.setTextColor(Color.WHITE)
    show()
}

fun Snackbar.oneLine(): Snackbar {
    val view = this.view
    val textView = view.findViewById<TextView>(Rm.id.snackbar_text)
    textView.maxLines = 1
    textView.ellipsize = TextUtils.TruncateAt.END
    return this
}

suspend fun <T> Call<PnutResponse<T>>.await(): PnutResponse<T> =
    suspendCancellableCoroutine { cont ->
        enqueue(object : Callback<PnutResponse<T>> {
            override fun onFailure(call: Call<PnutResponse<T>>, t: Throwable) {
                if (!cont.isCancelled) {
                    cont.resumeWithException(t)
                }
            }

            override fun onResponse(
                call: Call<PnutResponse<T>>,
                response: Response<PnutResponse<T>>
            ) {
                response.body()?.let { cont.resume(it) }
                    ?: response.errorBody()?.let {
                        cont.cancel(
                            ErrorCollections.CommunicationError.create(
                                it.string()
                            )
                        )
                    }

            }
        })

        cont.invokeOnCancellation {
            cancel()
        }
    }

fun Boolean.toInt(): Int = if (this) 1 else 0

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

fun Date.toFormatString(context: Context?): String {
    val dateFormatTemplate = context?.getString(R.string.file_date_format_template)
    return DateFormat.format(dateFormatTemplate, this).toString()
}

fun <T> Response<T>.bodyOrThrow(): T {
    val res = this
    val body = res.body()
    val errorBody = res.errorBody()
    if (body != null) return body
    if (errorBody != null) {
        val json = errorBody.string()
        LogUtil.e(json)
        throw ErrorCollections.CommunicationError.create(json)
    }
    throw Constants.unknownErrorException()
}
