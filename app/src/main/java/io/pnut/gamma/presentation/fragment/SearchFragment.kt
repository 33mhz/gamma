package io.pnut.gamma.presentation.fragment


import android.os.Bundle
import androidx.core.os.BundleCompat
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import kotlinx.parcelize.Parcelize
import androidx.appcompat.widget.PopupMenu
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentSearchBinding
import io.pnut.gamma.presentation.util.ShareUtil
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.SingleLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.util.Constants
import java.net.URLEncoder
import java.nio.charset.Charset

@AndroidEntryPoint
class SearchFragment : BaseFragment() {

    private val menuItemClickListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.menuSharePostSearch -> sharePostSearchRssUrl()
            else -> return@OnMenuItemClickListener false
        }
        true

    }

    private fun sharePostSearchRssUrl() {
        ShareUtil.launchShareUrlIntent(activity, postSearchRssUrl)
    }

    private val firstSearchObserver = Observer<Boolean> {
        if (!it) return@Observer
        // initial menu update handled by Event.Search observer
    }

    private val postSearchRssUrl
        get() = Constants.API_BASE_URL + "feed/rss/posts/search?q=${URLEncoder.encode(
            viewModel.lastKeyword,
            Charset.defaultCharset().name()
        )}"

    private fun updateMenu(searchType: SearchType) {
        updateMenuItemVisibility(R.id.menuSharePostSearch, searchType == SearchType.Post)
    }

    private fun updateMenuItemVisibility(itemId: Int, visibility: Boolean) {
        val menu = binding.toolbar.menu ?: return
        val menuItem = menu.findItem(itemId) ?: return
        menuItem.isVisible = visibility
    }

    private lateinit var binding: FragmentSearchBinding
    private var showKeyboardFlag: Boolean = false
    private val eventObserver = Observer<Event> {
        when (it) {
            is Event.Search -> {
                hideKeyboard()
                updatePagerInfo(it.keyword)
                showSearchResult(it.searchType, it.keyword, it.categories)
                updateMenu(it.searchType)
            }
            is Event.Clear -> {
                clearResults()
            }
        }
    }
    private val viewModel by lazy {
        ViewModelProvider(this, SearchViewModel.Factory())[SearchViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, eventObserver)
        viewModel.firstSearch.observe(this, firstSearchObserver)
        if (savedInstanceState != null) {
            BundleCompat.getParcelable(savedInstanceState, StateKey.PagerInfo.name, PagerInfo::class.java)?.let { pagerInfo = it }
        } else {
            val initialTypeOrdinal = arguments?.getInt(BundleKey.InitialType.name, SearchType.Post.ordinal) ?: SearchType.Post.ordinal
            viewModel.searchType.value = SearchType.entries[initialTypeOrdinal]
            val initialKeyword = arguments?.getString(BundleKey.InitialKeyword.name)
            val initialCategories = arguments?.getString(BundleKey.InitialCategories.name)

            if (initialKeyword != null || initialCategories != null) {
                viewModel.keyword.value = initialKeyword ?: ""
                viewModel.categories.value = initialCategories
                viewModel.search(initialCategories)
            }
        }
    }

    private fun updatePagerInfo(keyword: String): PagerInfo {
        pagerInfo = PagerInfo(System.currentTimeMillis(), keyword)
        return pagerInfo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.toolbar.setNavigationOnClickListener { backToPrevFragment() }
        binding.toolbar.setOnMenuItemClickListener(menuItemClickListener)
        binding.searchTypeButton.setOnClickListener { showSearchTypeMenu(it) }
        binding.clearButton.setOnClickListener { viewModel.clear() }
        binding.keywordEditText.doAfterTextChanged { 
            if (viewModel.keyword.value != it.toString()) {
                viewModel.keyword.value = it.toString()
            }
        }
        binding.keywordEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.action == android.view.KeyEvent.ACTION_DOWN &&
                        event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                viewModel.search()
                true
            } else {
                false
            }
        }

        viewModel.keyword.observe(viewLifecycleOwner) {
            if (binding.keywordEditText.text.toString() != it) {
                binding.keywordEditText.setText(it)
                binding.keywordEditText.setSelection(it.length)
            }
        }

        viewModel.searchType.observe(viewLifecycleOwner) {
            binding.searchTypeButton.setImageResource(it.iconRes)
        }

        viewModel.clearButtonVisibility.observe(viewLifecycleOwner) {
            binding.clearButton.visibility = it
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            updateCategoriesChips(categories)
        }

        return binding.root
    }

    private fun updateCategoriesChips(categories: String?) {
        val showChips = viewModel.searchType.value == SearchType.Chat && !categories.isNullOrEmpty()
        binding.categoriesChipGroup.visibility = if (showChips) View.VISIBLE else View.GONE
        binding.categoriesChipGroup.removeAllViews()

        if (showChips) {
            categories.split(",").filter { it.isNotBlank() }.forEach { category ->
                val chip = com.google.android.material.chip.Chip(requireContext())
                chip.text = category.trim().replaceFirstChar { it.uppercase() }
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    val current = viewModel.categories.value ?: ""
                    val updated = current.split(",")
                        .filter { it.trim() != category.trim() }
                        .joinToString(",")
                    viewModel.categories.value = updated.ifEmpty { null }
                    viewModel.search(viewModel.categories.value)
                }
                binding.categoriesChipGroup.addView(chip)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (pagerInfo.keyword.isNotEmpty()) {
            showSearchResult(viewModel.searchType.value ?: SearchType.Post, pagerInfo.keyword)
        }
        showKeyboard()
    }

    private fun showKeyboard() {
        if (showKeyboardFlag) return
        binding.keywordEditText.post {
            focusToEditText()
        }
        showKeyboardFlag = true
    }

    fun focusToEditText() {
        binding.keywordEditText.requestFocus()
        Util.showKeyboard(binding.keywordEditText)
    }

    private fun hideKeyboard() = Util.hideKeyboard(binding.keywordEditText)

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    private fun showSearchTypeMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        SearchType.entries.forEachIndexed { index, type ->
            popup.menu.add(0, index, index, type.titleRes).setIcon(type.iconRes)
        }
        Util.setTintForToolbarIcons(requireContext(), popup.menu)

        try {
            val fieldPopup = popup.javaClass.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupHelper = fieldPopup.get(popup)
            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
            val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
            setForceIcons.invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { item ->
            val type = SearchType.entries[item.itemId]
            viewModel.searchType.value = type
            updateCategoriesChips(viewModel.categories.value)
            if (viewModel.firstSearch.value == true) {
                viewModel.search(viewModel.categories.value)
            }
            true
        }
        popup.show()
    }

    private fun showSearchResult(searchType: SearchType, keyword: String, categories: String? = null) {
        val fragment = when (searchType) {
            SearchType.Post -> PostItemFragment.SearchPostsFragment.newInstance(keyword)
            SearchType.Chat -> {
                if (!categories.isNullOrEmpty()) {
                    ChannelListFragment.SearchChannelsFragment.newInstance(keyword, categories)
                } else {
                    ChannelListFragment.SearchChannelsFragment.newInstance(keyword)
                }
            }
            SearchType.User -> UserListFragment.SearchUserListFragment.newInstance(keyword)
            SearchType.PrivateMessage -> SearchMessagesFragment.newInstance(keyword)
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.searchContainer, fragment)
            .commit()
    }

    private fun clearResults() {
        val fragment = childFragmentManager.findFragmentById(R.id.searchContainer)
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    @Parcelize
    data class PagerInfo(val time: Long, val keyword: String) : Parcelable

    private enum class StateKey { PagerInfo }
    private enum class BundleKey { InitialType, InitialKeyword, InitialCategories }

    private var pagerInfo: PagerInfo = PagerInfo(System.currentTimeMillis(), "")

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(StateKey.PagerInfo.name, pagerInfo)
    }

    enum class SearchType(val titleRes: Int, val iconRes: Int) {
        Post(R.string.posts, R.drawable.ic_create_black_24dp),
        PrivateMessage(R.string.private_messages, R.drawable.ic_chat_bubble_outline_black_24dp),
        Chat(R.string.chat_rooms, R.drawable.ic_forum_black_24dp),
        User(R.string.users, R.drawable.ic_person_black_24dp),
    }

    sealed class Event {
        data class Search(val keyword: String, val searchType: SearchType, val categories: String? = null) : Event()
        object Clear : Event()
    }

    class SearchViewModel : ViewModel() {
        val keyword = MutableLiveData("")
        val categories: MutableLiveData<String?> = MutableLiveData(null)
        var lastKeyword: String = ""
        val event = SingleLiveEvent<Event>()
        var firstSearch = MutableLiveData(false)
        val searchType = MutableLiveData(SearchType.Post)

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel() as T
            }
        }

        fun search(categoriesArg: String? = null) {
            val k = keyword.value ?: ""
            firstSearch.value = true
            lastKeyword = k
            val cats = categoriesArg ?: categories.value
            event.emit(Event.Search(k, searchType.value ?: SearchType.Post, cats))
        }

        fun clear() {
            keyword.value = ""
            @Suppress("NullSafeMutableLiveData")
            categories.value = null
            firstSearch.value = false
            event.emit(Event.Clear)
        }

        val clearButtonVisibility: LiveData<Int> = keyword.map {
            if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    companion object {
        fun newInstance(initialType: SearchType = SearchType.Post, initialKeyword: String? = null, initialCategories: String? = null) = SearchFragment().apply {
            arguments = Bundle().apply {
                putInt(BundleKey.InitialType.name, initialType.ordinal)
                putString(BundleKey.InitialKeyword.name, initialKeyword)
                putString(BundleKey.InitialCategories.name, initialCategories)
            }
        }
    }
}
