package io.pnut.gamma.presentation.fragment


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.presentation.adapter.AccountListAdapter
import io.pnut.gamma.util.ErrorCollections
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentChangeAccountDialogBinding
import javax.inject.Inject

@AndroidEntryPoint
class ChangeAccountDialogFragment : DialogFragment() {
    private val currentUserId: String by lazy {
        arguments?.getString(BundleKey.CurrentUserId.name) ?: throw ErrorCollections.AccountNotFound()
    }

    private enum class BundleKey { CurrentUserId }

    interface Callback {
        fun changeAccount(account: Account)
    }

    private var listener: Callback? = null

    @Inject
    lateinit var getAccountListUseCase: GetAccountListUseCase

    private val accounts
        get() = getAccountListUseCase.run(Unit).accounts.filterNot { it.id == currentUserId }

    private val accountListListener = object : AccountListAdapter.Listener {
        override fun onAccountClick(account: Account) {
            listener?.changeAccount(account)
            dismiss()
        }

        override fun onAddAccount() {
        }

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
        val binding = FragmentChangeAccountDialogBinding.inflate(layoutInflater)
        binding.accountListInclude.accountList.adapter = AccountListAdapter(
            accounts,
            accountListListener,
            false
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.users)
            .setView(binding.root)
            .create()
    }

    companion object {
        fun newInstance(currentUserId: String) = ChangeAccountDialogFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.CurrentUserId.name, currentUserId)
            }
        }
    }

}
