package com.lexawall.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lexawall.R
import com.lexawall.core.utils.DevicePerformance

@Composable
fun LexaMascotAnimated(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val context = LocalContext.current
    if (DevicePerformance.isLowEndDevice(context)) {
        Image(
            painter = painterResource(id = R.drawable.ic_lexa_hummingbird),
            contentDescription = "Lexa mascot",
            modifier = modifier.then(Modifier.size(size)),
            contentScale = ContentScale.Fit
        )
        return
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lexa_hover.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.then(Modifier.size(size)),
        contentScale = ContentScale.Fit
    )
}
