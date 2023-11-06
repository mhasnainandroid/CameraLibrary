package com.elexoft.camera_mylibrary

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
internal fun CameraXImpl(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Bitmap) -> Unit = {},
    onVideoCaptured: (file: File) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    var recording by remember {
        mutableStateOf<Recording?>(null)
    }
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }
    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Permission not granted")
            }
        },
        permissionsNotAvailableContent = { /* ... */ }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        controller.cameraSelector =
                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Camera switch"
                    )
                }

                IconButton(
                    onClick = {
                        takePhoto(
                            controller = controller,
                            onPhotoTaken = {
                                onPhotoTaken(it)
                            }, context = context
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo"
                    )

                }
                if (recording != null) {
                    IconButton(onClick = {
                        recording?.stop()
                        recording = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop recording"
                        )
                    }
                } else {
                    IconButton(onClick = {
                        Toast.makeText(context, "Recording started", Toast.LENGTH_LONG).show()
                        val outputFile = File(context.filesDir, "my-recording.mp4")
                        recording = controller.startRecording(
                            FileOutputOptions.Builder(outputFile).build(),
                            AudioConfig.create(true),
                            ContextCompat.getMainExecutor(context),
                        ) { event ->
                            when (event) {
                                is VideoRecordEvent.Finalize -> {
                                    if (event.hasError()) {
                                        recording?.stop()
                                        recording = null
                                        Toast.makeText(
                                            context,
                                            "Video captured failed",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    } else {
                                        onVideoCaptured(outputFile)
                                        Toast.makeText(
                                            context,
                                            "Video captured successfully in ${outputFile.absolutePath}",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Record Video"
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Could not take photo", exception)
                Toast.makeText(context, exception.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    )
}