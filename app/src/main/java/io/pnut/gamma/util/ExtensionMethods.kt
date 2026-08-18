package io.pnut.gamma.util

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.pnut.gamma.R
import com.google.android.material.R as Rm
import com.google.android.material.snackbar.Snackbar
import java.util.Date

fun Snackbar.showAsError() {
    val view: View = this.view
    val bgColor: Int = ContextCompat.getColor(view.context, R.color.colorError)
    view.setBackgroundColor(bgColor)
    val textView: TextView = view.findViewById(Rm.id.snackbar_text)
    textView.setTextColor(Color.WHITE)
    show()
}

fun Snackbar.oneLine(): Snackbar {
    val view: View = this.view
    val textView: TextView = view.findViewById(Rm.id.snackbar_text)
    textView.maxLines = 1
    textView.ellipsize = TextUtils.TruncateAt.END
    return this
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
    val dateFormatTemplate: String? = context?.getString(R.string.file_date_format_template)
    return DateFormat.format(dateFormatTemplate, this).toString()
}
