package com.example.seizetheway.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.seizetheway.model.*
import com.example.seizetheway.data.*
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Dedicated Map Tab Content:
 * - Includes the view mode switcher between "Im Track" (only planned/saved stops & events) and "Alle" (all corridor results)!
 * - Shows active Track Metrics: Distanz (km), Höhenmeter (↑/↓ Hm), Reisedauer & Track-Typ
 * - Shows modified Track Polyline that routes directly through all selected stops
 * - Prominent button to transfer the track with all stops to Google Maps
 * - Category Icons on every point on the map; clicking any icon scrolls to its description below!
 */
@Composable
fun DedicatedMapTabScreen(
    route: RouteResult?,
    items: List<RouteDiscoveryItem>,
    allCorridorCount: Int,
    inTrackCount: Int,
    mapDisplayFilter: MapDisplayFilter,
    onMapDisplayFilterChange: (MapDisplayFilter) -> Unit,
    plannedStopIds: Set<String>,
    highlightedItemId: String?,
    maxDetourKm: Float,
    isRecalculatingTrack: Boolean,
    isAlreadySaved: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.DE,
    onSelectItem: (RouteDiscoveryItem) -> Unit,
    onOpenFullDetailSheet: (RouteDiscoveryItem) -> Unit,
    onTogglePlanned: (String) -> Unit,
    onLaunchExternalNavigation: () -> Unit,
    onOpenSaveDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (route == null || route.polyline.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (appLanguage.isGerman) "Bitte zuerst eine Route berechnen." else "Please calculate a route first.",
                color = Color(0xFF64748B)
            )
        }
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFullscreenMapOpen by remember { mutableStateOf(false) }

    val handleMapIconClick: (RouteDiscoveryItem) -> Unit = { clickedItem ->
        onSelectItem(clickedItem)
        val idx = items.indexOfFirst { it.id == clickedItem.id }
        if (idx >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(index = idx + 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 0: Interactive Map Card + View Switcher ("Im Track" vs "Alle") + Track Metrics + Google Maps Transfer
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1628)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row with Route Title & Großansicht Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = travelModeIcon(route.travelMode),
                                    contentDescription = null,
                                    tint = Color(0xFF2CE5C6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${route.origin.name} → ${route.destination.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (isRecalculatingTrack) {
                                    if (appLanguage.isGerman) "Track wird an $inTrackCount ausgewählte Stopps angepasst…" else "Adjusting track to $inTrackCount selected stops…"
                                } else {
                                    "${route.travelMode.localizedTrackType(appLanguage)} • $inTrackCount ${if (appLanguage.isGerman) "Stopps im Track integriert" else "stops in track"}"
                                },
                                color = Color(0xFF2CE5C6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isAlreadySaved) {
                                FilledTonalButton(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        disabledContainerColor = Color(0xFF0F2942),
                                        disabledContentColor = Color(0xFF2CE5C6)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkAdded,
                                        contentDescription = null,
                                        tint = Color(0xFF2CE5C6),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (appLanguage.isGerman) "✓ Gespeichert" else "✓ Saved",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2CE5C6),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = onOpenSaveDialog,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF006B5E),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkAdd,
                                        contentDescription = if (appLanguage.isGerman) "Tour speichern" else "Save Tour",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (appLanguage.isGerman) "Tour speichern" else "Save Tour",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { isFullscreenMapOpen = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1E3A5F),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = if (appLanguage.isGerman) "Großansicht" else "Fullscreen",
                                    tint = Color(0xFF2CE5C6),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (appLanguage.isGerman) "Großansicht" else "Fullscreen",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // View Switcher Bar: "Im Track" vs. "Alle"
                    MapViewModeSwitcherBar(
                        currentFilter = mapDisplayFilter,
                        inTrackCount = inTrackCount,
                        allCount = allCorridorCount,
                        appLanguage = appLanguage,
                        onSelectFilter = onMapDisplayFilterChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 10.dp)
                    )

                    // Track Metrics Strip: Distanz (km), Höhenmeter (↑/↓ Hm), Reisedauer & Stopps
                    TrackMetricsStrip(
                        distanceKm = route.distanceKm,
                        elevationGainMeters = route.elevationGainMeters,
                        elevationLossMeters = route.elevationLossMeters,
                        durationMinutes = route.durationMinutes,
                        stopsCount = inTrackCount,
                        appLanguage = appLanguage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 10.dp)
                    )

                    // Interactive Map Surface with modified Track Polyline & Category Icons
                    InteractiveCorridorMapSurface(
                        route = route,
                        items = items,
                        plannedStopIds = plannedStopIds,
                        highlightedItemId = highlightedItemId,
                        maxDetourKm = maxDetourKm,
                        mapHeight = 310.dp,
                        enableZoomPan = true,
                        appLanguage = appLanguage,
                        onPinClick = handleMapIconClick,
                        onOpenFullscreen = { isFullscreenMapOpen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )

                    // Category Legend Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage.isGerman) "Kategorie-Icons:" else "Category Icons:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        WayCategory.entries.forEach { cat ->
                            val count = items.count { it.category == cat }
                            if (count > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF152642))
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${cat.localizedShortLabel(appLanguage)} ($count)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Prominent Button: Transfer Track with Stops to Google Maps!
                    Button(
                        onClick = onLaunchExternalNavigation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2CE5C6),
                            contentColor = Color(0xFF081224)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (appLanguage.isGerman) {
                                if (inTrackCount > 0) "Track mit $inTrackCount Stopps auf Google Maps (${route.travelMode.localizedLabel(appLanguage)})" else "Track auf Google Maps (${route.travelMode.localizedLabel(appLanguage)})"
                            } else {
                                if (inTrackCount > 0) "Transfer Track with $inTrackCount Stops to Google Maps (${route.travelMode.localizedLabel(appLanguage)})" else "Transfer Track to Google Maps (${route.travelMode.localizedLabel(appLanguage)})"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Section Title for Descriptions below the map
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFF006B5E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (mapDisplayFilter == MapDisplayFilter.IN_TRACK) {
                            if (appLanguage.isGerman) "Orte & Events im Track (${items.size})" else "Places & Events in Track (${items.size})"
                        } else {
                            if (appLanguage.isGerman) "Alle Orte im Korridor (${items.size})" else "All Places in Corridor (${items.size})"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Text(
                    text = if (appLanguage.isGerman) "Klick auf Icon springt hierher" else "Tap icon on map to jump here",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // 1..N: Detailed Description Cards for each Map Point
        itemsIndexed(items, key = { _, it -> "map_desc_${it.id}" }) { index, item ->
            val isSelected = item.id == highlightedItemId
            val isPlanned = item.id in plannedStopIds
            MapPointDescriptionCard(
                indexNumber = index + 1,
                item = item,
                isSelectedFromMap = isSelected,
                isPlanned = isPlanned,
                appLanguage = appLanguage,
                onClickCard = { onOpenFullDetailSheet(item) },
                onTogglePlanned = { onTogglePlanned(item.id) }
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }

    if (isFullscreenMapOpen) {
        FullscreenRouteMapDialog(
            route = route,
            items = items,
            allCorridorCount = allCorridorCount,
            inTrackCount = inTrackCount,
            mapDisplayFilter = mapDisplayFilter,
            onMapDisplayFilterChange = onMapDisplayFilterChange,
            plannedStopIds = plannedStopIds,
            highlightedItemId = highlightedItemId,
            maxDetourKm = maxDetourKm,
            appLanguage = appLanguage,
            onSelectItem = onSelectItem,
            onOpenFullDetailSheet = onOpenFullDetailSheet,
            onTogglePlanned = onTogglePlanned,
            onLaunchExternalNavigation = onLaunchExternalNavigation,
            onOpenSaveDialog = onOpenSaveDialog,
            onDismiss = { isFullscreenMapOpen = false }
        )
    }
}

/**
 * Switcher between "Im Track" (only stops/events selected for the tour) and "Alle" (all corridor discoveries).
 */
@Composable
fun MapViewModeSwitcherBar(
    currentFilter: MapDisplayFilter,
    inTrackCount: Int,
    allCount: Int,
    appLanguage: AppLanguage = AppLanguage.DE,
    onSelectFilter: (MapDisplayFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF152946),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (appLanguage.isGerman) "Ansicht:" else "View:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                maxLines = 1,
                softWrap = false
            )

            // Option 1: "Im Track"
            val inTrackSelected = currentFilter == MapDisplayFilter.IN_TRACK
            Surface(
                color = if (inTrackSelected) Color(0xFFFFD15C) else Color.Transparent,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onSelectFilter(MapDisplayFilter.IN_TRACK) }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (inTrackSelected) Color(0xFF081224) else Color(0xFFFFD15C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${if (appLanguage.isGerman) "Im Track" else "In Track"} ($inTrackCount)",
                        color = if (inTrackSelected) Color(0xFF081224) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Option 2: "Alle"
            val allSelected = currentFilter == MapDisplayFilter.ALL
            Surface(
                color = if (allSelected) Color(0xFF2CE5C6) else Color.Transparent,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onSelectFilter(MapDisplayFilter.ALL) }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = if (allSelected) Color(0xFF081224) else Color(0xFF2CE5C6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${if (appLanguage.isGerman) "Alle" else "All"} ($allCount)",
                        color = if (allSelected) Color(0xFF081224) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun TrackMetricsStrip(
    distanceKm: Double,
    elevationGainMeters: Int,
    elevationLossMeters: Int,
    durationMinutes: Int,
    stopsCount: Int,
    appLanguage: AppLanguage = AppLanguage.DE,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF132644),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2CE5C6).copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricColumnItem(
                icon = Icons.Default.Route,
                label = if (appLanguage.isGerman) "Distanz" else "Distance",
                value = "${String.format("%.1f", distanceKm)} km",
                tint = Color(0xFF2CE5C6)
            )
            MetricColumnItem(
                icon = Icons.Default.Terrain,
                label = if (appLanguage.isGerman) "Höhenmeter" else "Elevation",
                value = "↑$elevationGainMeters / ↓$elevationLossMeters Hm",
                tint = Color(0xFFFFD15C)
            )
            MetricColumnItem(
                icon = Icons.Default.Schedule,
                label = if (appLanguage.isGerman) "Reisedauer" else "Duration",
                value = formatDuration(durationMinutes),
                tint = Color(0xFF38BDF8)
            )
            MetricColumnItem(
                icon = Icons.Default.Flag,
                label = if (appLanguage.isGerman) "Im Track" else "In Track",
                value = "$stopsCount ${if (appLanguage.isGerman) "Stopps" else "Stops"}",
                tint = Color(0xFFFF8A3D)
            )
        }
    }
}

@Composable
private fun MetricColumnItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
            Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun InteractiveCorridorMapSurface(
    route: RouteResult,
    items: List<RouteDiscoveryItem>,
    plannedStopIds: Set<String>,
    highlightedItemId: String?,
    maxDetourKm: Float,
    mapHeight: Dp,
    enableZoomPan: Boolean,
    appLanguage: AppLanguage = AppLanguage.DE,
    onPinClick: (RouteDiscoveryItem) -> Unit,
    onOpenFullscreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activePoly = route.polyline
    val basePoly = route.basePolyline.ifEmpty { activePoly }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .height(mapHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E223D), Color(0xFF081224))
                )
            )
            .then(
                if (enableZoomPan) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val newZoom = (zoom * gestureZoom).coerceIn(1f, 4.5f)
                            zoom = newZoom
                            if (newZoom > 1.01f) {
                                val maxPanX = size.width * (newZoom - 1f) * 0.55f
                                val maxPanY = size.height * (newZoom - 1f) * 0.55f
                                panOffset = Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                                )
                            } else {
                                panOffset = Offset.Zero
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val heightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

        val allPoints = basePoly + activePoly + items.map { it.point }
        val minLat = allPoints.minOf { it.lat }
        val maxLat = allPoints.maxOf { it.lat }
        val minLon = allPoints.minOf { it.lon }
        val maxLon = allPoints.maxOf { it.lon }
        val midLatRad = Math.toRadians((minLat + maxLat) / 2.0)
        val lonScale = cos(midLatRad).coerceAtLeast(0.25)

        val padLat = ((maxLat - minLat) * 0.24).coerceAtLeast(0.08)
        val padLon = ((maxLon - minLon) * 0.24).coerceAtLeast(0.08)
        val bMinLat = minLat - padLat
        val bMaxLat = maxLat + padLat
        val bMinLon = minLon - padLon
        val bMaxLon = maxLon + padLon

        val worldWidthKm = (bMaxLon - bMinLon) * lonScale * 111.0
        val worldHeightKm = (bMaxLat - bMinLat) * 111.0

        val baseScaleX = widthPx / worldWidthKm.coerceAtLeast(1.0)
        val baseScaleY = heightPx / worldHeightKm.coerceAtLeast(1.0)
        val pxPerKm = (min(baseScaleX, baseScaleY) * zoom).toFloat()

        val usedWidthPx = (worldWidthKm * (pxPerKm / zoom)).toFloat()
        val usedHeightPx = (worldHeightKm * (pxPerKm / zoom)).toFloat()
        val baseOffsetX = (widthPx - usedWidthPx) / 2f
        val baseOffsetY = (heightPx - usedHeightPx) / 2f

        fun toScreen(pt: GeoPoint): Offset {
            val xKm = (pt.lon - bMinLon) * lonScale * 111.0
            val yKm = (bMaxLat - pt.lat) * 111.0
            val rawX = baseOffsetX + (xKm * (pxPerKm / zoom)).toFloat()
            val rawY = baseOffsetY + (yKm * (pxPerKm / zoom)).toFloat()
            val cx = widthPx / 2f
            val cy = heightPx / 2f
            return Offset(
                x = cx + (rawX - cx) * zoom + panOffset.x,
                y = cy + (rawY - cy) * zoom + panOffset.y
            )
        }

        // 1. Base Canvas for Grid, Corridor Buffer, Base Route & Modified Track Line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            var gx = 0f
            while (gx < size.width) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.50f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy < size.height) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.50f),
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            val basePath = Path()
            basePoly.forEachIndexed { idx, pt ->
                val pos = toScreen(pt)
                if (idx == 0) basePath.moveTo(pos.x, pos.y) else basePath.lineTo(pos.x, pos.y)
            }

            val corridorStrokePx = (maxDetourKm * 2f * pxPerKm).coerceIn(24f, 220f)
            drawPath(
                path = basePath,
                color = Color(0xFF2CE5C6).copy(alpha = 0.10f),
                style = Stroke(width = corridorStrokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = basePath,
                color = Color(0xFF2CE5C6).copy(alpha = 0.24f),
                style = Stroke(
                    width = corridorStrokePx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )

            if (plannedStopIds.isNotEmpty()) {
                drawPath(
                    path = basePath,
                    color = Color(0xFF64748B).copy(alpha = 0.45f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
            }

            val activeTrackPath = Path()
            activePoly.forEachIndexed { idx, pt ->
                val pos = toScreen(pt)
                if (idx == 0) activeTrackPath.moveTo(pos.x, pos.y) else activeTrackPath.lineTo(pos.x, pos.y)
            }

            drawPath(
                path = activeTrackPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF8A3D), Color(0xFFFFD15C), Color(0xFF2CE5C6))
                ),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            items.forEach { item ->
                if (item.id !in plannedStopIds) {
                    val pinPos = toScreen(item.point)
                    val proj = GeoMath.projectOntoRoute(item.point, activePoly)
                    val routePos = toScreen(proj.closestRoutePoint)
                    val isHighlighted = item.id == highlightedItemId

                    drawLine(
                        color = if (isHighlighted) item.category.color else item.category.color.copy(alpha = 0.42f),
                        start = routePos,
                        end = pinPos,
                        strokeWidth = if (isHighlighted) 2.2.dp.toPx() else 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }
            }

            val startPos = toScreen(activePoly.first())
            val endPos = toScreen(activePoly.last())
            drawCircle(color = Color.White, radius = 8.5.dp.toPx(), center = startPos)
            drawCircle(color = Color(0xFFFF7B29), radius = 6.dp.toPx(), center = startPos)

            drawCircle(color = Color.White, radius = 8.5.dp.toPx(), center = endPos)
            drawCircle(color = Color(0xFF2CE5C6), radius = 6.dp.toPx(), center = endPos)
        }

        // 2. Interactive Category Icon Pins overlaid at their exact projected positions
        items.forEach { item ->
            val screenPos = toScreen(item.point)
            val isHighlighted = item.id == highlightedItemId
            val isPlanned = item.id in plannedStopIds
            val pinSizeDp by animateDpAsState(
                targetValue = if (isHighlighted) 36.dp else if (isPlanned) 31.dp else 26.dp,
                label = "pinSize"
            )
            val pinHalfPx = with(density) { (pinSizeDp / 2).roundToPx() }

            if (screenPos.x in -40f..(widthPx + 40f) && screenPos.y in -40f..(heightPx + 40f)) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = screenPos.x.roundToInt() - pinHalfPx,
                                y = screenPos.y.roundToInt() - pinHalfPx
                            )
                        }
                        .size(pinSizeDp)
                        .shadow(if (isHighlighted || isPlanned) 8.dp else 3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (isHighlighted) item.category.color
                            else if (isPlanned) Color(0xFF152946)
                            else Color(0xFF0B1628)
                        )
                        .border(
                            width = if (isPlanned) 2.5.dp else if (isHighlighted) 2.dp else 1.5.dp,
                            color = if (isPlanned) Color(0xFFFFD15C) else if (isHighlighted) Color.White else item.category.color,
                            shape = CircleShape
                        )
                        .clickable { onPinClick(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(item.category),
                        contentDescription = "${item.category.localizedLabel(appLanguage)}: ${item.displayTitle(appLanguage)}",
                        tint = if (isHighlighted) Color(0xFF081224) else item.category.color,
                        modifier = Modifier.size(if (isHighlighted) 20.dp else 15.dp)
                    )
                }

                if (isHighlighted) {
                    val calloutOffsetY = with(density) { (pinSizeDp / 2 + 26.dp).roundToPx() }
                    Surface(
                        color = Color(0xEE081224),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, item.category.color),
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (screenPos.x.roundToInt() - 75).coerceIn(6, (widthPx - 160).roundToInt().coerceAtLeast(6)),
                                    y = (screenPos.y.roundToInt() - calloutOffsetY).coerceAtLeast(6)
                                )
                            }
                            .widthIn(max = 165.dp)
                            .clickable { onPinClick(item) }
                    ) {
                        Text(
                            text = item.displayTitle(appLanguage),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 3. Map Overlay Controls: Zoom + / - / Reset & Fullscreen
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onOpenFullscreen != null) {
                SmallMapControlButton(
                    icon = Icons.Default.OpenInFull,
                    contentDesc = "Vollbild-Karte öffnen",
                    onClick = onOpenFullscreen
                )
            }
            SmallMapControlButton(
                icon = Icons.Default.Add,
                contentDesc = "Hineinzoomen",
                onClick = { zoom = (zoom * 1.35f).coerceAtMost(4.5f) }
            )
            SmallMapControlButton(
                icon = Icons.Default.Remove,
                contentDesc = "Herauszoomen",
                onClick = {
                    val next = (zoom / 1.35f).coerceAtLeast(1f)
                    zoom = next
                    if (next <= 1.05f) panOffset = Offset.Zero
                }
            )
            if (zoom > 1.05f || panOffset != Offset.Zero) {
                SmallMapControlButton(
                    icon = Icons.Default.CenterFocusStrong,
                    contentDesc = "Ansicht zentrieren",
                    onClick = {
                        zoom = 1f
                        panOffset = Offset.Zero
                    }
                )
            }
        }

        // 4. Top-Start Info Badge
        Surface(
            color = Color(0xCC081224),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF7B29))
                )
                Spacer(Modifier.width(4.dp))
                Text(route.origin.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                Text(" → ", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2CE5C6))
                )
                Spacer(Modifier.width(4.dp))
                Text(route.destination.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SmallMapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xD9152946),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
fun FullscreenRouteMapDialog(
    route: RouteResult,
    items: List<RouteDiscoveryItem>,
    allCorridorCount: Int,
    inTrackCount: Int,
    mapDisplayFilter: MapDisplayFilter,
    onMapDisplayFilterChange: (MapDisplayFilter) -> Unit,
    plannedStopIds: Set<String>,
    highlightedItemId: String?,
    maxDetourKm: Float,
    appLanguage: AppLanguage = AppLanguage.DE,
    onSelectItem: (RouteDiscoveryItem) -> Unit,
    onOpenFullDetailSheet: (RouteDiscoveryItem) -> Unit,
    onTogglePlanned: (String) -> Unit,
    onLaunchExternalNavigation: () -> Unit,
    onOpenSaveDialog: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF081224),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Fullscreen Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${if (appLanguage.isGerman) "Großansicht" else "Fullscreen"}: ${route.origin.name} → ${route.destination.name}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${route.travelMode.localizedTrackType(appLanguage)} • ${if (appLanguage.isGerman) "Klick auf ein Icon springt zur Beschreibung unten" else "Tap an icon to jump to description below"}",
                            color = Color(0xFF2CE5C6),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onOpenSaveDialog,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF006B5E),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Tour speichern" else "Save Tour",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        FilledTonalButton(
                            onClick = onLaunchExternalNavigation,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF2CE5C6),
                                contentColor = Color(0xFF081224)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Google Maps",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        FilledTonalButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF1E3A5F),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Schließen" else "Close",
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Switcher in Fullscreen View: "Im Track" vs. "Alle"
                MapViewModeSwitcherBar(
                    currentFilter = mapDisplayFilter,
                    inTrackCount = inTrackCount,
                    allCount = allCorridorCount,
                    appLanguage = appLanguage,
                    onSelectFilter = onMapDisplayFilterChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                )

                // Track Metrics Strip in Fullscreen View
                TrackMetricsStrip(
                    distanceKm = route.distanceKm,
                    elevationGainMeters = route.elevationGainMeters,
                    elevationLossMeters = route.elevationLossMeters,
                    durationMinutes = route.durationMinutes,
                    stopsCount = inTrackCount,
                    appLanguage = appLanguage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp)
                )

                // Interactive Map with Category Icon Pins
                InteractiveCorridorMapSurface(
                    route = route,
                    items = items,
                    plannedStopIds = plannedStopIds,
                    highlightedItemId = highlightedItemId,
                    maxDetourKm = maxDetourKm,
                    mapHeight = 340.dp,
                    enableZoomPan = true,
                    appLanguage = appLanguage,
                    onPinClick = { clicked ->
                        onSelectItem(clicked)
                        val idx = items.indexOfFirst { it.id == clicked.id }
                        if (idx >= 0) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(idx)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.52f)
                        .padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Lower Section: Synchronized Scrollable Descriptions
                Surface(
                    color = Color(0xFFF4F7FB),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.48f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (mapDisplayFilter == MapDisplayFilter.IN_TRACK) {
                                    if (appLanguage.isGerman) "Orte & Events im Track (${items.size})" else "Places & Events in Track (${items.size})"
                                } else {
                                    if (appLanguage.isGerman) "Alle Orte im Korridor (${items.size})" else "All Places in Corridor (${items.size})"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = if (appLanguage.isGerman) "Tippe auf Icon oben zum Springen" else "Tap icon above to jump",
                                fontSize = 11.sp,
                                color = Color(0xFF006B5E),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(items, key = { _, it -> "fs_desc_${it.id}" }) { idx, item ->
                                MapPointDescriptionCard(
                                    indexNumber = idx + 1,
                                    item = item,
                                    isSelectedFromMap = item.id == highlightedItemId,
                                    isPlanned = item.id in plannedStopIds,
                                    appLanguage = appLanguage,
                                    onClickCard = { onOpenFullDetailSheet(item) },
                                    onTogglePlanned = { onTogglePlanned(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapPointDescriptionCard(
    indexNumber: Int,
    item: RouteDiscoveryItem,
    isSelectedFromMap: Boolean,
    isPlanned: Boolean,
    appLanguage: AppLanguage = AppLanguage.DE,
    onClickCard: () -> Unit,
    onTogglePlanned: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelectedFromMap) Color(0xFFECFDF5) else Color.White,
        label = "descCardBg"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedFromMap) 6.dp else 2.dp),
        border = if (isSelectedFromMap) {
            androidx.compose.foundation.BorderStroke(2.dp, item.category.color)
        } else if (isPlanned) {
            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF8A3D))
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DiscoveryThumbnail(
                    item = item,
                    size = 78.dp,
                    cornerRadius = 14.dp
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = item.category.color,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "#$indexNumber • ${item.category.localizedLabel(appLanguage)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (isSelectedFromMap) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (appLanguage.isGerman) "Auf Karte ausgewählt" else "Selected on Map",
                                    color = Color(0xFF2CE5C6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Text(
                        text = item.displayTitle(appLanguage),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    Text(
                        text = item.displaySubtitle(appLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (appLanguage.isGerman) {
                            if (item.detourDistanceKm < 0.15) {
                                "Bei km ${item.routeProgressKm.roundToInt()} • Direkt an der Route (0 km Abstecher)"
                            } else {
                                "Bei km ${item.routeProgressKm.roundToInt()} • +${String.format(java.util.Locale.US, "%.1f", item.detourDistanceKm * 2.0)} km Abstecher (~${item.extraDetourMinutes} Min.)"
                            }
                        } else {
                            if (item.detourDistanceKm < 0.15) {
                                "At km ${item.routeProgressKm.roundToInt()} • Directly on route (0 km detour)"
                            } else {
                                "At km ${item.routeProgressKm.roundToInt()} • +${String.format(java.util.Locale.US, "%.1f", item.detourDistanceKm * 2.0)} km detour (~${item.extraDetourMinutes} min)"
                            }
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF006B5E)
                    )
                }
            }

            Text(
                text = item.displayDescription(appLanguage),
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 18.sp
            )

            val eventInfo = item.displayEventDate(appLanguage)
            if (eventInfo != null || item.openingHours != null) {
                Text(
                    text = eventInfo ?: (if (appLanguage.isGerman) "Öffnungszeiten: ${item.openingHours}" else "Opening hours: ${item.openingHours}"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFBE185D)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClickCard,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (appLanguage.isGerman) "Alle Details & Links" else "Details & Links",
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onTogglePlanned,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlanned) Color(0xFFFF8A3D) else Color(0xFF006B5E),
                        contentColor = if (isPlanned) Color(0xFF081224) else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (isPlanned) Icons.Default.Check else Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isPlanned) {
                            if (appLanguage.isGerman) "Im Track aktiv" else "Active in Track"
                        } else {
                            if (appLanguage.isGerman) "+ In Track einbauen" else "+ Add Stop"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

fun formatDuration(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m} Min."
}

fun travelModeIcon(mode: TravelMode) = when (mode) {
    TravelMode.CAR -> Icons.Default.DirectionsCar
    TravelMode.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
    TravelMode.HIKE -> Icons.AutoMirrored.Filled.DirectionsWalk
}

fun categoryIcon(category: WayCategory) = when (category) {
    WayCategory.NATUR -> Icons.Default.Park
    WayCategory.ERLEBNIS -> Icons.Default.Attractions
    WayCategory.TAGESEVENTS -> Icons.Default.ConfirmationNumber
    WayCategory.KOSTENLOSES -> Icons.Default.Savings
    WayCategory.CAFES -> Icons.Default.LocalCafe
    WayCategory.RESTAURANTS -> Icons.Default.Restaurant
    WayCategory.WANDERUNGEN -> Icons.Default.Hiking
    WayCategory.HISTORISCHES -> Icons.Default.Castle
    WayCategory.KULTURELLES -> Icons.Default.Museum
}
