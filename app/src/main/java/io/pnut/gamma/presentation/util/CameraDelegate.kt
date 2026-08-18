package io.pnut.gamma.presentation.util

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class CameraDelegate(
    private val fragment: Fragment,
    private val onPhotoTaken: (Uri) -> Unit
) {
    private var latestPhotoUri: Uri? = null

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                latestPhotoUri?.let { uri ->
                    onPhotoTaken(uri)
                }
            }
        }

    fun takePhoto() {
        val context = fragment.requireContext()
        val photoFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "io.pnut.gamma.fileprovider",
            photoFile
        )
        latestPhotoUri = uri
        takePictureLauncher.launch(uri)
    }
}
