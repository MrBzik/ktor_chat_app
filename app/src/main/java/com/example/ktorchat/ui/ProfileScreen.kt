package com.example.ktorchat.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.ktorchat.R
import com.example.ktorchat.presentation.profile.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


const val MIN_THRESH_HOLD = 50

const val BTN_SIZE = 25

const val PROFILE_PICTURE = "profile_pic.png"

@Composable
@Destination
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val profileImageUri by profileViewModel.bitmapUri.collectAsState()

    LaunchedEffect(Unit){

        Log.d("CHECKTAGS", profileImageUri.toString())

        if(profileImageUri == null){
            val localPath = context.filesDir.absolutePath + PROFILE_PICTURE
            val file = File(localPath)
            profileViewModel.initialGetBitmapUri(if (file.exists()) Uri.parse(localPath) else null)
        }
    }


//    var selectedImageUri by remember {
//        mutableStateOf<Uri?>(null)
//    }

    var originalBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

//    var croppedBitmap by remember {
//        mutableStateOf<Bitmap?>(null)
//    }





    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {

            result.uriContent?.let { uri ->

                profileViewModel.onImageCropped(uri)

            }

        } else {
            Log.d("CHECKTAGS", result.error?.stackTraceToString() ?: "")
        }
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            val cropOptions = CropImageContractOptions(uri, CropImageOptions().apply {
                fixAspectRatio = true
                aspectRatioX = 1
                aspectRatioY = 1
                outputCompressFormat = Bitmap.CompressFormat.PNG
                outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE
                outputRequestWidth = 240
                outputRequestHeight = 240

            })
            imageCropLauncher.launch(cropOptions)
        }



//    val resultState = remember {
//        mutableStateOf<Long>(10)
//    }
//
//    var imageLoaded by remember {
//        mutableStateOf(false)
//    }





    Column(horizontalAlignment = Alignment.CenterHorizontally) {


//            Button(onClick = {resultState.value = System.currentTimeMillis()}) {
//                Text(text = "Apply")
//            }

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {


            AsyncImage(
                model = profileImageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .clickable {
                        pickImageLauncher
                            .launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia.ImageOnly
                                )
                            )
                    }
                ,
                placeholder = painterResource(R.drawable.ic_add_profile_photo),
                fallback = painterResource(R.drawable.ic_add_profile_photo),
                error = painterResource(R.drawable.ic_add_profile_photo),
                onSuccess = {

                    (it.result.drawable as BitmapDrawable).bitmap?.let {bitmap ->
                        profileViewModel.updateBitmap(bitmap)
                        if(profileViewModel.isToBackUpProfilePic)
                            profileViewModel.savePicToFile()
                    }


//                     originalBitmap?.let {
//                        imageLoaded = true
//                     }
                }
            )






//            if(imageLoaded)
//
//                TransformableDemo(
//                    widthBoundary = originalBitmap?.width ?: 0,
//                    heightBoundary = originalBitmap?.height ?: 0,
//                    resultsState = resultState){ dimen ->
//
//                    originalBitmap?.let {bitmap ->
//
//                        val offsetX = if(dimen.offsetX < 0) 0
//                        else if(dimen.offsetX + dimen.size > bitmap.width) bitmap.width - dimen.size
//                        else dimen.offsetX
//
//                        val offsetY = if(dimen.offsetY < 0) 0
//                        else if(dimen.offsetY + dimen.size > bitmap.height) bitmap.height - dimen.size
//                        else dimen.offsetY
//
//
//                        croppedBitmap = Bitmap.createBitmap(
//                            bitmap,
//                            offsetX,
//                            offsetY,
//                            dimen.size,
//                            dimen.size
//                        )
//                    }
//                }




        }



//        Spacer(modifier = Modifier.height(32.dp))
//
//        Text(text = chatViewModel.userName)
//
//        Spacer(modifier = Modifier.height(32.dp))


    }

}



data class ResultDimen(
    val offsetX : Int,
    val offsetY : Int,
    val size : Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformableDemo(
    widthBoundary : Int,
    heightBoundary : Int,
    resultsState : MutableState<Long>,
    getResults : (ResultDimen) -> Unit) {



    val density = LocalDensity.current.density

    val maxWidth by remember {
        mutableStateOf((widthBoundary / density).dp)
    }

    val maxHeight by remember {
        mutableStateOf((heightBoundary / density).dp)
    }



    var size by remember { mutableStateOf(250f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var scale by remember { mutableStateOf(1f) }
//    var scaleY by remember { mutableStateOf(1f) }
//
//    var leftTopBtnX by remember { mutableStateOf(0f) }
//    var leftTopBtnY by remember { mutableStateOf(0f) }
//
//    var leftBottomBtnX by remember { mutableStateOf(0f) }
//    var leftBottomBtnY by remember { mutableStateOf((250f - BTN_SIZE) * density) }
//
//    var rightTopBtnX by remember { mutableStateOf((250f - BTN_SIZE) * density) }
//    var rightTopBtnY by remember { mutableStateOf(0f) }
//
//    var rightBottomBtnX by remember { mutableStateOf((250f - BTN_SIZE) * density) }
//    var rightBottomBtnY by remember { mutableStateOf((250f - BTN_SIZE) * density) }




    LaunchedEffect(resultsState.value){
        Log.d("CHECKTAGS", "size is : ${size * density}, x: $offsetX, y: $offsetY")
        getResults(
            ResultDimen(
            offsetX = offsetX.roundToInt(),
            offsetY = offsetY.roundToInt(),
            size = (size * density).roundToInt()
        )
        )
    }


    val state = rememberTransformableState {
            _, offsetChange, _ ->

        val xChange = offsetChange.x
        val yChange = offsetChange.y

        offsetX += xChange
        offsetY += yChange


//        leftTopBtnX += xChange
//        leftBottomBtnX += xChange
//        rightBottomBtnX += xChange



//        leftTopBtnY += yChange
//        leftBottomBtnY += yChange
//        rightBottomBtnY += yChange


//        if (xChange < 0 && offsetX < 0){
//            offsetX = 0f
//        } else if(xChange > 0 && offsetX + (density * size) > widthBoundary){
//            offsetX = (density * size) + widthBoundary
//            }
//        else
//         {
//             offsetX += xChange
//        }
//
//        if (yChange < 0 && offsetY < 0){
//            offsetY = 0f
//        } else if(yChange >0 && offsetY + (density * size) > heightBoundary){
//            offsetY = size * density + heightBoundary
//        }
//        else
//         {
//             offsetY += yChange
//        }

    }

    ModalDrawerSheet(modifier = Modifier.fillMaxSize(),
        drawerContainerColor = Color(0x66000000),


    ) {
        Box(modifier = Modifier.fillMaxSize()) {


            Box(
                modifier = Modifier
                    .graphicsLayer(
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .transformable(state = state)
                    .background(Color(0f, 0.4f, 0f, 0.3f))
                    .size(size.dp)

            ) {

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dragBy = if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
                                    dragAmount.x else dragAmount.y




                                if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy < 0) {

                                    val byX = offsetX + dragBy
                                    val byY = offsetY + dragBy

                                    if(byX > 0 && byY > 0){

                                        offsetX = byX
                                        offsetY = byY

                                        size -= dragBy.toDp().value

                                    }
                                }
                            }
                        }
                )

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->

                                change.consume()

                                var byX = true

                                val dragBy =
                                    if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
                                        dragAmount.x
                                    else {
                                        byX = false
                                        dragAmount.y
                                    }

                                if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy < 0 && byX ||
                                    size < MIN_THRESH_HOLD && dragBy > 0 && !byX
                                ) {
                                    if (byX) {


                                        offsetX += dragBy
                                        size -= dragBy.toDp().value
                                    } else {
                                        offsetX -= dragBy
                                        size += dragBy.toDp().value
                                    }
                                }
                            }
                        }
                )

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .pointerInput(Unit) {

                            detectDragGestures { change, dragAmount ->

                                change.consume()

                                val dragBy =
                                    if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
                                        dragAmount.x else dragAmount.y

                                if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy > 0) {

                                    size += dragBy.toDp().value

                                }
                            }
                        }
                )
            }
        }
    }




}



@Composable
fun DragAndScaleBox(){
    //                Box(
//            modifier = Modifier
//                .graphicsLayer(
//                    scaleX = scale,
//                    scaleY = scale,
//                    translationX = offsetX,
//                    translationY = offsetY,
//
//                )
//                .background(Color.Blue)
//                .size(size.dp)
//                .pointerInput(Unit){
//                    detectDragGestures { change, dragAmount ->
//                        offsetX += dragAmount.x
//                        offsetY += dragAmount.y
//                    }

//                    detectTransformGestures { _, pan, zoom, _ ->
//
//                        Log.d("CHECKTAGS", "zoom : $zoom, pamX: ${pan.x}, panY: ${pan.y}")
//
//                        size *= zoom
//
//                        if(zoom == 1f){
//                            offsetX += pan.x * scale
//                            offsetY += pan.y * scale
//
//                        }
//
//                        if(offsetX < 0) offsetX = 0f
//                        if(offsetY < 0) offsetY = 0f
//
//                    }
//                }
//        )

}

//        Box(
//            Modifier
//                .offset {
//                    IntOffset(
//                        leftTopBtnX
//                            .roundToInt()
//                            .coerceIn(0, (widthBoundary - size * density).roundToInt()),
//                        leftTopBtnY
//                            .roundToInt()
//                            .coerceIn(0, (heightBoundary - size * density).roundToInt())
//                    )
//                }
//                .background(Color.Red)
//                .size(25.dp)
//                .pointerInput(Unit) {
//                    detectDragGestures { change, dragAmount ->
//
//                        change.consume()
//
//                        val dragBy = if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
//                            dragAmount.x else dragAmount.y
//
//                        if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy < 0) {
//
//                            leftTopBtnX += dragBy
//                            leftTopBtnY += dragBy
//
//                            leftBottomBtnX += dragBy
//
//
//                            offsetX += dragBy
//                            offsetY += dragBy
//
//                            size -= dragBy.toDp().value
//
//                        }
//                    }
//                }
//        )


        // OLD BOXED BUTTONS


//
//        Box(
//            Modifier
//                .offset {
//                    IntOffset(
//                        leftBottomBtnX
//                            .roundToInt()
//                            .coerceIn(0, (widthBoundary - size * density).roundToInt()),
//                        leftBottomBtnY
//                            .roundToInt()
//                            .coerceIn(
//                                ((size - 25) * density).roundToInt(),
//                                (heightBoundary - 25 * density).roundToInt()
//                            )
//                    )
//                }
//                .background(Color.Red)
//                .size(25.dp)
//                .pointerInput(Unit) {
//
//                    detectDragGestures { change, dragAmount ->
//
//                        change.consume()
//
//                        var byX = true
//
//                        val dragBy = if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
//                            dragAmount.x
//                        else {
//                            byX = false
//                            dragAmount.y
//                        }
//
//                        if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy < 0 && byX ||
//                            size < MIN_THRESH_HOLD && dragBy > 0 && !byX
//                        ) {
//                            if (byX) {
//                                leftBottomBtnX += dragBy
//                                leftBottomBtnY -= dragBy
//                                leftTopBtnX += dragBy
//                                rightBottomBtnY -= dragBy
//                                offsetX += dragBy
//                                size -= dragBy.toDp().value
//                            } else {
//                                leftBottomBtnX -= dragBy
//                                leftBottomBtnY += dragBy
//                                leftTopBtnX -= dragBy
//                                rightBottomBtnY += dragBy
//                                offsetX -= dragBy
//                                size += dragBy.toDp().value
//                            }
//                        }
//                    }
//                }
//        )



//        Box(
//            Modifier
//                .offset {
//                    IntOffset(
//                        rightBottomBtnX
//                            .roundToInt()
//                            .coerceIn(
//                                ((size - 25) * density).roundToInt(),
//                                widthBoundary - (25 * density).roundToInt()
//                            ),
//                        rightBottomBtnY
//                            .roundToInt()
//                            .coerceIn(
//                                ((size - 25) * density).roundToInt(),
//                                heightBoundary - (25 * density).roundToInt()
//                            )
//                    )
//                }
//                .background(Color.Red)
//                .size(25.dp)
//                .pointerInput(Unit) {
//
//                    detectDragGestures { change, dragAmount ->
//
//                        change.consume()
//
//                        val dragBy = if (dragAmount.x.absoluteValue > dragAmount.y.absoluteValue)
//                            dragAmount.x else dragAmount.y
//
//                        if (size > MIN_THRESH_HOLD || size < MIN_THRESH_HOLD && dragBy > 0) {
//
//                            rightBottomBtnX += dragBy
//                            rightBottomBtnY += dragBy
//
//                            leftBottomBtnY += dragBy
//
//                            size += dragBy.toDp().value
//
//                        }
//                    }
//                }
//        )
