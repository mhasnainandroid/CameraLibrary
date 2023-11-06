package com.elexoft.cameralibrary
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elexoft.camera_mylibrary.CameraX
import com.elexoft.cameralibrary.ui.theme.CameraLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (!hasRequiredPermission()) {
//            ActivityCompat.requestPermissions(this, CAMERA_X_PERMISSION, 0)
//        }
        setContent {
            CameraLibraryTheme {
                CameraX(
                    onPhotoTaken = {},
                    onVideoCaptured = {}
                )
            }
        }
    }

//    private fun hasRequiredPermission(): Boolean {
//        return CAMERA_X_PERMISSION.all {
//            ContextCompat.checkSelfPermission(
//                applicationContext,
//                it
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    companion object {
//        private val CAMERA_X_PERMISSION = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
//        )
//    }
}