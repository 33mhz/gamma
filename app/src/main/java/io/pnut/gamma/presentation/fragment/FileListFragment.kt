package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FilesItemBinding
import io.pnut.gamma.domain.entity.File
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.GetFilesInputData
import io.pnut.gamma.domain.model.params.composed.GetFilesParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.GetFilesUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.util.toFormatString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FileListFragment : BaseListFragment<File, FileListFragment.FileViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<File, FileListFragment.FileViewHolder> {
    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<File>
    ) {
        viewModel.loadMoreItems()
    }
    override val itemNameRes: Int = R.string.files
    @Inject
    lateinit var getFilesUseCase: GetFilesUseCase

    override lateinit var viewModel: BaseListViewModel<File>
    override val baseListListener: BaseListRecyclerViewAdapter.IBaseList<File, FileViewHolder> = this
    override val dividerDrawable: Int = R.drawable.divider_full_bleed

    override fun createViewHolder(mView: View, viewType: Int): FileViewHolder = FileViewHolder(mView)

    override fun onClickItemListener(
        viewHolder: FileViewHolder,
        item: File,
        itemWrapper: PageableItemWrapper<File>
    ) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, FilesViewModel.Factory(getFilesUseCase))[FilesViewModel::class.java]

        super.onCreate(savedInstanceState)
    }

    override fun onBindViewHolder(item: File, viewHolder: FileViewHolder, position: Int, isMainItem: Boolean) {
        viewHolder.binding.filesItemTitleTextView.text = item.name
        viewHolder.binding.filesItemDateTextView.text = item.createdAt.toFormatString(context)
        viewHolder.binding.filesItemSubTitleTextView.text = item.mimeType
    }

    override fun getItemLayout(): Int = R.layout.files_item

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = FilesItemBinding.bind(itemView)
    }

    class FilesViewModel(private val getFilesUseCase: GetFilesUseCase) : BaseListViewModel<File>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<File>?): PnutResponse<List<File>> {
            val getPostParam =
                GetFilesParam().apply { requestPager?.let { add(PaginationParam.createFromPager(it)) } }
//                .also { it.add(params) }

            return getFilesUseCase.run(GetFilesInputData(getPostParam)).res
        }

        override fun storeItems() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        class Factory(private val getFilesUseCase: GetFilesUseCase) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FilesViewModel(getFilesUseCase) as T
            }


        }
    }

    companion object {
        fun newInstance() = FileListFragment()
    }
}
