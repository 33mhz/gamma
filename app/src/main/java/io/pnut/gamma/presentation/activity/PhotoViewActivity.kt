package io.pnut.gamma.presentation.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.SharedElementCallback
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.pnut.gamma.domain.model.ThumbAndFull
import io.pnut.gamma.presentation.fragment.PhotoViewItemFragment
import io.pnut.gamma.R
import androidx.core.view.isVisible
import io.pnut.gamma.databinding.ActivityPhotoViewBinding
import io.pnut.gamma.util.AppUtil


class PhotoViewActivity : BaseActivity() {
    private enum class IntentKey { Photos, Index, SharedElementId, Radius, TransitionName }

    companion object {

        fun newIntent(context: Context, url: String, sharedElementId: Int): Intent {
            val items = listOf(ThumbAndFull(url, url))

            return Intent(context, PhotoViewActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.Photos.name, ArrayList(items))
                putExtra(IntentKey.SharedElementId.name, sharedElementId)
            }
        }

        fun startActivity(
            activity: Activity?,
            item: String,
            imageView: ImageView,
            radius: Float = 0f,
            transitionName: String = ""
        ) = startActivity(
            activity,
            ThumbAndFull(item, item),
            imageView,
            radius = radius,
            transitionName = transitionName
        )

        fun startActivity(
            activity: Activity?,
            item: ThumbAndFull,
            imageView: ImageView,
            transitionName: String = "",
            radius: Float = 0f
        ) {
            val intent = photoViewInstance(
                activity,
                listOf(item),
                radius = radius,
                transitionName = transitionName
            )
            val options = ActivityOptions.makeSceneTransitionAnimation(
                activity,
                Pair.create(imageView, transitionName)
            )
            activity?.startActivity(intent, options.toBundle())
        }

        fun photoViewInstance(
            context: Context?,
            items: List<ThumbAndFull>,
            index: Int = 0,
            radius: Float = 0f,
            transitionName: String = ""
        ) =
            Intent(context, PhotoViewActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKey.Photos.name, ArrayList(items))
                putExtra(IntentKey.Index.name, index)
                putExtra(IntentKey.Radius.name, radius)
                putExtra(IntentKey.TransitionName.name, transitionName)
            }

    }

    private val photos by lazy {
        IntentCompat.getParcelableArrayListExtra(intent, IntentKey.Photos.name, ThumbAndFull::class.java)
    }

    private val index by lazy {
        intent.getIntExtra(IntentKey.Index.name, 0)
    }
    private val adapter by lazy {
        MediaViewPager(this, photos.orEmpty(), index)
    }

    private lateinit var binding: ActivityPhotoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupAnimation()
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportPostponeEnterTransition()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.mediaViewPager.adapter = adapter
        binding.mediaViewPager.setCurrentItem(index, false)
        TabLayoutMediator(binding.mediaviewPagerIndicator, binding.mediaViewPager) { _, _ -> }.attach()

        binding.prevButton.setOnClickListener {
            binding.mediaViewPager.currentItem -= 1
        }

        binding.nextButton.setOnClickListener {
            binding.mediaViewPager.currentItem += 1
        }

        binding.mediaViewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateNavigationButtons(position)
            }
        })
        updateNavigationButtons(index)

        binding.haulerView.setOnDragDismissedListener {
            finishAfterTransition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.photo_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_browser -> {
                val url = photos?.get(binding.mediaViewPager.currentItem)?.full ?: return true
                AppUtil.openUrl(this, url)
                return true
            }
            R.id.menu_copy_url -> {
                val url = photos?.get(binding.mediaViewPager.currentItem)?.full ?: return true
                AppUtil.copyToClipboard(this, url)
                Toast.makeText(this, R.string.copy_url, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun toggleUI() {
        binding.toolbar.isVisible = !binding.toolbar.isVisible
        binding.mediaviewPagerIndicator.isVisible = !binding.mediaviewPagerIndicator.isVisible
        updateNavigationButtons(binding.mediaViewPager.currentItem)
    }

    private fun updateNavigationButtons(position: Int) {
        val total = adapter.itemCount
        val uiVisible = binding.toolbar.isVisible
        binding.prevButton.isVisible = uiVisible && position > 0
        binding.nextButton.isVisible = uiVisible && position < total - 1
    }

    private fun setupAnimation() {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                super.onMapSharedElements(names, sharedElements)
                val view = adapter.getItem(binding.mediaViewPager.currentItem).requireView()
                    .findViewById<View>(R.id.photoView)
                sharedElements[names[0]] = view
            }
        })
    }

    class MediaViewPager(activity: PhotoViewActivity, items: List<ThumbAndFull>, index: Int = 0) :
        FragmentStateAdapter(activity) {
        private val fragments =
            items.mapIndexed { i, it -> PhotoViewItemFragment.newInstance(it, i == index) }

        override fun createFragment(position: Int): Fragment = fragments[position]
        override fun getItemCount(): Int = fragments.size

        fun getItem(position: Int): Fragment = fragments[position]
    }
}
