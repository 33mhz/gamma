package io.pnut.gamma.presentation.fragment


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.os.BundleCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentPhotoViewItemBinding
import io.pnut.gamma.domain.model.ThumbAndFull
import com.bumptech.glide.Glide
import androidx.core.graphics.createBitmap


class PhotoViewItemFragment : Fragment() {

    private val path by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.Path.name, ThumbAndFull::class.java) }
            ?: throw NullPointerException("Must set path")
    }
    private val isSharedElementTarget by lazy {
        requireArguments().getBoolean(BundleKey.SharedElementTarget.name, false)
    }

    private var _binding: FragmentPhotoViewItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoViewItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progress = CircularProgressDrawable(view.context).apply {
            val gray = view.context.getColor(R.color.colorGrayLighter)
            setTint(gray)
            start()
            centerRadius = 30f
        }
        Glide.with(view).load(path.full).placeholder(progress)
            .transition(DrawableTransitionOptions.withCrossFade(0))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    if (isSharedElementTarget)
                        requireActivity().startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    val width = resource.intrinsicWidth
                    val height = resource.intrinsicWidth
                    val ratio = height.toFloat() / width.toFloat()
                    val resizedWidth = 100
                    val resizedHeight = (100 * ratio).toInt()
                    val bmp =
                        createBitmap(resizedWidth, resizedHeight)
                    val canvas = Canvas(bmp)
                    resource.setBounds(0, 0, canvas.width, canvas.height)
                    resource.draw(canvas)
                    val palette = Palette.from(bmp).generate()
                    val color = palette.mutedSwatch?.rgb ?: Color.BLACK
                    binding.photoViewWrapper.setBackgroundColor(color)
                    if (isSharedElementTarget)
                        requireActivity().startPostponedEnterTransition()
                    return false
                }
            }).into(binding.photoView)
    }

    private enum class BundleKey { Path, SharedElementTarget }

    companion object {
        fun newInstance(thumbAndFull: ThumbAndFull, isSharedElementTarget: Boolean) =
            PhotoViewItemFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleKey.Path.name, thumbAndFull)
                    putBoolean(BundleKey.SharedElementTarget.name, isSharedElementTarget)
                }
            }
    }
}
