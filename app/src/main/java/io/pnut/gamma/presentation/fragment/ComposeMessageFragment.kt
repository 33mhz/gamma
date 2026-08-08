package io.pnut.gamma.presentation.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ComposeThumbnailImageBinding
import io.pnut.gamma.databinding.FragmentComposePostBinding
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.domain.entity.raw.RawValue
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.domain.model.io.CreateMessageInputData
import io.pnut.gamma.domain.model.io.CreatePollInputData
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.domain.usecases.CreateMessageUseCase
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.domain.usecases.UploadFileUseCase
import io.pnut.gamma.presentation.activity.EditPhotoActivity
import io.pnut.gamma.presentation.util.AnimationCallback
import io.pnut.gamma.presentation.util.BackPressedHookable
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.Constants
import io.pnut.gamma.util.ErrorCollections
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.SingleLiveEvent
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.entity.raw.LongPost
import io.pnut.gamma.domain.entity.raw.PollNotice
import io.pnut.gamma.domain.entity.raw.Spoiler
import io.pnut.gamma.domain.entity.raw.replacement.PostPoll
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.util.observeOnce
import io.pnut.gamma.util.showAsError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ComposeMessageFragment : BaseFragment(),
    AnimationCallback,
    BackPressedHookable, ComposeLongPostFragment.Callback, SpoilerDialogFragment.Callback,
    ChangeAccountDialogFragment.Callback, ComposePollFragment.Callback {

    override fun onDiscardPoll() {
        viewModel.enablePoll.value = false
        updatePollMenuItem()
    }

    override fun onUpdateLongPost(longPost: LongPost?) {
        viewModel.longPost = longPost
        updateLongPostMenuItem()
    }

    override fun onUpdateSpoiler(spoiler: Spoiler?) {
        viewModel.spoiler = spoiler
        updateSpoilerMenuItem()
    }

    override fun changeAccount(account: Account) {
        viewModel.currentUserIdLiveData.value = account.id
    }

    override fun onAnimationEnd(open: Boolean) {
        if (open) {
            focusToEditText()
        }
    }

    override fun onBackPressed() {
        cancelToCompose()
    }

    override fun onAnimationStart(open: Boolean) {
        if (!open) {
            Util.hideKeyboard(binding.composeTextEditText)
        }
    }

    interface Callback {
        fun onFinish()
        fun addFragment(fragment: Fragment)
    }

    private enum class BundleKey {
        ChannelId, ReplyTarget, InitialText, InitialPhoto
    }

    private enum class DialogKey {
        Discard, Spoiler, Accounts, LongPost
    }

    private enum class RequestCode { Discard }

    private val enablePollObserver = Observer<Boolean> {
        updatePollMenuItem()
        togglePollView(it)
    }

    private val pollFragment: ComposePollFragment?
        get() = childFragmentManager.findFragmentById(R.id.pollLayout) as? ComposePollFragment

    private fun togglePollView(it: Boolean) {
        val ft = childFragmentManager.beginTransaction()
        if (it) {
            ft
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.pollLayout, ComposePollFragment.newInstance())
        } else {
            pollFragment?.let { ft.remove(it) }
        }
        ft.commit()
    }

    private var listener: Callback? = null
    private val thumbnailAdapterListener = object : ThumbnailAdapter.Callback {
        override fun updateList(list: List<UriInfo>) {
            viewModel.media = list.toMutableList()
        }

        override fun onClick(uri: Uri, index: Int) {
            val newIntent = EditPhotoActivity.newIntent(requireContext(), uri, index)
            editPhotoLauncher.launch(newIntent)
        }

        override fun onRemove() {
            if (adapter.getItems().isNotEmpty()) return
            viewModel.previewAttachmentsVisibility.value = View.GONE
        }
    }

    private val counterObserver = Observer<Int> {
        updateSendMenuItem()
    }

    private fun updateSendMenuItem() {
        val menuItem = findMenuItemWithinRightMenu(R.id.menuPost) ?: return
        val count = viewModel.counter.value ?: 0
        val enabled = (0 <= count) && count < viewModel.maxLength
        menuItem.isEnabled = enabled
    }

    private fun findMenuItemWithinRightMenu(menuId: Int): MenuItem? {
        val menu = binding.viewRightActionMenuView.menu ?: return null
        return menu.findItem(menuId)
    }

    private fun findMenuItemWithinLeftMenu(menuId: Int): MenuItem? {
        val menu = binding.viewLeftActionMenuView.menu ?: return null
        return menu.findItem(menuId)
    }

    private fun updateNsfwMenuItem() {
        val nsfwMenuItem = findMenuItemWithinLeftMenu(R.id.menuNsfw) ?: return
        val nsfwFlag = viewModel.nsfw.value ?: false
        nsfwMenuItem.isChecked = nsfwFlag
        Util.setTintForCheckableMenuItem(requireContext(), nsfwMenuItem)
    }

    private fun updatePollMenuItem() {
        val pollMenuItem = findMenuItemWithinLeftMenu(R.id.menuPoll) ?: return
        pollMenuItem.isChecked = viewModel.enablePoll.value == true
        Util.setTintForCheckableMenuItem(requireContext(), pollMenuItem)
    }

    private fun updateSpoilerMenuItem() {
        val spoilerMenuItem = findMenuItemWithinLeftMenu(R.id.menuSpoiler) ?: return
        spoilerMenuItem.isChecked = viewModel.spoiler != null
        Util.setTintForCheckableMenuItem(requireContext(), spoilerMenuItem)
    }

    private fun syncMenuState() {
        updateSendMenuItem()
        updateNsfwMenuItem()
        updateSpoilerMenuItem()
        updatePollMenuItem()
        updateLongPostMenuItem()
    }

    private fun updateLongPostMenuItem() {
        val longPostMenuItem = findMenuItemWithinLeftMenu(R.id.menuLongPost) ?: return
        longPostMenuItem.isChecked = viewModel.longPost != null
        Util.setTintForCheckableMenuItem(requireContext(), longPostMenuItem)
    }

    private val viewModel: ComposeMessageViewModel by lazy {
        ViewModelProvider(
            this,
            ComposeMessageViewModel.Factory(
                channelId,
                replyTarget,
                mentionToMyself,
                initialText,
                currentUserId,
                uploadFileUseCase,
                createMessageUseCase,
                createPollUseCase,
            )
        )[ComposeMessageViewModel::class.java]
    }

    private val channelId: String by lazy {
        arguments?.getString(BundleKey.ChannelId.name).orEmpty()
    }

    private val replyTarget: Message? by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.ReplyTarget.name, Message::class.java) }
    }

    private var _binding: FragmentComposePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ThumbnailAdapter

    @Inject
    lateinit var getAccountListUseCase: GetAccountListUseCase

    @Inject
    lateinit var uploadFileUseCase: UploadFileUseCase

    @Inject
    lateinit var createMessageUseCase: CreateMessageUseCase

    @Inject
    lateinit var createPollUseCase: CreatePollUseCase

    private val hasAnotherAccounts by lazy { getAccountListUseCase.run(Unit).accounts.filterNot { it.id == currentUserId }.size > 1 }

    private val eventObserver = Observer<Event> {
        when (it) {
            Event.ShowAccountList -> showAccountList()
            is Event.Success -> listener?.onFinish()
            is Event.Failed -> {
                val message = when (val throwable = it.t) {
                    is ErrorCollections -> throwable.getErrorMessage(requireContext())
                    else -> throwable.localizedMessage ?: getString(R.string.communication_error)
                }
                com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).showAsError()
            }
        }
    }

    private val loadingObserver = Observer<Boolean> { loading ->
        binding.loadingLayout.visibility = if (loading) View.VISIBLE else View.GONE
        binding.viewRightActionMenuView.isEnabled = !loading
        binding.viewLeftActionMenuView.isEnabled = !loading
    }

    private val statusObserver = Observer<String?> { status ->
        binding.statusTextView.text = status
    }

    private fun showAccountList() {
        if (hasAnotherAccounts) return
        val fragment =
            ChangeAccountDialogFragment.newInstance(viewModel.currentUserIdLiveData.value.orEmpty())
        fragment.show(childFragmentManager, DialogKey.Accounts.name)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Callback
    }

    @Inject
    lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

    private val currentUserId: String by lazy {
        getCurrentAccountUseCase.run(Unit).account?.id.orEmpty()
    }

    private val mentionToMyself: Boolean by lazy {
        replyTarget != null && replyTarget?.userId == currentUserId
    }
    private val initialText by lazy {
        arguments?.getString(BundleKey.InitialText.name)
    }
    private val uriInfo by lazy {
        arguments?.let { BundleCompat.getParcelableArrayList(it, BundleKey.InitialPhoto.name, UriInfo::class.java) }
    }

    val pollPostBody
        get() = pollFragment?.let {
            val composePollViewModel =
                ViewModelProvider(it)[ComposePollFragment.ComposePollViewModel::class.java]
            composePollViewModel.generatedPollPostBody
        }

    private fun send() {
        viewModel.sendMessage(requireContext(), adapter.getItems(), pollPostBody)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, eventObserver)
        viewModel.counter.observe(this, counterObserver)
        viewModel.enablePoll.observe(this, enablePollObserver)
        uriInfo?.let { viewModel.media = it }

        childFragmentManager.setFragmentResultListener(RequestCode.Discard.name, this) { _, bundle ->
            if (bundle.getInt(BasicDialogFragment.ResponseKey.ResultCode.name) == Activity.RESULT_OK) {
                cancelToCompose(true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposePostBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun updatePhoto(data: Intent) {
        val editPhotoResult = EditPhotoActivity.parseIntent(data) ?: return
        val uriInfo = adapter.getItems()[editPhotoResult.index].copy(uri = editPhotoResult.uri)
        adapter.replace(uriInfo, editPhotoResult.index)
    }

    private val editPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        focusToEditText()
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { updatePhoto(it) }
        }
    }

    private val pickMultipleMediaLauncher = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        uris.forEach { uri ->
            adapter.add(UriInfo(uri))
        }
        if (uris.isNotEmpty()) {
            viewModel.previewAttachmentsVisibility.value = View.VISIBLE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
        viewModel.status.observe(viewLifecycleOwner, statusObserver)

        adapter = ThumbnailAdapter(viewModel.media.toMutableList(), thumbnailAdapterListener)
        viewModel.previewAttachmentsVisibility.observe(viewLifecycleOwner) {
            binding.thumbnailRecyclerView.visibility = it
        }
        binding.thumbnailRecyclerView.adapter = adapter

        Util.setTintForToolbarIcons(
            binding.viewLeftActionMenuView.context,
            binding.viewLeftActionMenuView.menu
        )
        Util.setTintForToolbarIcons(
            binding.viewRightActionMenuView.context,
            binding.viewRightActionMenuView.menu
        )

        binding.viewLeftActionMenuView.setOnMenuItemClickListener(::onMenuItemClick)
        binding.viewRightActionMenuView.setOnMenuItemClickListener(::onMenuItemClick)

        viewModel.replyTargetVisibility.observe(viewLifecycleOwner) {
            binding.replyTargetCardView.visibility = it
        }
        
        viewModel.replyTarget.observe(viewLifecycleOwner) { target ->
            target?.let {
                BindingUtil.glideAvatarSrc(binding.replyAvatarImageView, it.avatarUrl)
                binding.replyScreenNameTextView.text = it.username
                binding.replyNameTextView.text = it.name
                binding.replyDateTextView.text = DateUtil.getShortDateStr(requireContext(), it.createdAt)
                binding.replyBodyTextView.text = it.text
            }
        }

        viewModel.myAccountAvatarUrl.observe(viewLifecycleOwner) {
            BindingUtil.glideAvatarSrc(binding.myAccountAvatarImageView, it)
        }
        
        binding.myAccountAvatarImageView.setOnClickListener {
            viewModel.showAccountList()
        }
        
        binding.composeTextEditText.setText(viewModel.text.value)
        binding.composeTextEditText.doAfterTextChanged { 
            viewModel.text.value = it?.toString()
        }
        
        viewModel.counterStr.observe(viewLifecycleOwner) {
            binding.counterTextView.text = it
        }

        viewModel.text.observeOnce(viewLifecycleOwner) {
            binding.composeTextEditText.setSelection(it.length)
        }
        if (viewModel.initialized) {
            focusToEditText()
        } else {
            viewModel.initialized = true
        }
        syncMenuState()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title =
            if (replyTarget != null)
                getString(R.string.compose_reply_title_template, replyTarget?.username)
            else
                getString(R.string.send_a_message)
        binding.toolbar.setOnMenuItemClickListener(::onMenuItemClick)
        binding.toolbar.setNavigationOnClickListener {
            cancelToCompose()
        }
    }

    fun focusToEditText() {
        binding.composeTextEditText.requestFocus()
        Util.showKeyboard(binding.composeTextEditText)
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> cancelToCompose()
            R.id.menuInsertPhoto -> pickMultipleMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            R.id.menuNsfw -> toggleNSFW(item)
            R.id.menuPost -> send()
            R.id.menuLongPost -> composeLongPost()
            R.id.menuSpoiler -> setSpoiler()
            R.id.menuPoll -> enablePoll()
        }
        return true
    }

    private fun enablePoll() {
        if (viewModel.enablePoll.value == true) return
        viewModel.enablePoll.value = true
    }

    private fun setSpoiler() {
        val spoiler = viewModel.spoiler
        val dialog = SpoilerDialogFragment.newInstance(spoiler)
        dialog.show(childFragmentManager, DialogKey.Spoiler.name)
    }

    private fun composeLongPost() {
        val fragment = ComposeLongPostFragment.newInstance(viewModel.longPost)
        fragment.show(childFragmentManager, DialogKey.LongPost.name)
    }

    private fun toggleNSFW(item: MenuItem) {
        val nextValue = !item.isChecked
        item.isChecked = nextValue
        Util.setTintForCheckableMenuItem(requireContext(), item)
        viewModel.nsfw.value = nextValue
    }


    fun cancelToCompose(force: Boolean = false): Boolean {
        val hasAnyMedia = adapter.getItems().isNotEmpty()
        val hasAnyRaw =
            viewModel.longPost != null || viewModel.spoiler != null || pollPostBody != null
        val isChanged =
            viewModel.computedInitialText != viewModel.text.value || hasAnyMedia || hasAnyRaw
        if (!force && isChanged) {
            val fragment = BasicDialogFragment.Builder()
                .setMessage(R.string.discard_changes)
                .setPositive(R.string.discard)
                .setRequestKey(RequestCode.Discard.name)
                .build()
            fragment.show(childFragmentManager, DialogKey.Discard.name)
            return false
        } else {
            listener?.onFinish()
            return true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ThumbnailAdapter(
        private val items: MutableList<UriInfo> = mutableListOf(),
        private val listener: Callback
    ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
        interface Callback {
            fun onRemove()
            fun onClick(uri: Uri, index: Int)
            fun updateList(list: List<UriInfo>)
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.compose_thumbnail_image, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val uriInfo = items[position]
            Glide
                .with(holder.binding.thumbnail)
                .load(uriInfo.uri)
                .sizeMultiplier(.7f)
                .into(holder.binding.thumbnail)

            holder.binding.removeButton.setOnClickListener { remove(holder.bindingAdapterPosition) }
            holder.binding.thumbnail.setOnClickListener { listener.onClick(uriInfo.uri, holder.bindingAdapterPosition) }
        }

        private fun remove(index: Int) {
            items.removeAt(index)
            listener.onRemove()
            listener.updateList(items)
            notifyItemRemoved(index)
        }

        fun add(uriInfo: UriInfo) {
            val index = items.size
            items.add(index, uriInfo)
            listener.updateList(items)
            notifyItemInserted(index)
        }

        fun replace(uriInfo: UriInfo, index: Int) {
            items[index] = uriInfo
            listener.updateList(items)
            notifyItemChanged(index)
        }

        fun getItems() = items

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val binding = ComposeThumbnailImageBinding.bind(view)
        }
    }

    sealed class Event {
        object ShowAccountList : Event()
        object Success : Event()
        data class Failed(val t: Throwable) : Event()
    }


    class ComposeMessageViewModel(
        private val channelId: String,
        replyTargetArg: Message?,
        mentionToMyself: Boolean,
        initialText: String? = null,
        currentUserId: String,
        private val uploadFileUseCase: UploadFileUseCase,
        private val createMessageUseCase: CreateMessageUseCase,
        private val createPollUseCase: CreatePollUseCase
    ) : ViewModel() {
        val event = SingleLiveEvent<Event>()
        val currentUserIdLiveData: MutableLiveData<String> =
            MutableLiveData<String>().apply { value = currentUserId }
        val myAccountAvatarUrl: LiveData<String> =
            currentUserIdLiveData.map {
                User.getAvatarUrl(
                    it,
                    User.AvatarSize.Large
                )
            }
        var spoiler: Spoiler? = null
        var media: List<UriInfo> = emptyList()
        var initialized: Boolean = false
        val nsfw = MutableLiveData<Boolean>().apply { value = false }
        val replyTarget = MutableLiveData<Message>().apply { value = replyTargetArg }
        val replyTargetVisibility: LiveData<Int> = replyTarget.map {
            if (it != null) View.VISIBLE else View.GONE
        }
        var longPost: LongPost? = null
        val text = MutableLiveData<String>().apply { value = "" }
        val maxLength = Constants.MAX_MESSAGE_TEXT_LENGTH
        val counter: LiveData<Int> = text.map {
            val text = it ?: ""
            maxLength - text.codePointCount(0, text.length)
        }
        val counterStr: LiveData<String> = counter.map { it.toString() }
        val previewAttachmentsVisibility = MutableLiveData<Int>().apply { value = View.GONE }
        val computedInitialText by lazy {
            val replyTargetUserUsername = replyTargetArg?.username
            when {
                replyTargetUserUsername != null && !mentionToMyself -> "@$replyTargetUserUsername "
                initialText != null -> "$initialText "
                else -> ""
            }
        }
        var enablePoll = MutableLiveData<Boolean>().apply { value = false }

        val loading = MutableLiveData<Boolean>().apply { value = false }
        val status = MutableLiveData<String?>()

        init {
            text.value = computedInitialText
        }

        fun showAccountList() = event.emit((Event.ShowAccountList))

        fun sendMessage(context: Context, adapterItems: List<UriInfo>, pollPostBody: PollPostBody?) {
            val text = text.value ?: return
            val isNsfw = nsfw.value ?: false

            viewModelScope.launch {
                loading.value = true
                status.value = context.getString(R.string.uploading_images)
                
                try {
                    val cachedFiles = withContext(Dispatchers.IO) {
                        adapterItems.mapNotNull { uriInfo ->
                            copyUriToCache(context, uriInfo.uri)?.let { UriInfo(it) }
                        }
                    }

                    val raw = mutableMapOf<String, MutableList<RawValue>>()
                    longPost?.let {
                        raw.getOrPut(LongPost.TYPE) { mutableListOf() }.add(it.copy(tstamp = Date().time))
                    }
                    spoiler?.let {
                        raw.getOrPut(Spoiler.TYPE) { mutableListOf() }.add(it)
                    }

                    val replacementFileRawList = withContext(Dispatchers.IO) {
                        cachedFiles.map {
                            val inputStream = context.contentResolver.openInputStream(it.uri)
                            val fileName = getFileName(context, it.uri)
                            val res = uploadFileUseCase.run(UploadFileInputData(it, inputStream, fileName)).postOEmbedRaw
                            inputStream?.close()

                            // Cleanup cache file
                            if (it.uri.scheme == "file") {
                                it.uri.path?.let { path -> java.io.File(path).delete() }
                            }

                            res
                        }
                    }

                    pollPostBody?.let {
                        status.value = context.getString(R.string.creating_poll)
                        val res = withContext(Dispatchers.IO) {
                            createPollUseCase.run(CreatePollInputData(it))
                        }
                        val pollNotice = PostPoll.createFromPoll(res.poll)
                        raw.getOrPut(PollNotice.TYPE) { mutableListOf() }.add(pollNotice)
                    }

                    replacementFileRawList.forEach {
                        raw.getOrPut(OEmbed.TYPE) { mutableListOf() }.add(it)
                    }

                    val messageBody = PostBody(text, replyTarget.value?.id, isNsfw = isNsfw, raw = raw.toMap())
                    
                    status.value = context.getString(R.string.creating_message)
                    withContext(Dispatchers.IO) {
                        createMessageUseCase.run(CreateMessageInputData(channelId, messageBody))
                    }
                    
                    event.emit(Event.Success)
                } catch (e: Exception) {
                    LogUtil.e(e.message)
                    event.emit(Event.Failed(e))
                } finally {
                    loading.value = false
                    status.value = null
                }
            }
        }

        private fun copyUriToCache(context: Context, uri: Uri): Uri? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
                val file = java.io.File(context.cacheDir, "upload_${System.currentTimeMillis()}_${(0..1000).random()}.$extension")
                file.outputStream().use { outputStream ->
                    inputStream.use { it.copyTo(outputStream) }
                }
                Uri.fromFile(file)
            } catch (e: Exception) {
                LogUtil.e(e.message)
                null
            }
        }

        private fun getFileName(context: Context, uri: Uri): String? {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            return cursor.getString(index)
                        }
                    }
                }
            }
            return uri.path?.let { java.io.File(it).name }
        }

        class Factory(
            private val channelId: String,
            private val replyTarget: Message?,
            private val mentionToMyself: Boolean,
            private val initialText: String? = null,
            private val currentUserId: String,
            private val uploadFileUseCase: UploadFileUseCase,
            private val createMessageUseCase: CreateMessageUseCase,
            private val createPollUseCase: CreatePollUseCase
        ) :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ComposeMessageViewModel(
                    channelId,
                    replyTarget,
                    mentionToMyself,
                    initialText,
                    currentUserId,
                    uploadFileUseCase,
                    createMessageUseCase,
                    createPollUseCase
                ) as T
            }
        }
    }

    companion object {
        fun newInstance(
            channelId: String,
            initialText: String? = null,
            initialPhoto: ArrayList<UriInfo>? = null,
            replyTarget: Message? = null
        ) = ComposeMessageFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.ChannelId.name, channelId)
                putString(BundleKey.InitialText.name, initialText)
                putParcelableArrayList(BundleKey.InitialPhoto.name, initialPhoto)
                putParcelable(BundleKey.ReplyTarget.name, replyTarget)
            }
        }
    }
}
