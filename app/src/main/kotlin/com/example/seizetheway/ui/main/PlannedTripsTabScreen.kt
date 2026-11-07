package com.example.seizetheway.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seizetheway.data.*
import com.example.seizetheway.model.*
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PlannedTripsTabScreen(
    state: SeizeTheWayUiState,
    onAutoCurateStops: () -> Unit,
    onRemovePlannedStop: (String) -> Unit,
    onOpenItemDetail: (RouteDiscoveryItem) -> Unit,
    onShowItemOnMap: (RouteDiscoveryItem) -> Unit,
    onSaveCurrentTrip: (String, String) -> Unit,
    onLoadSavedTrip: (SavedTrip) -> Unit,
    onDeleteSavedTrip: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onTransferTrackToGoogleMaps: (GeoPoint, GeoPoint, List<RouteDiscoveryItem>, TravelMode) -> Unit,
    onGoToDiscoveriesTab: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Current Active Tour Card (Mit grafischem Höhenprofil, Zeit-Diagramm & Kategorien-Mix)
        item {
            ActiveTourCard(
                route = state.route,
                plannedStops = state.plannedStops,
                totalExtraKm = state.totalExtraDetourKm,
                notes = state.tripNotesDraft,
                isAlreadySaved = state.isCurrentTourAlreadySaved,
                appLanguage = state.appLanguage,
                onNotesChange = onUpdateNotes,
                onAutoCurate = onAutoCurateStops,
                onOpenSaveDialog = onOpenSaveDialog,
                onTransferToGoogleMaps = {
                    state.route?.let { r ->
                        onTransferTrackToGoogleMaps(r.origin, r.destination, state.plannedStops, r.travelMode)
                    }
                },
                onShareTrip = {
                    state.route?.let { r ->
                        shareTripItinerary(
                            context = context,
                            title = "${r.origin.name} → ${r.destination.name} (${r.travelMode.localizedLabel(state.appLanguage)})",
                            originName = r.origin.name,
                            destName = r.destination.name,
                            trackKm = r.distanceKm,
                            elevationGain = r.elevationGainMeters,
                            elevationLoss = r.elevationLossMeters,
                            durationMin = r.durationMinutes,
                            stops = state.plannedStops,
                            notes = state.tripNotesDraft,
                            appLanguage = state.appLanguage
                        )
                    }
                },
                onOpenItemDetail = onOpenItemDetail,
                onRemoveStop = onRemovePlannedStop,
                onGoToDiscoveriesTab = onGoToDiscoveriesTab
            )
        }

        // 2. Saved Tours Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = null,
                            tint = Color(0xFF006B5E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Gespeicherte Touren (${state.savedTrips.size})"
                        } else {
                            "Saved Tours (${state.savedTrips.size})"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Archivierte Touren mit Grafiken, Höhenprofil, Events & Wechsel zur Karte"
                        } else {
                            "Archived tours with charts, elevation profile, events & map switch"
                        },
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // 3. Saved Tours Cards
        if (state.savedTrips.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardTravel,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (state.appLanguage.isGerman) "Noch keine Tour gespeichert" else "No tours saved yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = if (state.appLanguage.isGerman) {
                                "Speichere deinen aktuellen Track inklusive Stopps & Events oben über 'Tour speichern' oder direkt im Tab 'Karte'."
                            } else {
                                "Save your current track including stops & events using 'Save Tour' above or directly in the 'Map' tab."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(state.savedTrips, key = { it.id }) { trip ->
                SavedTourArchiveCard(
                    trip = trip,
                    appLanguage = state.appLanguage,
                    onOpenTourOnMap = { onLoadSavedTrip(trip) },
                    onTransferToGoogleMaps = {
                        onTransferTrackToGoogleMaps(
                            trip.origin,
                            trip.destination,
                            trip.stops,
                            trip.travelMode
                        )
                    },
                    onOpenStopDetail = onOpenItemDetail,
                    onShareTrip = {
                        shareTripItinerary(
                            context = context,
                            title = trip.title,
                            originName = trip.originName,
                            destName = trip.destinationName,
                            trackKm = trip.trackDistanceKm,
                            elevationGain = trip.elevationGainMeters,
                            elevationLoss = trip.elevationLossMeters,
                            durationMin = trip.trackDurationMinutes,
                            stops = trip.stops,
                            notes = trip.notes,
                            appLanguage = state.appLanguage
                        )
                    },
                    onDeleteTrip = { onDeleteSavedTrip(trip.id) }
                )
            }
        }

        // Google AdMob Banner
        item {
            GoogleAdMobBanner(appLanguage = state.appLanguage)
        }

        item {
            Spacer(Modifier.height(28.dp))
        }
    }
}

/**
 * Die aktuelle Tour im modernen Grafik-Dashboard-Design:
 * - 2x2 KPI-Kacheln (Distanz, Höhenmeter, Reine Bewegungszeit, Gesamtdauer & Energie)
 * - Grafisches Höhen- & Etappenprofil (Canvas-Kurve mit eingezeichneten Zwischenstopps)
 * - Grafische Zeit-Aufteilung (Bewegungszeit vs. Aufenthalt) & Kategorien-Mix-Balken
 * - Standardmäßig leeres Notizenfeld mit Lösch-Button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveTourCard(
    route: RouteResult?,
    plannedStops: List<RouteDiscoveryItem>,
    totalExtraKm: Double,
    notes: String,
    isAlreadySaved: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.DE,
    onNotesChange: (String) -> Unit,
    onAutoCurate: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    onTransferToGoogleMaps: () -> Unit,
    onShareTrip: () -> Unit,
    onOpenItemDetail: (RouteDiscoveryItem) -> Unit,
    onRemoveStop: (String) -> Unit,
    onGoToDiscoveriesTab: () -> Unit
) {
    val stayMinutes = plannedStops.size * 40
    val travelMinutes = route?.durationMinutes ?: 0
    val totalDayMinutes = travelMinutes + stayMinutes
    val difficulty = route?.let {
        computeTourDifficultyLabel(it.travelMode, it.distanceKm, it.elevationGainMeters, appLanguage)
    } ?: (if (appLanguage.isGerman) "Geplante Tour" else "Planned Tour")
    val energyEstimate = route?.let {
        computeTourEnergyEstimate(it.travelMode, it.distanceKm, it.elevationGainMeters, appLanguage)
    } ?: ("-- kcal" to "")

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFF006B5E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header: Live-Badge, Mode/Difficulty, Optimaler Mix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (appLanguage.isGerman) "AKTIVE TOUR (LIVE)" else "ACTIVE TOUR (LIVE)",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (route != null) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${route.travelMode.localizedLabel(appLanguage)} • $difficulty",
                                    color = Color(0xFF2CE5C6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = route?.let { "${it.origin.name} → ${it.destination.name}" }
                            ?: (if (appLanguage.isGerman) "Aktuelle Route" else "Current Route"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (route != null) {
                val avgSpeedKmh = if (route.durationMinutes > 0) {
                    (route.distanceKm / (route.durationMinutes / 60.0)).roundToInt()
                } else route.travelMode.avgSpeedKmh.roundToInt()

                val avgGradePct = if (route.distanceKm > 0.1) {
                    ((route.elevationGainMeters.toDouble() / (route.distanceKm * 1000.0)) * 100.0)
                } else 0.0

                // 3. 2x2 KPI Dashboard Grid (Übersichtliche Kennzahlen-Kacheln)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TourKpiMetricTile(
                            icon = Icons.Default.Route,
                            iconTint = Color(0xFF006B5E),
                            iconBg = Color(0xFFCCFBF1),
                            label = if (appLanguage.isGerman) "Gesamtdistanz" else "Total Distance",
                            primaryValue = "${String.format(java.util.Locale.US, "%.1f", route.distanceKm)} km",
                            subValue = if (totalExtraKm > 0.05) {
                                if (appLanguage.isGerman) "+${String.format(java.util.Locale.US, "%.1f", totalExtraKm)} km für Stopps"
                                else "+${String.format(java.util.Locale.US, "%.1f", totalExtraKm)} km for stops"
                            } else {
                                if (appLanguage.isGerman) "Direkt auf dem Track" else "Directly on track"
                            },
                            modifier = Modifier.weight(1f)
                        )

                        TourKpiMetricTile(
                            icon = Icons.Default.Terrain,
                            iconTint = Color(0xFFD97706),
                            iconBg = Color(0xFFFEF3C7),
                            label = if (appLanguage.isGerman) "Höhenmeter" else "Elevation",
                            primaryValue = "↑${route.elevationGainMeters} • ↓${route.elevationLossMeters} Hm",
                            subValue = "${route.minElevationMeters}–${route.maxElevationMeters} m • Ø ${String.format(java.util.Locale.US, "%.1f", avgGradePct)}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TourKpiMetricTile(
                            icon = Icons.Default.Schedule,
                            iconTint = Color(0xFF0284C7),
                            iconBg = Color(0xFFE0F2FE),
                            label = if (appLanguage.isGerman) "Reine Fahr-/Gehzeit" else "Pure Travel Time",
                            primaryValue = formatDuration(route.durationMinutes),
                            subValue = "Ø $avgSpeedKmh km/h (${route.travelMode.localizedLabel(appLanguage)})",
                            modifier = Modifier.weight(1f)
                        )

                        TourKpiMetricTile(
                            icon = Icons.Default.Bolt,
                            iconTint = Color(0xFF7C3AED),
                            iconBg = Color(0xFFEDE9FE),
                            label = if (appLanguage.isGerman) "Gesamtdauer & Energie" else "Total Time & Energy",
                            primaryValue = "~${formatDuration(totalDayMinutes)}",
                            subValue = "⚡ ${energyEstimate.first}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. GRAFISCHE AUFBEREITUNG 1: Höhen- & Etappen-Profil-Grafik (Canvas)
                TourElevationAndStopsGraphic(
                    originName = route.origin.name,
                    destName = route.destination.name,
                    distanceKm = route.distanceKm,
                    elevationGain = route.elevationGainMeters,
                    elevationLoss = route.elevationLossMeters,
                    minElevation = route.minElevationMeters,
                    maxElevation = route.maxElevationMeters,
                    stops = plannedStops,
                    appLanguage = appLanguage
                )

                // 5. GRAFISCHE AUFBEREITUNG 2: Zeit-Aufteilung & Kategorien-Mix-Grafik
                TourTimeAndCategoryAnalyticsCard(
                    travelMinutes = travelMinutes,
                    stayMinutes = stayMinutes,
                    stops = plannedStops,
                    appLanguage = appLanguage
                )
            }

            // 6. Selected Places & Events Horizontal Cards
            if (plannedStops.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage.isGerman) {
                            "Etappen-Stopps & Events (${plannedStops.size}):"
                        } else {
                            "Stage Stops & Events (${plannedStops.size}):"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (appLanguage.isGerman) "Tippen für Details" else "Tap for details",
                        fontSize = 11.sp,
                        color = Color(0xFF006B5E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    plannedStops.forEachIndexed { idx, stop ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .width(235.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenItemDetail(stop) }
                        ) {
                            Row(
                                modifier = Modifier.padding(9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DiscoveryThumbnail(
                                    item = stop,
                                    size = 54.dp,
                                    cornerRadius = 11.dp
                                )
                                Spacer(Modifier.width(9.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${idx + 1} • ${stop.category.localizedLabel(appLanguage)}",
                                        color = stop.category.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = stop.displayTitle(appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (stop.detourDistanceKm < 0.15) {
                                            if (appLanguage.isGerman) "km ${stop.routeProgressKm.roundToInt()} • Direkt am Track"
                                            else "km ${stop.routeProgressKm.roundToInt()} • On track"
                                        } else {
                                            "km ${stop.routeProgressKm.roundToInt()} • +${String.format(java.util.Locale.US, "%.1f", stop.detourDistanceKm * 2.0)} km"
                                        },
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    stop.displayEventDate(appLanguage)?.let { ev ->
                                        Text(
                                            text = "📅 $ev",
                                            fontSize = 9.5.sp,
                                            color = Color(0xFFBE185D),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onRemoveStop(stop.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (appLanguage.isGerman) "Aus Tour entfernen" else "Remove from tour",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add More Places Chip
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .width(135.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onGoToDiscoveriesTab() }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = Color(0xFF006B5E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (appLanguage.isGerman) "+ Mehr Orte" else "+ More Places",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006B5E),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (appLanguage.isGerman) "Direkter Track ohne Zwischenstopps" else "Direct track without stops",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = if (appLanguage.isGerman) {
                                    "Wähle Orte im Tab 'Ergebnisse' oder klicke auf 'Optimaler Mix'."
                                } else {
                                    "Select places in 'Results' or tap 'Optimal Mix'."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        OutlinedButton(
                            onClick = onGoToDiscoveriesTab,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (appLanguage.isGerman) "Orte wählen" else "Select places",
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 7. Notes Input (Default EMPTY as requested, with placeholder & clear button!)
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = {
                    Text(
                        if (appLanguage.isGerman) "Touren-Notizen (optional)"
                        else "Tour Notes (optional)"
                    )
                },
                placeholder = {
                    Text(
                        text = if (appLanguage.isGerman) "Hier eigene Notizen, Packliste oder Ticket-Infos eintragen…"
                        else "Enter your own notes, packing list or ticket info here…",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                colors = seizeInputFieldColors(),
                leadingIcon = {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF006B5E))
                },
                trailingIcon = {
                    if (notes.isNotEmpty()) {
                        IconButton(onClick = { onNotesChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = if (appLanguage.isGerman) "Notiz löschen" else "Clear note",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 8. Primary Action Buttons Row (Save Tour, Google Maps Transfer, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAlreadySaved) {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            disabledContainerColor = Color(0xFFDCFCE7),
                            disabledContentColor = Color(0xFF15803D)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (appLanguage.isGerman) "✓ Gespeichert" else "✓ Saved",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Button(
                        onClick = onOpenSaveDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006B5E),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (appLanguage.isGerman) "Tour speichern" else "Save Tour",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Button(
                    onClick = onTransferToGoogleMaps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Google Maps",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onShareTrip)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (appLanguage.isGerman) "Teilen" else "Share",
                            tint = Color(0xFF006B5E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TourKpiMetricTile(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    primaryValue: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = iconBg,
                    shape = RoundedCornerShape(7.dp),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = primaryValue,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subValue,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF006B5E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Grafisches Höhen- & Etappen-Profil (Canvas-Visualisierung ohne Karten-Kachel):
 * Zeigt den Höhenverlauf von Start bis Ziel inklusive markierter Zwischenstopps entlang der km-Achse.
 */
@Composable
private fun TourElevationAndStopsGraphic(
    originName: String,
    destName: String,
    distanceKm: Double,
    elevationGain: Int,
    elevationLoss: Int,
    minElevation: Int,
    maxElevation: Int,
    stops: List<RouteDiscoveryItem>,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = Color(0xFF2CE5C6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (appLanguage.isGerman) "Höhen- & Etappen-Profil" else "Elevation & Stage Profile",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp
                    )
                }
                Text(
                    text = "${minElevation}m – ${maxElevation}m ü. NHN",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Elevation Curve + Stop Pins Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                val w = size.width
                val h = size.height
                val topPad = 12.dp.toPx()
                val bottomPad = 14.dp.toPx()
                val usableH = (h - topPad - bottomPad).coerceAtLeast(10f)

                // Horizontal reference grid lines
                val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for (row in 0..2) {
                    val y = topPad + (usableH * row / 2f)
                    drawLine(
                        color = Color(0xFF334155).copy(alpha = 0.65f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = dash
                    )
                }

                // Synthesize a smooth realistic elevation curve from gain/loss & stop fractions
                val steps = 48
                val netDeltaRatio = ((elevationGain - elevationLoss).toFloat() /
                        (elevationGain + elevationLoss).coerceAtLeast(100).toFloat()).coerceIn(-0.5f, 0.5f)

                fun elevationFractionAt(t: Float): Float {
                    val wave1 = sin(t * PI * 2.2 + 0.4).toFloat() * 0.24f
                    val wave2 = sin(t * PI * 4.5 + 1.1).toFloat() * 0.11f
                    val trend = (t - 0.5f) * netDeltaRatio
                    return (0.52f + wave1 + wave2 + trend).coerceIn(0.12f, 0.92f)
                }

                val linePath = Path()
                val fillPath = Path()

                for (i in 0..steps) {
                    val t = i.toFloat() / steps.toFloat()
                    val x = t * w
                    val y = topPad + usableH * (1f - elevationFractionAt(t))
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h - bottomPad)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(w, h - bottomPad)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2CE5C6).copy(alpha = 0.36f),
                            Color(0xFF2CE5C6).copy(alpha = 0.03f)
                        ),
                        startY = topPad,
                        endY = h - bottomPad
                    )
                )

                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF8A3D), Color(0xFFFFD15C), Color(0xFF2CE5C6))
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Start & End Dots
                val startY = topPad + usableH * (1f - elevationFractionAt(0f))
                val endY = topPad + usableH * (1f - elevationFractionAt(1f))
                drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = Offset(4.dp.toPx(), startY))
                drawCircle(color = Color(0xFFFF8A3D), radius = 3.5.dp.toPx(), center = Offset(4.dp.toPx(), startY))

                drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = Offset(w - 4.dp.toPx(), endY))
                drawCircle(color = Color(0xFF2CE5C6), radius = 3.5.dp.toPx(), center = Offset(w - 4.dp.toPx(), endY))

                // Planned Stop Pins along the curve
                stops.forEach { stop ->
                    val t = stop.routeProgressFraction.coerceIn(0.05f, 0.95f)
                    val sx = t * w
                    val sy = topPad + usableH * (1f - elevationFractionAt(t))
                    drawLine(
                        color = stop.category.color.copy(alpha = 0.65f),
                        start = Offset(sx, sy),
                        end = Offset(sx, h - bottomPad),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dash
                    )
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(sx, sy))
                    drawCircle(color = stop.category.color, radius = 4.dp.toPx(), center = Offset(sx, sy))
                }
            }

            // Bottom Axis Labels (0 km -> Midpoint -> Total km)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0 km (${originName.take(12)})",
                    color = Color(0xFFFF8A3D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", distanceKm / 2.0)} km",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", distanceKm)} km (${destName.take(12)})",
                    color = Color(0xFF2CE5C6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Grafische Darstellung der Zeit-Aufteilung (Reine Fahr-/Gehzeit vs. Aufenthalt bei Stopps)
 * sowie des Kategorien-Mixes und Qualitäts-Scores der aktuellen Tour.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TourTimeAndCategoryAnalyticsCard(
    travelMinutes: Int,
    stayMinutes: Int,
    stops: List<RouteDiscoveryItem>,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val totalMinutes = (travelMinutes + stayMinutes).coerceAtLeast(1)
    val moveRatio = (travelMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0.12f, 1f)
    val movePct = (moveRatio * 100f).roundToInt()
    val stayPct = (100 - movePct).coerceAtLeast(0)

    val avgRating = if (stops.isNotEmpty()) stops.map { it.ratingScore }.average() else 4.8
    val freeCount = stops.count { it.isFree || WayCategory.KOSTENLOSES in it.additionalCategories }

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Time Split Header & Segmented Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage.isGerman) "📊 Zeit- & Erlebnis-Aufteilung" else "📊 Time & Experience Breakdown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                if (stops.isNotEmpty()) {
                    Text(
                        text = "★ ${String.format(java.util.Locale.US, "%.1f", avgRating)} • ${
                            if (appLanguage.isGerman) "$freeCount gratis" else "$freeCount free"
                        }",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B5E)
                    )
                }
            }

            // Segmented Time Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(moveRatio)
                        .background(Color(0xFF006B5E))
                )
                if (stayMinutes > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(Color(0xFFFF8A3D))
                    )
                }
            }

            // Legend for Time Split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006B5E))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (appLanguage.isGerman) {
                            "Bewegung: ${formatDuration(travelMinutes)} ($movePct%)"
                        } else {
                            "Travel: ${formatDuration(travelMinutes)} ($movePct%)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF8A3D))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (appLanguage.isGerman) {
                            "Stopps: ~${formatDuration(stayMinutes)} ($stayPct%)"
                        } else {
                            "Stops: ~${formatDuration(stayMinutes)} ($stayPct%)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }

            // 2. Category Distribution Graphic (if stops are selected)
            if (stops.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFE2E8F0))

                val catCounts = stops.groupingBy { it.category }.eachCount()
                Text(
                    text = if (appLanguage.isGerman) "Kategorien-Mix der Tour:" else "Tour Category Mix:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                // Multi-color Category Proportion Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    catCounts.forEach { (cat, count) ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(count.toFloat())
                                .background(cat.color)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    catCounts.forEach { (cat, count) ->
                        Surface(
                            color = cat.color.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(cat.color)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${cat.localizedLabel(appLanguage)} ($count)",
                                    color = Color(0xFF0F172A),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
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
private fun SavedTourArchiveCard(
    trip: SavedTrip,
    appLanguage: AppLanguage = AppLanguage.DE,
    onOpenTourOnMap: () -> Unit,
    onTransferToGoogleMaps: () -> Unit,
    onOpenStopDetail: (RouteDiscoveryItem) -> Unit,
    onShareTrip: () -> Unit,
    onDeleteTrip: () -> Unit
) {
    val stayMinutes = trip.stops.size * 40
    val totalDayMinutes = trip.trackDurationMinutes + stayMinutes
    val difficulty = computeTourDifficultyLabel(trip.travelMode, trip.trackDistanceKm, trip.elevationGainMeters, appLanguage)
    val energyEstimate = computeTourEnergyEstimate(trip.travelMode, trip.trackDistanceKm, trip.elevationGainMeters, appLanguage)
    val moveRatio = (trip.trackDurationMinutes.toFloat() / totalDayMinutes.coerceAtLeast(1).toFloat()).coerceIn(0.15f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📅 ${GermanTranslator.translateToEnglish(trip.dateLabel).let { if (appLanguage.isGerman) trip.dateLabel else it }}",
                                color = Color(0xFF0369A1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${trip.travelMode.localizedLabel(appLanguage)} • $difficulty",
                                color = Color(0xFF2CE5C6),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = trip.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                IconButton(onClick = onDeleteTrip, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = if (appLanguage.isGerman) "Löschen" else "Delete",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Saved Tour Metrics Box with Vertically Stacked Full-Width Rows
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF006B5E), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Gesamtdistanz:" else "Total distance:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", trip.trackDistanceKm)} km",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Row 2: Elevation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Höhenmeter (Auf / Ab):" else "Elevation (Gain / Loss):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "↑ ${trip.elevationGainMeters} Hm • ↓ ${trip.elevationLossMeters} Hm",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Row 3: Pure travel time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Reine Fahr-/Gehzeit:" else "Pure travel time:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = formatDuration(trip.trackDurationMinutes),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Visual Time Proportion Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(moveRatio)
                                .background(Color(0xFF006B5E))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .background(Color(0xFFFF8A3D))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage.isGerman) {
                                "⏱ Gesamt inkl. ${trip.stops.size} Stopps: ~${formatDuration(totalDayMinutes)}"
                            } else {
                                "⏱ Total incl. ${trip.stops.size} stops: ~${formatDuration(totalDayMinutes)}"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "⚡ ${energyEstimate.first}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF006B5E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Horizontal Scroll of Included Places & Events with their Thumbnail Images
            if (trip.stops.isNotEmpty()) {
                Text(
                    text = if (appLanguage.isGerman) {
                        "Gespeicherte Orte & Events der Tour (${trip.stops.size}):"
                    } else {
                        "Saved places & events of the tour (${trip.stops.size}):"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    trip.stops.forEachIndexed { idx, stop ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenStopDetail(stop) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DiscoveryThumbnail(
                                    item = stop,
                                    size = 52.dp,
                                    cornerRadius = 10.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${idx + 1} • ${stop.category.localizedLabel(appLanguage)}",
                                        color = stop.category.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = stop.displayTitle(appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (stop.detourDistanceKm < 0.15) {
                                            if (appLanguage.isGerman) "km ${stop.routeProgressKm.roundToInt()} • Direkt am Track"
                                            else "km ${stop.routeProgressKm.roundToInt()} • On track"
                                        } else {
                                            "km ${stop.routeProgressKm.roundToInt()} • +${String.format(java.util.Locale.US, "%.1f", stop.detourDistanceKm * 2.0)} km"
                                        },
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    val evDate = stop.displayEventDate(appLanguage)
                                    if (evDate != null) {
                                        Text(
                                            text = "📅 $evDate",
                                            fontSize = 9.sp,
                                            color = Color(0xFFBE185D),
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (trip.notes.isNotBlank()) {
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${if (appLanguage.isGerman) "📝 Notiz" else "📝 Note"}: ${trip.notes}",
                        fontSize = 12.sp,
                        color = Color(0xFF78350F),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            // Action Buttons: "Karte" (switches to Map tab showing ONLY saved events of this tour!) + Google Maps Transfer + Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenTourOnMap,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF006B5E)
                    ),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (appLanguage.isGerman) "Karte" else "Map",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onTransferToGoogleMaps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Google Maps",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onShareTrip)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (appLanguage.isGerman) "Teilen" else "Share",
                            tint = Color(0xFF006B5E),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun computeTourDifficultyLabel(
    mode: TravelMode,
    distanceKm: Double,
    elevationGain: Int,
    appLanguage: AppLanguage = AppLanguage.DE
): String {
    val isDe = appLanguage.isGerman
    return when (mode) {
        TravelMode.HIKE -> when {
            elevationGain > 750 || distanceKm > 22.0 -> if (isDe) "Anspruchsvoll / Bergtour" else "Challenging / Alpine Tour"
            elevationGain > 320 || distanceKm > 12.0 -> if (isDe) "Moderat / Aktiv" else "Moderate / Active"
            else -> if (isDe) "Leichte Genuss-Wanderung" else "Easy Scenic Hike"
        }
        TravelMode.BIKE -> when {
            elevationGain > 900 || distanceKm > 75.0 -> if (isDe) "Sportliche Rad-Etappe" else "Athletic Cycling Stage"
            elevationGain > 400 || distanceKm > 38.0 -> if (isDe) "Moderat / Panorama-Radtour" else "Moderate / Scenic Bike Tour"
            else -> if (isDe) "Entspannte Genuss-Radtour" else "Relaxed Leisure Bike Ride"
        }
        TravelMode.CAR -> when {
            distanceKm > 260.0 -> if (isDe) "Tages-Roadtrip" else "Full-Day Roadtrip"
            elevationGain > 800 -> if (isDe) "Alpen- / Panorama-Passfahrt" else "Alpine / Scenic Pass Drive"
            else -> if (isDe) "Komfort-Entdeckertour" else "Comfort Explorer Tour"
        }
    }
}

private fun computeTourEnergyEstimate(
    mode: TravelMode,
    distanceKm: Double,
    elevationGain: Int,
    appLanguage: AppLanguage = AppLanguage.DE
): Pair<String, String> {
    val isDe = appLanguage.isGerman
    return when (mode) {
        TravelMode.HIKE -> {
            val kcal = (distanceKm * 52.0 + elevationGain * 0.65).roundToInt()
            "~$kcal kcal" to (if (isDe) "Wander-Energie" else "Hike Energy")
        }
        TravelMode.BIKE -> {
            val kcal = (distanceKm * 26.0 + elevationGain * 0.55).roundToInt()
            "~$kcal kcal" to (if (isDe) "Rad-Energie" else "Bike Energy")
        }
        TravelMode.CAR -> {
            val liters = distanceKm * 0.066
            val kwh = distanceKm * 0.175
            "~${String.format(java.util.Locale.US, "%.1f", liters)} L / ${String.format(java.util.Locale.US, "%.1f", kwh)} kWh" to (if (isDe) "Richtverbrauch" else "Est. Fuel / Energy")
        }
    }
}

private fun shareTripItinerary(
    context: Context,
    title: String,
    originName: String,
    destName: String,
    trackKm: Double,
    elevationGain: Int,
    elevationLoss: Int,
    durationMin: Int,
    stops: List<RouteDiscoveryItem>,
    notes: String,
    appLanguage: AppLanguage = AppLanguage.DE
) {
    val totalDayMin = durationMin + stops.size * 40
    val isDe = appLanguage.isGerman
    val text = buildString {
        appendLine("🧭 SeizeTheWay ${if (isDe) "Tour & Etappenplan" else "Tour & Itinerary"}: $title")
        appendLine("📍 ${if (isDe) "Strecke" else "Route"}: $originName → $destName")
        appendLine("📊 ${if (isDe) "Distanz" else "Distance"}: ${String.format(java.util.Locale.US, "%.1f", trackKm)} km • ${if (isDe) "Höhenmeter" else "Elevation"}: ↑$elevationGain / ↓$elevationLoss Hm")
        appendLine("⏱ ${if (isDe) "Reine Zeit" else "Travel time"}: ${formatDuration(durationMin)} • ${if (isDe) "Gesamtdauer inkl. Stopps" else "Total incl. stops"}: ~${formatDuration(totalDayMin)}")
        appendLine()
        if (stops.isEmpty()) {
            appendLine(if (isDe) "Direkter Track ohne Zwischenstopps." else "Direct track without stops.")
        } else {
            appendLine("🌟 ${if (isDe) "Integrierte Zwischenstopps & Events" else "Included Stops & Events"} (${stops.size}):")
            stops.forEachIndexed { idx, s ->
                val detourText = if (s.detourDistanceKm < 0.15) {
                    if (isDe) "Direkt am Track" else "Directly on track"
                } else {
                    "+${String.format(java.util.Locale.US, "%.1f", s.detourDistanceKm * 2.0)} km ${if (isDe) "Abstecher" else "detour"}"
                }
                appendLine("${idx + 1}. [${s.category.localizedLabel(appLanguage)}] ${s.displayTitle(appLanguage)} (km ${s.routeProgressKm.roundToInt()}, $detourText)")
                s.displayEventDate(appLanguage)?.let { appendLine("   📅 $it") }
            }
        }
        if (notes.isNotBlank()) {
            appendLine()
            appendLine("📝 ${if (isDe) "Notizen" else "Notes"}: $notes")
        }
    }
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SeizeTheWay Tour: $title")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, if (isDe) "Tour teilen" else "Share tour"))
    } catch (_: Exception) {
    }
}
