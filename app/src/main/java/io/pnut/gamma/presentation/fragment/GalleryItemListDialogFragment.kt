package io.pnut.gamma.presentation.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import androidx.core.os.BundleCompat
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentResolverCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.Glide
import io.pnut.gamma.R
import com.google.android.material.R as Rm
import io.pnut.gamma.util.Constants
import java.io.File


class GalleryItemListDialogFragment : BaseBottomSheetDialogFragment() {
    private var currentPhotoPath: Uri? = null
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery_item_list_dialog, container, false)
    }

    private val galleryItemList: ArrayList<GalleryItem> = ArrayList()
    private val mode by lazy {
        arguments?.let { BundleCompat.getSerializable(it, BundleKey.Mode.name, Mode::class.java) } ?: Mode.Single
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GalleryItemAdapter()
        val pictureList = view.findViewById<RecyclerView>(R.id.pictureList)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        Thread {
            galleryItemList.addAll(getImages())
            pictureList.post { adapter.submitList(ArrayList(galleryItemList)) }
        }.start()
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.setOnMenuItemClickListener(::onMenuItemClick)
        pictureList.adapter = adapter

        dialog?.window
            ?.decorView
            ?.findViewById<View>(Rm.id.touch_outside)
            ?.setOnClickListener { dismiss() }
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCamera -> openCamera()
            R.id.menuLibrary -> openLibrary()
            else -> return false

        }
        return true
    }

    private fun createImageFile(): File? {
        val storageDir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.GAMMA)
        storageDir.mkdirs()
        val file = File.createTempFile(
            System.currentTimeMillis().toString(),
            ".jpg",
            storageDir
        )
        currentPhotoPath = Uri.fromFile(file)
        return file
    }

    private fun galleryAddPic() {
        val photoPath = currentPhotoPath ?: return
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(photoPath.path ?: "")
            mediaScanIntent.data = Uri.fromFile(f)
            context?.sendBroadcast(mediaScanIntent)
        }
    }

    enum class RequestCode { TakePhoto, Library }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            galleryAddPic()
            currentPhotoPath?.let {
                listener?.onGalleryItemClicked(it, tag)
                dismiss()
            }
        }
    }

    private val libraryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            listener?.onGalleryItemClicked(uri, tag)
            dismiss()
        }
    }

    // https://developer.android.com/training/camera/photobasics
    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                it
            )
            takePhotoLauncher.launch(photoURI)
        }
    }

    private fun openLibrary() {
        libraryLauncher.launch("image/*")
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (parentFragment ?: context) as Listener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun onGalleryItemClicked(uri: Uri, tag: String?)
        fun onShow()
        fun onDismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.let {
            it.setOnShowListener {
                listener?.onShow()
            }
            it.setOnDismissListener { listener?.onDismiss() }
            it.setOnCancelListener { listener?.onDismiss() }
        }
        return dialog
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDismiss()
    }

    private fun getImages(): ArrayList<GalleryItem> {
        val res = ArrayList<GalleryItem>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val order = "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"
        val resolver = context?.contentResolver ?: return res
        val cursor = ContentResolverCompat.query(resolver, uri, projection, null, null, order, null as CancellationSignal?) ?: return res
        while (cursor.moveToNext()) {
            val pathIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            val path = cursor.getString(pathIdx)
            if (path != null) {
                res.add(GalleryItem(Uri.fromFile(File(path))))
            }
        }
        cursor.close()
        return res
    }

    data class GalleryItem(val uri: Uri)

    private object GalleryItemDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }

    private inner class GalleryItemAdapter :
        ListAdapter<GalleryItem, GalleryItemAdapter.ViewHolder>(GalleryItemDiffCallback) {

        inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_gallery_item_list_dialog_item, parent, false)) {

            val imageView: ImageView = itemView.findViewById(R.id.imageView)

            init {
                imageView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener?.onGalleryItemClicked(getItem(position).uri, tag)
                        dismiss()
                    }
                }
            }
        }

        private inner class ErrorHandling(
            val item: GalleryItem
        ) : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                galleryItemList.remove(item)
                submitList(ArrayList(galleryItemList))
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            Glide.with(holder.itemView)
                .load(item.uri)
                .thumbnail(.1f)
                .listener(ErrorHandling(item))
                .into(holder.imageView)
        }
    }

    private enum class Mode { Single, Multiple }
    private enum class BundleKey { Mode }
    companion object {
        fun chooseMultiple() = GalleryItemListDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.Mode.name, Mode.Multiple)
            }
        }

        fun chooseSingle() = GalleryItemListDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.Mode.name, Mode.Single)
            }
        }
    }
}
