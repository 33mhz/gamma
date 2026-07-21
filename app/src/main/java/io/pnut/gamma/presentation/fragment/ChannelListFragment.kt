package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import androidx.core.os.BundleCompat
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.model.ChannelType
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.GetChannelsInputData
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.model.params.single.GeneralChannelParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.GetChannelsUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : BaseListFragment<Channel, ChannelListFragment.ChannelViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Channel, ChannelListFragment.ChannelViewHolder> {
    private val channelType: ChannelType by lazy {
        arguments?.let { BundleCompat.getSerializable(it, BundleKey.ChannelType.name, ChannelType::class.java) }
            ?: ChannelType.Public
    }

    @Inject
    lateinit var getChannelUseCase: GetChannelsUseCase

    override fun getFragmentLayout(): Int = R.layout.fragment_base_list
    override fun getRecyclerView(view: View): RecyclerView = view.findViewById(R.id.baseList)
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    override val viewModel: BaseListViewModel<Channel> by lazy {
        ViewModelProvider(
            this, ChannelListViewModel.Factory(channelType, getChannelUseCase)
        )[ChannelListViewModel::class.java]
    }

    override val baseListListener: BaseListRecyclerViewAdapter.IBaseList<Channel, ChannelViewHolder> by lazy { this }

    override fun createViewHolder(mView: View, viewType: Int): ChannelViewHolder {
        return ChannelViewHolder(mView)
    }

    override fun onClickItemListener(
        viewHolder: ChannelViewHolder,
        item: Channel,
        itemWrapper: PageableItemWrapper<Channel>
    ) {
        // TODO
    }

    override fun onBindViewHolder(
        item: Channel,
        viewHolder: ChannelViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        // TODO
    }

    override fun getItemLayout(): Int = R.layout.channel_item

    override val itemNameRes: Int = R.string.channels

    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Channel>
    ) {
        // TODO:
        viewModel.loadMoreItems()
    }

    class ChannelViewHolder(mView: View) : RecyclerView.ViewHolder(mView)

    private enum class BundleKey { ChannelType }

    class ChannelListViewModel(
        private val channelType: ChannelType,
        private val getChannelsUseCase: GetChannelsUseCase
    ) : BaseListViewModel<Channel>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Channel>?): PnutResponse<List<Channel>> {
            val params = GetChannelsParam().also { getChannelParams ->
                getChannelParams.add(GeneralChannelParam(includeRecentMessage = true))
                requestPager?.let { getChannelParams.add(PaginationParam.createFromPager(it)) }
            }
            val getChannelsOutputData =
                getChannelsUseCase.run(GetChannelsInputData(channelType, params))
            return getChannelsOutputData.channels
        }

        class Factory(
            private val channelType: ChannelType,
            private val getChannelsUseCase: GetChannelsUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChannelListViewModel(channelType, getChannelsUseCase) as T
            }
        }
    }

    companion object {
        fun privateChannels() = newInstance(ChannelType.Private)
        fun publicChannels() = newInstance(ChannelType.Public)
        private fun newInstance(channelType: ChannelType) = ChannelListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.ChannelType.name, channelType)
            }
        }
    }

}
