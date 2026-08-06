package io.pnut.gamma.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import io.pnut.gamma.R
import io.pnut.gamma.databinding.AccountListFooterItemBinding
import io.pnut.gamma.databinding.AccountListItemBinding
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.presentation.util.BindingUtil

class AccountListAdapter(
    private val accounts: List<Account>,
    private val listener: Listener,
    private val showAddAccountButton: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ItemViewType { Body, Footer }

    interface Listener {
        fun onAccountClick(account: Account)
        fun onAddAccount()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ItemViewType.entries[viewType]) {
            ItemViewType.Body -> ItemViewHolder(
                inflater.inflate(
                    R.layout.account_list_item,
                    parent,
                    false
                )
            )
            ItemViewType.Footer -> FooterViewHolder(
                inflater.inflate(
                    R.layout.account_list_footer_item,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type =
            if (!showAddAccountButton || itemCount - 1 > position) ItemViewType.Body else ItemViewType.Footer
        return type.ordinal
    }

    override fun getItemCount(): Int =
        if (showAddAccountButton) accounts.size + 1 else accounts.size // item + footer

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ordinal = getItemViewType(position)
        when (ItemViewType.entries[ordinal]) {
            ItemViewType.Body -> {
                (holder as? ItemViewHolder)?.also {
                    val account = accounts[position]
                    it.bindTo(account)
                    it.itemView.setOnClickListener { listener.onAccountClick(account) }
                }
            }
            ItemViewType.Footer -> (holder as? FooterViewHolder)?.also {
                it.binding.addAccountButton.setOnClickListener { listener.onAddAccount() }
            }
        }
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AccountListItemBinding.bind(itemView)
        private val avatarView: ImageView = binding.accountListItemAvatarImageView
        private val usernameView: TextView = binding.accountListItemScreenNameTextView
        private val nameView: TextView = binding.accountListItemNameTextView
        fun bindTo(account: Account) {
            BindingUtil.glideAvatarSrc(avatarView, account.getAvatarUrl())
            usernameView.text = account.usernameWithAt
            nameView.text = account.name
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AccountListFooterItemBinding.bind(itemView)
    }
}
