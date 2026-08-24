package io.pnut.gamma.presentation.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentStarNoteDialogBinding
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.observeOnce

class StarNoteDialogFragment : DialogFragment(), DialogInterface.OnClickListener {
    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> ok()
            DialogInterface.BUTTON_NEGATIVE -> cancel()
        }
    }

    private fun ok() {
        val note = viewModel.note.value.orEmpty()
        listener?.onAddStarWithNote(note, adapterPosition)
    }

    private fun cancel() {
        dismiss()
    }

    private val noteObserver = Observer<String> {
        updateOkButtonEnabled(alertDialog, it.isNotEmpty())
    }

    private fun updateOkButtonEnabled(dialog: AlertDialog?, b: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = b
    }

    private val alertDialog: AlertDialog?
        get() = (dialog as? AlertDialog)

    private var listener: Callback? = null
    private val adapterPosition by lazy {
        arguments?.getInt(BundleKey.AdapterPosition.name) ?: -1
    }

    private lateinit var binding: FragmentStarNoteDialogBinding
    private val viewModel by lazy {
        ViewModelProvider(this)[StarNoteDialogViewModel::class.java]
    }

    interface Callback {
        fun onAddStarWithNote(note: String, adapterPosition: Int)
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
        viewModel.note.observe(this, noteObserver)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentStarNoteDialogBinding.inflate(layoutInflater)

        binding.noteEditText.doAfterTextChanged {
            if (viewModel.note.value != it.toString()) {
                viewModel.note.value = it.toString()
            }
        }

        viewModel.note.observe(this) {
            if (binding.noteEditText.text.toString() != it) {
                binding.noteEditText.setText(it)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.star)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel, this)
            .setPositiveButton(R.string.ok, this)
            .show()

        updateOkButtonEnabled(dialog, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        binding.noteEditText.requestFocus()
        viewModel.note.observeOnce(this) {
            binding.noteEditText.also { view ->
                view.requestFocus()
                view.setSelection(it.length)
            }
        }
        Util.showKeyboard(binding.noteEditText)
        return dialog
    }

    class StarNoteDialogViewModel : ViewModel() {
        val note = MutableLiveData<String>()
    }

    private enum class BundleKey { AdapterPosition }

    companion object {
        fun newInstance(adapterPosition: Int) = StarNoteDialogFragment().also {
            it.arguments = Bundle().also { bundle ->
                bundle.putInt(BundleKey.AdapterPosition.name, adapterPosition)
            }
        }
    }
}
