package io.pnut.gamma.presentation.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.presentation.adapter.ComposeThumbnailAdapter
import io.pnut.gamma.presentation.adapter.UserSuggestionAdapter
import io.pnut.gamma.databinding.FragmentNewPrivateMessageBinding
import io.pnut.gamma.domain.entity.raw.Spoiler
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.presentation.activity.EditPhotoActivity
import io.pnut.gamma.presentation.util.CameraDelegate
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.presentation.viewmodel.NewPrivateMessageViewModel
import io.pnut.gamma.util.Constants
import io.pnut.gamma.util.ErrorCollections

@AndroidEntryPoint
class NewPrivateMessageFragment : BaseFragment(),
    SpoilerDialogFragment.Callback,
    ComposePollFragment.Callback {

    private var _binding: FragmentNewPrivateMessageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPrivateMessageViewModel by viewModels()
    private lateinit var suggestionAdapter: UserSuggestionAdapter

    private val adapter: ComposeThumbnailAdapter by lazy {
        ComposeThumbnailAdapter(listener = thumbnailAdapterListener)
    }

    private val thumbnailAdapterListener = object : ComposeThumbnailAdapter.Callback {
        override fun updateList(list: List<UriInfo>) {
            viewModel.media = list.toMutableList()
        }

        override fun onClick(uri: Uri, index: Int) {
            val newIntent = EditPhotoActivity.newIntent(requireContext(), uri, index)
            editPhotoLauncher.launch(newIntent)
        }

        override fun onRemove() {
            if (adapter.getItems().isNotEmpty()) return
            binding.thumbnailRecyclerView.visibility = View.GONE
        }
    }

    override fun onDiscardPoll() {
        viewModel.enablePoll.value = false
    }

    override fun onUpdateSpoiler(spoiler: Spoiler?) {
        viewModel.spoiler.value = spoiler
    }

    private var listener: Callback? = null

    interface Callback {
        fun onFinish()
        fun addFragment(fragment: androidx.fragment.app.Fragment)
    }

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        listener = context as? Callback
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNewPrivateMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                backToPrevFragment()
            } else {
                listener?.onFinish()
            }
        }

        binding.usernamesEditText.doAfterTextChanged {
            val usernameText = it?.toString() ?: ""
            viewModel.usernames.value = usernameText
            binding.usernamesLayout.error = null
            val hasUsernames = usernameText.isNotEmpty()
            binding.textLayout.isVisible = hasUsernames
            binding.counterTextView.isVisible = hasUsernames
            binding.bottomToolbar.isVisible = hasUsernames
            updatePostMenuItem()
            viewModel.onTextChanged(usernameText, binding.usernamesEditText.selectionStart, requireAtSymbol = false)
        }

        binding.textEditText.doAfterTextChanged {
            viewModel.text.value = it?.toString() ?: ""
            binding.textEditText.post { updatePostMenuItem() }
            viewModel.onTextChanged(it?.toString().orEmpty(), binding.textEditText.selectionStart)
        }

        suggestionAdapter = UserSuggestionAdapter { user ->
            val focusedView = if (binding.usernamesEditText.isFocused) binding.usernamesEditText else binding.textEditText
            Util.insertMention(focusedView, user.username, addAtSymbol = focusedView == binding.usernamesEditText)
        }
        binding.suggestionRecyclerView.adapter = suggestionAdapter

        viewModel.suggestions.observe(viewLifecycleOwner) {
            suggestionAdapter.submitList(it)
            binding.suggestionRecyclerView.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.counter.observe(viewLifecycleOwner) {
            binding.counterTextView.text = it.toString()
            updatePostMenuItem()
        }

        binding.lookupButton.setOnClickListener {
            viewModel.onLookup(requireContext())
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NewPrivateMessageViewModel.Event.NavigateToChannel -> {
                    navigateToChannel(event.channelId, event.title, ArrayList(event.usernames))
                }
                is NewPrivateMessageViewModel.Event.Error -> {
                    val message = ErrorCollections.getErrorMessage(requireContext(), event.throwable)
                    binding.usernamesLayout.error = message
                }
            }
        }

        binding.viewLeftActionMenuView.setOnMenuItemClickListener(::onMenuItemClick)
        binding.viewRightActionMenuView.setOnMenuItemClickListener(::onMenuItemClick)

        findMenuItemWithinLeftMenu(R.id.menuLongPost)?.isVisible = false

        viewModel.enablePoll.observe(viewLifecycleOwner) {
            togglePollView(it)
        }

        viewModel.status.observe(viewLifecycleOwner) {
            binding.statusTextView.text = it
        }

        viewModel.spoiler.observe(viewLifecycleOwner) {
            updateSpoilerMenuItem()
        }

        viewModel.nsfw.observe(viewLifecycleOwner) {
            updateNsfwMenuItem()
        }

        val prepopulatedUsernames = arguments?.getStringArrayList("usernames")
        if (!prepopulatedUsernames.isNullOrEmpty()) {
            val usernamesText = prepopulatedUsernames.joinToString(", ") { "@$it" }
            binding.usernamesEditText.setText(usernamesText)
            binding.lookupButton.visibility = View.GONE
            binding.usernamesEditText.isFocusable = false
            binding.usernamesEditText.isFocusableInTouchMode = false
            binding.usernamesEditText.isCursorVisible = false
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingLayout.isVisible = isLoading
            binding.lookupButton.isEnabled = !isLoading
            updatePostMenuItem()
        }

        binding.thumbnailRecyclerView.adapter = adapter
        syncMenuState()
    }

    private fun syncMenuState() {
        val ctx = context ?: return
        Util.setTintForToolbarIcons(ctx, binding.viewLeftActionMenuView.menu)
        Util.setTintForToolbarIcons(ctx, binding.viewRightActionMenuView.menu)
        updatePostMenuItem()
        updateNsfwMenuItem()
        updatePollMenuItem()
        updateSpoilerMenuItem()
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menuTakePhoto -> cameraDelegate.takePhoto()
            R.id.menuInsertPhoto -> pickMultipleMediaLauncher.launch(Constants.SUPPORTED_IMAGE_TYPES)
            R.id.menuPoll -> viewModel.enablePoll.value = viewModel.enablePoll.value == false
            R.id.menuSpoiler -> setSpoiler()
            R.id.menuNsfw -> toggleNSFW()
            R.id.menuPost -> viewModel.onSend(requireContext(), pollFragment?.viewModel?.generatedPollPostBody)
        }
        return true
    }

    private var pollFragment: ComposePollFragment? = null

    private fun togglePollView(enable: Boolean) {
        val fm = childFragmentManager
        if (enable) {
            pollFragment = ComposePollFragment.newInstance()
            fm.beginTransaction().replace(R.id.pollLayout, pollFragment!!).commit()
            binding.pollLayout.visibility = View.VISIBLE
        } else {
            pollFragment?.let { fm.beginTransaction().remove(it).commit() }
            pollFragment = null
            binding.pollLayout.visibility = View.GONE
        }
        updatePollMenuItem()
    }

    private fun updatePollMenuItem() {
        val pollMenuItem = findMenuItemWithinLeftMenu(R.id.menuPoll) ?: return
        pollMenuItem.isChecked = viewModel.enablePoll.value == true
        Util.setTintForCheckableMenuItem(requireContext(), pollMenuItem)
    }

    private fun updateSpoilerMenuItem() {
        val spoilerMenuItem = findMenuItemWithinLeftMenu(R.id.menuSpoiler) ?: return
        spoilerMenuItem.isChecked = viewModel.spoiler.value != null
        Util.setTintForCheckableMenuItem(requireContext(), spoilerMenuItem)
    }

    private fun updateNsfwMenuItem() {
        val nsfwMenuItem = findMenuItemWithinLeftMenu(R.id.menuNsfw) ?: return
        val nsfwFlag = viewModel.nsfw.value ?: false
        nsfwMenuItem.isChecked = nsfwFlag
        Util.setTintForCheckableMenuItem(requireContext(), nsfwMenuItem)
    }

    private fun toggleNSFW() {
        viewModel.nsfw.value = !(viewModel.nsfw.value ?: false)
    }

    private fun setSpoiler() {
        val dialog = SpoilerDialogFragment.newInstance(viewModel.spoiler.value)
        dialog.show(childFragmentManager, NewPrivateMessageFragment::class.java.simpleName)
    }

    private fun findMenuItemWithinLeftMenu(menuId: Int): MenuItem? {
        val menu = binding.viewLeftActionMenuView.menu
        return menu.findItem(menuId)
    }

    private fun updatePhoto(intent: Intent) {
        val result = EditPhotoActivity.parseIntent(intent) ?: return
        val index = result.index
        val uri = result.uri
        if (index == -1) {
            adapter.add(UriInfo(uri))
        } else {
            adapter.replace(UriInfo(uri), index)
        }
        binding.thumbnailRecyclerView.visibility = View.VISIBLE
    }

    private val editPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { intent -> updatePhoto(intent) }
        }
    }

    private val pickMultipleMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            adapter.addAll(uris.map { UriInfo(it) })
            binding.thumbnailRecyclerView.visibility = View.VISIBLE
        }
    }

    private val cameraDelegate = CameraDelegate(this) { uri ->
        adapter.add(UriInfo(uri))
        binding.thumbnailRecyclerView.visibility = View.VISIBLE
    }

    private fun updatePostMenuItem() {
        val usernames = binding.usernamesEditText.text?.toString() ?: ""
        val text = binding.textEditText.text?.toString() ?: ""
        val hasUsernames = usernames.trim().isNotEmpty()
        val hasText = text.trim().isNotEmpty()

        val count = Constants.MAX_MESSAGE_TEXT_LENGTH - if (text.isEmpty()) 0 else text.codePointCount(0, text.length)
        val withinLimit = count >= 0
        val isLoading = viewModel.loading.value ?: false

        binding.viewRightActionMenuView.menu.findItem(R.id.menuPost)?.let {
            it.isEnabled = hasUsernames && hasText && withinLimit && !isLoading
        }
    }

    private fun navigateToChannel(channelId: String, title: String, usernames: ArrayList<String>) {
        val intent = Intent(requireContext(), io.pnut.gamma.presentation.activity.MainActivity::class.java).apply {
            putExtra("CHANNEL_ID", channelId)
            putExtra("CHANNEL_TITLE", title)
            putStringArrayListExtra("USERNAMES", usernames)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        listener?.onFinish()
    }

    fun focusToEditText() {
        val target = if (!arguments?.getStringArrayList("usernames").isNullOrEmpty()) {
            binding.textEditText
        } else {
            binding.usernamesEditText
        }
        target.requestFocus()
        Util.showKeyboard(target)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(usernames: ArrayList<String>? = null) = NewPrivateMessageFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList("usernames", usernames)
            }
        }
    }
}
