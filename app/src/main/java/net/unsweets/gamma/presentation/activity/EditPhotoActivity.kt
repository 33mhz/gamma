package net.unsweets.gamma.presentation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.canhub.cropper.CropImageView
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ActivityEditPhotoBinding
import java.io.File


class EditPhotoActivity : BaseActivity() {

    private val uri: Uri by lazy {
        intent.getParcelableExtra<Uri>(IntentKey.Uri.name) ?: throw IllegalArgumentException("Must set Uri")
    }
    private val index by lazy {
        intent.getIntExtra(IntentKey.Index.name, -1)
    }

    private lateinit var binding: ActivityEditPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (mode == Mode.Square) {
            binding.cropImageView.setAspectRatio(1, 1)
            binding.cropImageView.setFixedAspectRatio(true)
        }
        binding.cropImageView.setImageUriAsync(uri)
        binding.cropImageView.setOnCropImageCompleteListener { _, result -> cropped(result) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuCrop -> {
                requestToCrop()
                true
            }
            R.id.menuRotateRight -> {
                rotate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun rotate() {
        binding.cropImageView.rotatedDegrees = binding.cropImageView.rotatedDegrees + 90
    }

    private fun requestToCrop() {
        val ext = File(uri.path ?: "").extension
        val outputUri = Uri.fromFile(File(externalCacheDir, "${System.currentTimeMillis()}.$ext"))
        binding.cropImageView.croppedImageAsync(customOutputUri = outputUri)
    }

    private fun cropped(result: CropImageView.CropResult) {
        val data = Intent().apply {
            putExtra(IntentKey.Index.name, index)
            putExtra(IntentKey.Uri.name, result.uriContent)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private val mode by lazy {
        @Suppress("DEPRECATION")
        intent.getSerializableExtra(IntentKey.Mode.name) as? Mode ?: Mode.Free
    }

    private enum class IntentKey { Uri, Index, Mode }
    enum class Mode { Free, Square }
    data class EditPhotoResult(val uri: Uri, val index: Int)
    companion object {
        fun newIntentSquareMode(context: Context?, uri: Uri) = newIntent(context, uri).apply {
            putExtra(IntentKey.Mode.name, Mode.Square)
        }

        fun newIntent(context: Context?, uri: Uri, index: Int = -1) =
            Intent(context, EditPhotoActivity::class.java).apply {
            putExtra(IntentKey.Uri.name, uri)
            putExtra(IntentKey.Index.name, index)
        }
        fun parseIntent(intent: Intent): EditPhotoResult? {
            val uri = intent.getParcelableExtra<Uri>(IntentKey.Uri.name) ?: return null
            val index = intent.getIntExtra(IntentKey.Index.name, -1)
            return EditPhotoResult(uri, index)
        }
    }
}
