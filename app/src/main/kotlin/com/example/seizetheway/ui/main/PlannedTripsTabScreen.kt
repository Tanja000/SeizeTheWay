package com.example.seizetheway.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seizetheway.model.*
import kotlin.math.roundToInt

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
        // 1. Current Active Tour Card (Structured similarly to saved tours, but distinct as LIVE active tour!)
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
                            title = "${r.origin.name} → ${r.destination.name} (${r.travelMode.label})",
                            originName = r.origin.name,
                            destName = r.destination.name,
                            trackKm = r.distanceKm,
                            elevationGain = r.elevationGainMeters,
                            elevationLoss = r.elevationLossMeters,
                            durationMin = r.durationMinutes,
                            stops = state.plannedStops,
                            notes = state.tripNotesDraft
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
                Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = Color(0xFF006B5E),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Gespeicherte Touren (${state.savedTrips.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Archivierte Touren mit Distanz, Höhenmetern, Events & Wechsel zur Karte",
                        fontSize = 11.sp,
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
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
                            text = "Noch keine Tour gespeichert",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Speichere deinen aktuellen Track inklusive Stopps & Events oben über 'Als Tour speichern' oder direkt im Tab 'Karte'.",
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
                            notes = trip.notes
                        )
                    },
                    onDeleteTrip = { onDeleteSavedTrip(trip.id) }
                )
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
        }
    }
}

/**
 * Die aktuelle Tour: optisch und strukturell sehr ähnlich wie die gespeicherten Touren
 * (SavedTourArchiveCard), aber klar unterscheidbar als AKTIVE LIVE-TOUR (grüner Live-Badge,
 * eleganter Rahmen, Live-Buttons wie "Optimaler Mix", "Als Tour speichern" & Löschoptionen für Stopps).
 */
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
    var showOptimalMixInfo by remember { mutableStateOf(false) }

    val stayMinutes = plannedStops.size * 40
    val totalDayMinutes = (route?.durationMinutes ?: 0) + stayMinutes
    val difficulty = route?.let { computeTourDifficultyLabel(it.travelMode, it.distanceKm, it.elevationGainMeters) } ?: "Geplante Tour"
    val energyEstimate = route?.let { computeTourEnergyEstimate(it.travelMode, it.distanceKm, it.elevationGainMeters) } ?: ("-- kcal" to "")
    val moveRatio = if (totalDayMinutes > 0) {
        ((route?.durationMinutes ?: 0).toFloat() / totalDayMinutes.toFloat()).coerceIn(0.15f, 1f)
    } else 1f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFF006B5E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Live-Badge, Mode/Difficulty, Optimaler Mix
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
                                    text = "AKTIVE TOUR (LIVE)",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (route != null) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${route.travelMode.label} • $difficulty",
                                    color = Color(0xFF2CE5C6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = route?.let { "${it.origin.name} → ${it.destination.name}" } ?: "Aktuelle Route",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Optimaler Mix Button
                FilledTonalButton(
                    onClick = onAutoCurate,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFF92400E)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Optimaler Mix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Info-Banner for "Optimaler Mix"
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOptimalMixInfo = !showOptimalMixInfo }
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ Was bedeutet »Optimaler Mix«?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (showOptimalMixInfo) "Einklappen ▲" else "Erklärung ▼",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                    AnimatedVisibility(visible = showOptimalMixInfo) {
                        Text(
                            text = "Der »Optimale Mix« wählt automatisch bis zu 4 harmonisch verteilte Highlights:\n" +
                                    "• ~25 %: Natur, Aussichtspunkt oder Wanderung\n" +
                                    "• ~50 %: Café, Kaffeerösterei oder Restaurant\n" +
                                    "• ~75 %: Historisches oder kulturelles Highlight\n" +
                                    "• Plus: 1 passendes Tagesevent entlang der Route",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Metrics Box (Identical clean layout to SavedTourArchiveCard, but with active extra-km info)
            if (route != null) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF006B5E), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${String.format("%.1f", route.distanceKm)} km",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (totalExtraKm > 0.05) {
                                    Text(
                                        text = " (+${String.format("%.1f", totalExtraKm)})",
                                        fontSize = 10.sp,
                                        color = Color(0xFF006B5E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terrain, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "↑${route.elevationGainMeters} / ↓${route.elevationLossMeters} Hm",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = formatDuration(route.durationMinutes),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        // Visual Time Breakdown Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
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
                                text = "⏱ Gesamt inkl. ${plannedStops.size} Stopps: ~${formatDuration(totalDayMinutes)}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "⚡ ${energyEstimate.first}",
                                fontSize = 11.sp,
                                color = Color(0xFF006B5E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Places & Events Horizontal Scroll (Matching SavedTourArchiveCard, but with remove icon on active items)
            if (plannedStops.isNotEmpty()) {
                Text(
                    text = "Ausgewählte Orte & Events der Tour (${plannedStops.size}):",
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
                    plannedStops.forEach { stop ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .width(225.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenItemDetail(stop) }
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
                                        text = stop.category.label,
                                        color = stop.category.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stop.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "km ${stop.routeProgressKm.roundToInt()} • +${String.format("%.1f", stop.detourDistanceKm)} km",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    stop.eventDateText?.let { ev ->
                                        Text(
                                            text = "📅 $ev",
                                            fontSize = 9.sp,
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
                                        contentDescription = "Aus Tour entfernen",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add More Places Chip
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .width(130.dp)
                            .height(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGoToDiscoveriesTab() }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = Color(0xFF006B5E), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(3.dp))
                            Text("+ Mehr Orte", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006B5E))
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
                                text = "Direkter Track ohne Zwischenstopps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Wähle Orte im Tab 'Ergebnisse' oder klicke auf 'Optimaler Mix'.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        OutlinedButton(
                            onClick = onGoToDiscoveriesTab,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Orte wählen", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Notes input
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Touren-Notizen, Packliste & Event-Tickets") },
                leadingIcon = {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF006B5E))
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Primary Action Buttons Row (Save Tour, Google Maps Transfer, Share)
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
                            .height(40.dp)
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
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
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (appLanguage.isGerman) "Tour speichern" else "Save Tour",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
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
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Google Maps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onShareTrip, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Teilen", tint = Color(0xFF006B5E))
                }
            }
        }
    }
}

@Composable
private fun SavedTourArchiveCard(
    trip: SavedTrip,
    onOpenTourOnMap: () -> Unit,
    onTransferToGoogleMaps: () -> Unit,
    onOpenStopDetail: (RouteDiscoveryItem) -> Unit,
    onShareTrip: () -> Unit,
    onDeleteTrip: () -> Unit
) {
    val stayMinutes = trip.stops.size * 40
    val totalDayMinutes = trip.trackDurationMinutes + stayMinutes
    val difficulty = computeTourDifficultyLabel(trip.travelMode, trip.trackDistanceKm, trip.elevationGainMeters)
    val energyEstimate = computeTourEnergyEstimate(trip.travelMode, trip.trackDistanceKm, trip.elevationGainMeters)
    val moveRatio = (trip.trackDurationMinutes.toFloat() / totalDayMinutes.coerceAtLeast(1).toFloat()).coerceIn(0.15f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = "📅 ${trip.dateLabel}",
                                color = Color(0xFF0369A1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${trip.travelMode.label} • $difficulty",
                                color = Color(0xFF2CE5C6),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Löschen", tint = Color(0xFF94A3B8))
                }
            }

            // Saved Tour Metrics Box with Visual Time Breakdown Bar
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF006B5E), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", trip.trackDistanceKm)} km",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "↑${trip.elevationGainMeters} / ↓${trip.elevationLossMeters} Hm",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = formatDuration(trip.trackDurationMinutes),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Visual Time Proportion Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
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
                            text = "⏱ Gesamt inkl. ${trip.stops.size} Stopps: ~${formatDuration(totalDayMinutes)}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "⚡ ${energyEstimate.first}",
                            fontSize = 11.sp,
                            color = Color(0xFF006B5E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Horizontal Scroll of Included Places & Events with their Thumbnail Images
            if (trip.stops.isNotEmpty()) {
                Text(
                    text = "Gespeicherte Orte & Events der Tour (${trip.stops.size}):",
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
                    trip.stops.forEach { stop ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .width(215.dp)
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
                                        text = stop.category.label,
                                        color = stop.category.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stop.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "km ${stop.routeProgressKm.roundToInt()} • +${String.format("%.1f", stop.detourDistanceKm)} km",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    stop.eventDateText?.let { ev ->
                                        Text(
                                            text = "📅 $ev",
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
                Text(
                    text = "📝 Notiz: ${trip.notes}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF006B5E)
                    ),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Karte", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTransferToGoogleMaps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Google Maps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onShareTrip, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Teilen", tint = Color(0xFF006B5E))
                }
            }
        }
    }
}

private fun computeTourDifficultyLabel(mode: TravelMode, distanceKm: Double, elevationGain: Int): String {
    return when (mode) {
        TravelMode.HIKE -> when {
            elevationGain > 750 || distanceKm > 22.0 -> "Anspruchsvoll / Bergtour"
            elevationGain > 320 || distanceKm > 12.0 -> "Moderat / Aktiv"
            else -> "Leichte Genuss-Wanderung"
        }
        TravelMode.BIKE -> when {
            elevationGain > 900 || distanceKm > 75.0 -> "Sportliche Rad-Etappe"
            elevationGain > 400 || distanceKm > 38.0 -> "Moderat / Panorama-Radtour"
            else -> "Entspannte Genuss-Radtour"
        }
        TravelMode.CAR -> when {
            distanceKm > 260.0 -> "Tages-Roadtrip"
            elevationGain > 800 -> "Alpen- / Panorama-Passfahrt"
            else -> "Komfort-Entdeckertour"
        }
    }
}

private fun computeTourEnergyEstimate(mode: TravelMode, distanceKm: Double, elevationGain: Int): Pair<String, String> {
    return when (mode) {
        TravelMode.HIKE -> {
            val kcal = (distanceKm * 52.0 + elevationGain * 0.65).roundToInt()
            "~$kcal kcal" to "Wander-Energie"
        }
        TravelMode.BIKE -> {
            val kcal = (distanceKm * 26.0 + elevationGain * 0.55).roundToInt()
            "~$kcal kcal" to "Rad-Energie"
        }
        TravelMode.CAR -> {
            val liters = distanceKm * 0.066
            val kwh = distanceKm * 0.175
            "~${String.format("%.1f", liters)} L / ${String.format("%.1f", kwh)} kWh" to "Richtverbrauch"
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
    notes: String
) {
    val totalDayMin = durationMin + stops.size * 40
    val text = buildString {
        appendLine("🧭 SeizeTheWay Tour & Etappenplan: $title")
        appendLine("📍 Strecke: $originName → $destName")
        appendLine("📊 Distanz: ${String.format("%.1f", trackKm)} km • Höhenmeter: ↑$elevationGain / ↓$elevationLoss Hm")
        appendLine("⏱ Reine Zeit: ${formatDuration(durationMin)} • Gesamtdauer inkl. Stopps: ~${formatDuration(totalDayMin)}")
        appendLine()
        if (stops.isEmpty()) {
            appendLine("Direkter Track ohne Zwischenstopps.")
        } else {
            appendLine("🌟 Integrierte Zwischenstopps & Events (${stops.size}):")
            stops.forEachIndexed { idx, s ->
                appendLine("${idx + 1}. [${s.category.label}] ${s.title} (bei km ${s.routeProgressKm.roundToInt()}, +${String.format("%.1f", s.detourDistanceKm)} km Abstecher)")
                s.eventDateText?.let { appendLine("   📅 $it") }
            }
        }
        if (notes.isNotBlank()) {
            appendLine()
            appendLine("📝 Notizen: $notes")
        }
    }
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SeizeTheWay Tour: $title")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Tour teilen"))
    } catch (_: Exception) {
    }
}
