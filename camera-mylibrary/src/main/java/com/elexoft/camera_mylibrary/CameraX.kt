package com.elexoft.camera_mylibrary

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.File

@Composable
fun CameraX(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Bitmap) -> Unit = {},
    onVideoCaptured: (file: File) -> Unit = {}
) {
    CameraXImpl(modifier = modifier, onPhotoTaken = onPhotoTaken, onVideoCaptured = onVideoCaptured)
}