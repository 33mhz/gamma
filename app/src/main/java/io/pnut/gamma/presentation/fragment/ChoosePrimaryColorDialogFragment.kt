package io.pnut.gamma.presentation.fragment


import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.pnut.gamma.presentation.adapter.ColorListAdapter
import io.pnut.gamma.presentation.util.ThemeColorUtil
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentChoosePrimaryColorDialogBinding

class ChoosePrimaryColorDialogFragment : DialogFragment(), DialogInterface.OnClickListener,
    ColorListAdapter.Callback {
    override fun chooseThemeColor(themeColor: ThemeColorUtil.ThemeColor) {
        viewModel.themeColor = themeColor
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> listener?.updateColor(viewModel.themeColor)
            DialogInterface.BUTTON_NEGATIVE -> dismiss()
            DialogInterface.BUTTON_NEUTRAL -> listener?.setAsDefault()
        }
    }

    private var listener: Callback? = null
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ChoosePrimaryColorDialogViewModel.Factory(themeColor)
        )[ChoosePrimaryColorDialogViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Callback
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Callback {
        fun updateColor(themeColor: ThemeColorUtil.ThemeColor?)
        fun setAsDefault()
    }

    private val themeColor by lazy {
        arguments?.let { BundleCompat.getSerializable(it, BundleKey.ThemeColor.name, ThemeColorUtil.ThemeColor::class.java) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.MaterialAlertDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentChoosePrimaryColorDialogBinding.inflate(layoutInflater)
        binding.colorList.adapter = ColorListAdapter(this, themeColor)
        binding.colorList.setHasFixedSize(true)
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.ok, this)
            .setNeutralButton(R.string.default_text, this)
            .setNegativeButton(R.string.cancel, this)
            .setTitle(R.string.change_theme_color)
            .create()
    }


    class ChoosePrimaryColorDialogViewModel(themeColorArg: ThemeColorUtil.ThemeColor?) :
        ViewModel() {
        var themeColor: ThemeColorUtil.ThemeColor? = themeColorArg

        class Factory(private val themeColor: ThemeColorUtil.ThemeColor?) :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChoosePrimaryColorDialogViewModel(themeColor) as T
            }
        }
    }

    private enum class BundleKey { ThemeColor }

    companion object {
        fun newInstance(themeColor: ThemeColorUtil.ThemeColor?) =
            ChoosePrimaryColorDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BundleKey.ThemeColor.name, themeColor)
                }
            }
    }

}
