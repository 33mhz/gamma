package io.pnut.gamma.presentation.fragment


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.model.PollDeadline
import io.pnut.gamma.presentation.adapter.ComposePollListAdapter
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.SingleLiveEvent
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentComposePollBinding

class ComposePollFragment : BaseFragment(), ComposePollOptionFragment.Callback {
    override fun ok(updatedPollPostBody: PollPostBody) {
        viewModel.pollPostBody.value = updatedPollPostBody
    }

    private var listener: Callback? = null
    private val eventObserver = Observer<Event> {
        when (it) {
            is Event.AddOption -> addOptionIfPossible()
            is Event.OpenMoreOptions -> openMoreOptions()
        }
    }

    interface Callback {
        fun onDiscardPoll()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Callback
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }

    private fun openMoreOptions() {
        val generatedPollPostBody = viewModel.generatedPollPostBody ?: return
        val fragment = ComposePollOptionFragment.newInstance(generatedPollPostBody)
        LogUtil.d("generatedPollPostBody, $generatedPollPostBody")
        fragment.show(childFragmentManager, DialogKey.PollOption.name)
    }

    private enum class DialogKey { PollOption, Discard }

    private fun addOptionIfPossible() {
        composePollListAdapter.addItem()
        LogUtil.d("generatedPollPostBody: ${viewModel.generatedPollPostBody}")
    }

    private lateinit var binding: FragmentComposePollBinding

    val viewModel by lazy {
        ViewModelProvider(
            this
//            ComposePollViewModel.Factory(activity!!.application, pollPostBody)
        )[ComposePollViewModel::class.java]
    }
    private val composePollListAdapter by lazy {
        ComposePollListAdapter(viewModel.items, viewModel.enableAddOptionButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, eventObserver)
        childFragmentManager.setFragmentResultListener(RequestCode.Discard.name, this) { _, bundle ->
            if (bundle.getInt(BasicDialogFragment.ResponseKey.ResultCode.name) == Activity.RESULT_OK) {
                discard()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComposePollBinding.inflate(inflater, container, false)
        binding.composePollRecyclerView.adapter = composePollListAdapter
        binding.composePollRecyclerView.isNestedScrollingEnabled = false

        binding.composePollOptionalPromptTextEditText.doAfterTextChanged {
            if (viewModel.prompt.value != it.toString()) {
                viewModel.prompt.value = it.toString()
            }
        }

        viewModel.prompt.observe(viewLifecycleOwner) {
            if (binding.composePollOptionalPromptTextEditText.text.toString() != it) {
                binding.composePollOptionalPromptTextEditText.setText(it)
            }
        }

        viewModel.enableAddOptionButton.observe(viewLifecycleOwner) {
            binding.composePollOptionalAddChoiceButton.isEnabled = it
        }

        binding.composePollOptionalAddChoiceButton.setOnClickListener {
            viewModel.addOption()
        }

        binding.composePollMoreOptionsLayout.setOnClickListener {
            viewModel.openMoreOptions()
        }

        viewModel.durationStr.observe(viewLifecycleOwner) {
            binding.composePollDurationValue.text = it
        }

        viewModel.maxOptions.observe(viewLifecycleOwner) {
            binding.composePollMaxOptionsValue.text = it.toString()
        }

        viewModel.isAnonymous.observe(viewLifecycleOwner) {
            binding.composeAnonymousValue.text = getString(if (it) R.string.yes else R.string.no)
        }

        binding.composePollToolbar.let {
            AppCompatResources.getColorStateList(it.context, R.color.toolbar_icon_tint)
            val colorStateList =
                resources.getColorStateList(R.color.toolbar_icon_tint, resources.newTheme())
            Util.setTintForToolbarIcon(
                colorStateList,
                binding.composePollToolbar.menu.findItem(R.id.menuDiscardPoll)
            )
            it.setOnMenuItemClickListener {
                discardConfirmation()
                false
            }
            return binding.root
        }
    }

    private enum class RequestCode { Discard }

    private fun discardConfirmation() {
        if (viewModel.generatedPollPostBody?.edited == true) {
            val fragment = BasicDialogFragment.Builder()
                .setMessage(R.string.this_operation_cannot_be_undone)
                .setPositive(R.string.discard)
                .setRequestKey(RequestCode.Discard.name)
                .build()
            fragment.show(childFragmentManager, DialogKey.Discard.name)
        } else {
            discard()
        }
    }



    private fun discard() {
        viewModel.pollPostBody.value = null
        listener?.onDiscardPoll()
    }

    sealed class Event {
        object AddOption : Event()
        object OpenMoreOptions : Event()
    }

    class ComposePollViewModel(private val app: Application) :
        AndroidViewModel(app) {
        val pollPostBody =
            MutableLiveData<PollPostBody>().apply { value = PollPostBody.defaultValue }
        val prompt = MutableLiveData<String>().apply { value = "" }
        val items = PollPostBody.PollOption.template
        val isAnonymous: LiveData<Boolean> = pollPostBody.map {
            it?.isAnonymous == true
        }
        val maxOptions: LiveData<Int> = pollPostBody.map {
            it?.maxOptions ?: 1
        }
        val duration: LiveData<Int?> = pollPostBody.map {
            it?.duration
        }
        val durationStr: LiveData<String> = pollPostBody.map {
            if (it == null) return@map ""
            PollDeadline.fromInt(it.duration).toFormatString(app)
        }

        val generatedPollPostBody
            get() = pollPostBody.value?.copy(
                prompt = prompt.value.orEmpty(),
                options = items
            )


        val event = SingleLiveEvent<Event>()
        val enableAddOptionButton = MutableLiveData<Boolean>().apply { value = true }
        fun addOption() = event.emit(Event.AddOption)
        fun openMoreOptions() = event.emit(Event.OpenMoreOptions)

//        class Factory(private val app: Application, private val pollPostBody: PollPostBody) :
//            ViewModelProvider.AndroidViewModelFactory(app) {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                return ComposePollViewModel(app) as T
//
//            }
//        }
    }

    companion object {
        fun newInstance() = ComposePollFragment()
    }
}
