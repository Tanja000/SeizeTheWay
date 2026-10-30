package com.example.seizetheway.ui.main

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seizetheway.model.RouteDiscoveryItem
import com.example.seizetheway.model.WayCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private object ThumbnailMemoryCache {
    val cache = LruCache<String, ImageBitmap>(60)
}

@Composable
fun DiscoveryThumbnail(
    item: RouteDiscoveryItem,
    size: Dp = 84.dp,
    cornerRadius: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    var remoteBitmap by remember(item.id, item.imageUrl) {
        mutableStateOf(item.imageUrl?.let { ThumbnailMemoryCache.cache.get(it) })
    }

    LaunchedEffect(item.imageUrl) {
        val url = item.imageUrl
        if (!url.isNullOrBlank() && remoteBitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                try {
                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 4500
                        readTimeout = 4500
                        setRequestProperty("User-Agent", "SeizeTheWay-Android-App/1.0")
                    }
                    try {
                        if (conn.responseCode in 200..299) {
                            conn.inputStream.use { stream ->
                                BitmapFactory.decodeStream(stream)?.asImageBitmap()
                            }
                        } else null
                    } finally {
                        conn.disconnect()
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (loaded != null) {
                ThumbnailMemoryCache.cache.put(url, loaded)
                remoteBitmap = loaded
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // 1. Scenic Category Artwork Background (always rendered underneath or when no remote photo)
        ScenicCategoryIllustration(
            category = item.category,
            seed = item.title.hashCode(),
            modifier = Modifier.fillMaxSize()
        )

        // 2. Remote Photo if available (from Wikipedia / Wikimedia / Ticketmaster)
        remoteBitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle bottom gradient for badge legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x99000000)),
                            startY = 80f
                        )
                    )
            )
        }

        // 3. Top-left Category Mini Icon Badge
        Surface(
            color = Color(0xCC091324),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(5.dp)
                .size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = categoryIcon(item.category),
                    contentDescription = item.category.label,
                    tint = item.category.color,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 4. Bottom-right Detour Distance Pill inside Thumbnail
        Surface(
            color = Color(0xD9091324),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        ) {
            Text(
                text = "+${String.format("%.1f", item.detourDistanceKm)} km",
                color = Color(0xFF2CE5C6),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ScenicCategoryIllustration(
    category: WayCategory,
    seed: Int,
    modifier: Modifier = Modifier
) {
    val shift = ((seed % 7) + 7) % 7
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sky / Backdrop gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    category.secondaryColor,
                    category.color.copy(alpha = 0.85f),
                    Color(0xFF0F172A)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Glowing sun / moon orb
        val sunX = w * (0.68f + (shift - 3) * 0.03f)
        val sunY = h * 0.28f
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = w * 0.22f,
            center = Offset(sunX, sunY)
        )
        drawCircle(
            color = Color(0xFFFFF3C4).copy(alpha = 0.65f),
            radius = w * 0.12f,
            center = Offset(sunX, sunY)
        )

        when (category) {
            WayCategory.NATUR, WayCategory.WANDERUNGEN -> {
                // Mountain peaks & winding trail / lake waves
                val m1 = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.65f)
                    lineTo(w * 0.35f, h * 0.34f)
                    lineTo(w * 0.68f, h * 0.62f)
                    lineTo(w, h * 0.40f)
                    lineTo(w, h)
                    close()
                }
                drawPath(m1, color = Color(0xFF064E3B).copy(alpha = 0.72f))

                val trail = Path().apply {
                    moveTo(w * 0.25f, h)
                    quadraticTo(w * 0.58f, h * 0.75f, w * 0.42f, h * 0.56f)
                }
                drawPath(
                    trail,
                    color = Color(0xFFFDE68A).copy(alpha = 0.8f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            WayCategory.HISTORISCHES -> {
                // Castle silhouette & towers on hill
                val hill = Path().apply {
                    moveTo(0f, h)
                    quadraticTo(w * 0.5f, h * 0.55f, w, h)
                    close()
                }
                drawPath(hill, color = Color(0xFF451A03).copy(alpha = 0.75f))
                drawRect(
                    color = Color(0xFFFEF3C7).copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.32f, h * 0.38f),
                    size = Size(w * 0.14f, h * 0.32f)
                )
                drawRect(
                    color = Color(0xFFFDE68A).copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.50f, h * 0.30f),
                    size = Size(w * 0.16f, h * 0.40f)
                )
            }

            WayCategory.CAFES, WayCategory.RESTAURANTS -> {
                // Warm table / cup silhouette & steam waves
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.24f),
                    topLeft = Offset(w * 0.28f, h * 0.46f),
                    size = Size(w * 0.44f, h * 0.30f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(w * 0.42f, h * 0.38f),
                    end = Offset(w * 0.45f, h * 0.24f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(w * 0.56f, h * 0.38f),
                    end = Offset(w * 0.59f, h * 0.24f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            WayCategory.TAGESEVENTS, WayCategory.ERLEBNIS -> {
                // Stage spotlights / Ferris wheel arcs
                drawCircle(
                    color = Color.White.copy(alpha = 0.30f),
                    radius = w * 0.28f,
                    center = Offset(w * 0.5f, h * 0.55f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = Color(0xFFFDE047).copy(alpha = 0.45f),
                    start = Offset(w * 0.2f, h),
                    end = Offset(w * 0.45f, h * 0.2f),
                    strokeWidth = 4.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF2CE5C6).copy(alpha = 0.45f),
                    start = Offset(w * 0.8f, h),
                    end = Offset(w * 0.55f, h * 0.2f),
                    strokeWidth = 4.dp.toPx()
                )
            }

            WayCategory.KULTURELLES, WayCategory.KOSTENLOSES -> {
                // Classical columns / Horizon waves
                val roof = Path().apply {
                    moveTo(w * 0.22f, h * 0.44f)
                    lineTo(w * 0.50f, h * 0.26f)
                    lineTo(w * 0.78f, h * 0.44f)
                    close()
                }
                drawPath(roof, color = Color.White.copy(alpha = 0.32f))
                for (c in 0..2) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.28f),
                        topLeft = Offset(w * (0.30f + c * 0.16f), h * 0.46f),
                        size = Size(w * 0.07f, h * 0.28f)
                    )
                }
            }
        }
    }
}
