package io.pnut.gamma.presentation.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentComposeLongPostBinding
import io.pnut.gamma.domain.entity.raw.LongPost
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.observeOnce

@AndroidEntryPoint
class ComposeLongPostFragment : DialogFragment(), DialogInterface.OnClickListener {
    
    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> ok()
            DialogInterface.BUTTON_NEUTRAL -> remove()
            DialogInterface.BUTTON_NEGATIVE -> cancel()
        }
    }

    private fun ok() {
        val body = viewModel.body.value ?: ""
        val title = viewModel.title.value?.takeIf { it.isNotEmpty() }
        val longPost = if (body.isNotEmpty()) {
            LongPost(body, title, 0L)
        } else {
            null
        }
        listener?.onUpdateLongPost(longPost)
    }

    private fun remove() {
        listener?.onUpdateLongPost(null)
    }

    private fun cancel() {
        dismiss()
    }

    private val bodyObserver = Observer<String> {
        updateOkButtonEnabled(alertDialog, it.isNotEmpty())
    }

    private fun updateOkButtonEnabled(dialog: AlertDialog?, b: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = b
    }

    private val alertDialog: AlertDialog?
        get() = (dialog as? AlertDialog)

    private fun updateRemoveButtonEnabled(dialog: AlertDialog?, b: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_NEUTRAL)?.isEnabled = b
    }

    private var listener: Callback? = null
    private val longPost by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.LongPost.name, LongPost::class.java) }
    }

    private lateinit var binding: FragmentComposeLongPostBinding
    private val viewModel by lazy {
        ViewModelProvider(this, ComposeLongPostViewModel.Factory(longPost))[ComposeLongPostViewModel::class.java]
    }

    interface Callback {
        fun onUpdateLongPost(longPost: LongPost?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Callback
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.body.observe(this, bodyObserver)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_compose_long_post,
            null,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.long_post)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel, this)
            .setPositiveButton(R.string.ok, this)

        if (longPost != null) {
            builder.setNeutralButton(R.string.remove, this)
        }
        val dialog = builder.show()
        updateRemoveButtonEnabled(dialog, longPost != null)
        updateOkButtonEnabled(dialog, longPost != null)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        binding.bodyEditText.requestFocus()
        viewModel.body.observeOnce(this) { text ->
            text?.let {
                binding.bodyEditText.also { view ->
                    view.requestFocus()
                    view.setSelection(it.length)
                }
            }
        }
        Util.showKeyboard(binding.bodyEditText)
        return dialog
    }

    class ComposeLongPostViewModel(private val longPost: LongPost?) : ViewModel() {
        val title = MutableLiveData<String>().also { liveData ->
            liveData.value = longPost?.title ?: ""
        }
        val body = MutableLiveData<String>().also { liveData ->
            liveData.value = longPost?.body ?: ""
        }

        class Factory(private val longPost: LongPost?) :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ComposeLongPostViewModel(longPost) as T
            }
        }
    }

    private enum class BundleKey { LongPost }

    companion object {
        fun newInstance(longPost: LongPost?) = ComposeLongPostFragment().also {
            it.arguments = Bundle().also { bundle ->
                bundle.putParcelable(BundleKey.LongPost.name, longPost)
            }
        }
    }
}
