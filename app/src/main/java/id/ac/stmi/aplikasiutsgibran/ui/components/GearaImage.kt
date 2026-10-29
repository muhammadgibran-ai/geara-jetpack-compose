package id.ac.stmi.aplikasiutsgibran.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import id.ac.stmi.aplikasiutsgibran.R
import id.ac.stmi.aplikasiutsgibran.ui.theme.GearaOrange

/**
 * Komponen pemuat gambar asinkron dengan caching Coil (Memory & Disk Cache).
 * Mengambil gambar dari Cloud Storage (AWS S3 / Firebase Storage) via URL Retrofit.
 * Menyediakan fallback jika offline atau URL gagal dimuat untuk mencegah pembengkakan ukuran APK.
 */
@Composable
fun GearaImage(
    imageUrl: String?,
    @DrawableRes fallbackRes: Int = R.drawable.ic_geara_logo,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    if (imageUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(id = if (fallbackRes != 0) fallbackRes else R.drawable.ic_geara_logo),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .diskCacheKey(imageUrl)
                .memoryCacheKey(imageUrl)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (fallbackRes != 0) {
                        Image(
                            painter = painterResource(id = fallbackRes),
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale,
                            alpha = 0.5f
                        )
                    } else {
                        CircularProgressIndicator(
                            color = GearaOrange,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            error = {
                Image(
                    painter = painterResource(id = if (fallbackRes != 0) fallbackRes else R.drawable.ic_geara_logo),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        )
    }
}
