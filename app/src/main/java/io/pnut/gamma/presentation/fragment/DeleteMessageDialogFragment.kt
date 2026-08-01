package io.pnut.gamma.presentation.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Message

class DeleteMessageDialogFragment : DialogFragment(), DialogInterface.OnClickListener {
    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> listener?.ok(position, message)
            DialogInterface.BUTTON_NEGATIVE -> listener?.cancel()
        }
        dismiss()
    }

    private var listener: Callback? = null
    private val position by lazy {
        arguments?.getInt(BundleKey.Position.name) ?: throw NullPointerException("Must set Position")
    }

    private val message by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.Message.name, Message::class.java) } ?: throw NullPointerException("Must set Message")
    }

    enum class BundleKey { Position, Message }

    interface Callback {
        fun ok(position: Int, message: Message)
        fun cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Callback
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.this_operation_cannot_be_undone)
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, this)
            .show()
    }

    companion object {
        fun newInstance(position: Int, message: Message) = DeleteMessageDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(BundleKey.Position.name, position)
                putParcelable(BundleKey.Message.name, message)
            }
        }
    }
}
