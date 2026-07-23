package io.pnut.gamma.presentation.fragment


import android.R as Rr
import android.Manifest
import io.pnut.gamma.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.parcelize.Parcelize
import io.pnut.gamma.databinding.ComposeThumbnailImageBinding
import io.pnut.gamma.databinding.FragmentComposePostBinding
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.entity.PostBodyOuter
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.raw.LongPost
import io.pnut.gamma.domain.entity.raw.RawValue
import io.pnut.gamma.domain.entity.raw.Spoiler
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.presentation.activity.EditPhotoActivity
import io.pnut.gamma.presentation.util.AnimationCallback
import io.pnut.gamma.presentation.util.BackPressedHookable
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.service.PostWorker
import io.pnut.gamma.util.Constants
import io.pnut.gamma.util.SingleLiveEvent
import io.pnut.gamma.util.observeOnce
import java.util.ArrayList
import java.util.Date
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposePostFragment : BaseFragment(), GalleryItemListDialogFragment.Listener,
    AnimationCallback,
    BackPressedHookable, ComposeLongPostFragment.Callback, SpoilerDialogFragment.Callback,
    ChangeAccountDialogFragment.Callback, ComposePollFragment.Callback {

    override fun onDiscardPoll() {
        viewModel.enablePoll.value = false
        updatePollMenuItem()
    }

    override fun changeAccount(account: Account) {
        viewModel.currentUserIdLiveData.value = account.id
    }

    override fun onUpdateLongPost(longPost: LongPost?) {
        viewModel.longPost = longPost
        parentFragmentManager.popBackStack()
    }


    override fun onUpdateSpoiler(spoiler: Spoiler?) {
        viewModel.spoiler = spoiler
        updateSpoilerMenuItem()
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
        ReplyTarget, InitialText, InitialPhoto
    }

    private enum class DialogKey {
        Gallery, Discard, Spoiler, Accounts
    }

    private enum class PermissionRequestCode {
        Storage
    }

    private enum class RequestCode { EditPhoto, Discard }

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
                .setCustomAnimations(Rr.anim.fade_in, Rr.anim.fade_out)
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
        val enabled = (0 <= count) && count < Constants.MAX_POST_TEXT_LENGTH
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


    private fun syncMenuState() {
        updateSendMenuItem()
        updateNsfwMenuItem()
        updateSpoilerMenuItem()
        updatePollMenuItem()
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

    private val viewModel: ComposePostViewModel by lazy {
        ViewModelProvider(
            this,
            ComposePostViewModel.Factory(
                replyTarget,
                mentionToMyself,
                initialText,
                currentUserId
            )
        )[ComposePostViewModel::class.java]
    }

    private val replyTarget: Post? by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.ReplyTarget.name, Post::class.java) }
    }

    private var _binding: FragmentComposePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ThumbnailAdapter

    @Inject
    lateinit var getAccountListUseCase: GetAccountListUseCase

    private val hasAnotherAccounts by lazy { getAccountListUseCase.run(Unit).accounts.filterNot { it.id == currentUserId }.size > 1 }

    private val eventObserver = Observer<Event> {
        when (it) {
            is Event.ShowAccountList -> showAccountList()
        }
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
        replyTarget != null && replyTarget?.user?.id == currentUserId
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
        val text = viewModel.text.value ?: return
        val isNsfw = viewModel.nsfw.value ?: false
        val currentUserId = viewModel.currentUserIdLiveData.value ?: return
        val raw = mutableMapOf<String, MutableList<RawValue>>()
        viewModel.longPost?.let {
            raw.getOrPut(LongPost.TYPE) { mutableListOf() }.add(it.copy(tstamp = Date().time))
        }
        viewModel.spoiler?.let {
            raw.getOrPut(Spoiler.TYPE) { mutableListOf() }.add(it)
        }


        val postBodyOuter = PostBodyOuter(
            currentUserId,
            PostBody(text, replyTarget?.id, isNsfw = isNsfw, raw = raw.toMap()),
            adapter.getItems(),
            pollPostBody
        )
        context?.let { PostWorker.enqueueSendPost(it, postBodyOuter) }
        listener?.onFinish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, eventObserver)
        viewModel.counter.observe(this, counterObserver)
        viewModel.enablePoll.observe(this, enablePollObserver)
        uriInfo?.let { viewModel.media = it }

        setFragmentResultListener(ComposeLongPostFragment.RequestKey.UpdateLongPost.name) { _, bundle ->
            val longPost = BundleCompat.getParcelable(bundle, ComposeLongPostFragment.ResponseKey.LongPost.name, LongPost::class.java)
            onUpdateLongPost(longPost)
        }

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        syncMenuState()

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
        
        viewModel.replyTarget.observe(viewLifecycleOwner) { post ->
            post?.let {
                BindingUtil.glideAvatarSrc(binding.replyAvatarImageView, it.user?.content?.avatarImage?.url)
                binding.replyScreenNameTextView.text = it.user?.username
                binding.replyNameTextView.text = it.user?.name
                binding.replyDateTextView.text = DateUtil.getShortDateStr(requireContext(), it.createdAt)
                binding.replyBodyTextView.text = it.content?.text
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
        val menu = binding.viewLeftActionMenuView.menu ?: return
        val longPostMenuItem = menu.findItem(R.id.menuLongPost) ?: return
        longPostMenuItem.isChecked = viewModel.longPost != null
        Util.setTintForCheckableMenuItem(view.context, longPostMenuItem)
    }

    private fun setupToolbar() {
        binding.toolbar.title =
            if (replyTarget != null)
                getString(R.string.compose_reply_title_template, replyTarget?.user?.username)
            else
                getString(R.string.compose_post)
        binding.toolbar.setOnMenuItemClickListener(::onMenuItemClick)
        binding.toolbar.setNavigationOnClickListener {
            cancelToCompose()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showGalleryDialog()
        }
    }

    private fun requestGalleryDialog() {
        val permission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            showGalleryDialog()
        }
    }


    private fun showGalleryDialog() {
        val fragment = GalleryItemListDialogFragment.chooseMultiple()
        fragment.show(childFragmentManager, DialogKey.Gallery.name)
    }

    override fun onGalleryItemClicked(uri: Uri, tag: String?) {
        adapter.add(UriInfo(uri))
        viewModel.previewAttachmentsVisibility.value = View.VISIBLE
    }


    override fun onShow() {
    }

    override fun onDismiss() {
        focusToEditText()
    }


    fun focusToEditText() {
        binding.composeTextEditText.requestFocus()
        Util.showKeyboard(binding.composeTextEditText)
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> cancelToCompose()
            R.id.menuInsertPhoto -> requestGalleryDialog()
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
        listener?.addFragment(fragment)
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
        RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
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

        fun addAll(uriList: List<UriInfo>) {
            items.addAll(uriList)
            listener.updateList(items)
            notifyItemRangeInserted(0, uriList.size)
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

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val binding = ComposeThumbnailImageBinding.bind(view)
        }
    }

    sealed class Event {
        object ShowAccountList : Event()
    }


    class ComposePostViewModel private constructor(
        replyTargetArg: Post?,
        mentionToMyself: Boolean,
        initialText: String? = null,
        currentUserId: String
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
        val replyTarget = MutableLiveData<Post>().apply { value = replyTargetArg }
        val replyTargetVisibility: LiveData<Int> = replyTarget.map {
            if (it != null) View.VISIBLE else View.GONE
        }
        var longPost: LongPost? = null
        val text = MutableLiveData<String>().apply { value = "" }
        val counter: LiveData<Int> = text.map {
            val text = it ?: ""
            Constants.MAX_POST_TEXT_LENGTH - text.codePointCount(0, text.length)
        }
        val counterStr: LiveData<String> = counter.map { it.toString() }
        val previewAttachmentsVisibility = MutableLiveData<Int>().apply { value = View.GONE }
        val computedInitialText by lazy {
            val replyTargetUserUsername = replyTargetArg?.user?.username
            when {
                replyTargetUserUsername != null && !mentionToMyself -> "@$replyTargetUserUsername "
                initialText != null -> "$initialText "
                else -> ""
            }
        }
        var enablePoll = MutableLiveData<Boolean>().apply { value = false }

        init {
            text.value = computedInitialText
        }

        fun showAccountList() = event.emit((Event.ShowAccountList))

        class Factory(
            private val replyTarget: Post?,
            private val mentionToMyself: Boolean,
            private val initialText: String? = null,
            private val currentUserId: String
        ) :
            ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ComposePostViewModel(
                    replyTarget,
                    mentionToMyself,
                    initialText,
                    currentUserId
                ) as T
            }

        }
    }

    @Parcelize
    data class ComposePostFragmentOption(
        val initialText: String? = null,
        val intentExtraDataList: ArrayList<UriInfo>? = null,
        val post: Post? = null
    ) : Parcelable

    companion object {
        fun newInstance(composePostFragmentOption: ComposePostFragmentOption? = null) =
            ComposePostFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.InitialText.name, composePostFragmentOption?.initialText)
                    putParcelableArrayList(
                        BundleKey.InitialPhoto.name,
                        composePostFragmentOption?.intentExtraDataList
                    )
                    putParcelable(BundleKey.ReplyTarget.name, composePostFragmentOption?.post)
                }
            }
    }
}
