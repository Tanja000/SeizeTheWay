package com.example.seizetheway.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.seizetheway.R
import com.example.seizetheway.model.*
import kotlin.math.roundToInt

private val SUGGESTED_KEYWORDS = listOf(
    "See", "Burg", "Wasserfall", "Aussicht", "Kostenlos",
    "Klamm", "Kaffee", "Biergarten", "Museum", "Konzert", "Schloss", "Moor"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SeizeTheWayViewModel = viewModel { SeizeTheWayViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showSaveTripDialog by remember { mutableStateOf(false) }

    // Standorterkennung als Default für den Ausgangspunkt
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectUserLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.detectUserLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F7FB),
        topBar = {
            SeizeTheWayTopBar(
                savedTripsCount = state.savedTrips.size,
                plannedStopsCount = state.plannedStops.size,
                activeApisCount = state.apiReports.count { it.active },
                appLanguage = state.appLanguage,
                onToggleLanguage = viewModel::toggleAppLanguage,
                onOpenPlannedTab = { viewModel.selectTab(AppTab.TRIPS) },
                onOpenApiDialog = { viewModel.setShowApiDialog(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.activeTab == AppTab.SEARCH,
                    onClick = { viewModel.selectTab(AppTab.SEARCH) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text(AppTab.SEARCH.localized(state.appLanguage), fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = state.activeTab == AppTab.DISCOVERIES,
                    onClick = { viewModel.selectTab(AppTab.DISCOVERIES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.displayedItems.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF006B5E)) {
                                        Text("${state.displayedItems.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null)
                        }
                    },
                    label = { Text(AppTab.DISCOVERIES.localized(state.appLanguage), fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = state.activeTab == AppTab.MAP,
                    onClick = { viewModel.selectTab(AppTab.MAP) },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text(AppTab.MAP.localized(state.appLanguage), fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = state.activeTab == AppTab.TRIPS,
                    onClick = { viewModel.selectTab(AppTab.TRIPS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.savedTrips.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF006B5E)) {
                                        Text("${state.savedTrips.size}", color = Color.White)
                                    }
                                } else if (state.plannedStops.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFFF8A3D)) {
                                        Text("${state.plannedStops.size}", color = Color(0xFF081224))
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CardTravel, contentDescription = null)
                        }
                    },
                    label = { Text(AppTab.TRIPS.localized(state.appLanguage), fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                AppTab.SEARCH -> {
                    SearchAndFilterTabContent(
                        state = state,
                        onOriginChange = viewModel::updateOrigin,
                        onConfirmOriginSuggestion = viewModel::confirmOriginSuggestion,
                        onConfirmOriginManual = viewModel::confirmOriginManual,
                        onDestinationChange = viewModel::updateDestination,
                        onConfirmDestinationSuggestion = viewModel::confirmDestinationSuggestion,
                        onConfirmDestinationManual = viewModel::confirmDestinationManual,
                        onDetectLocation = { viewModel.detectUserLocation(context) },
                        onSwapLocations = viewModel::swapOriginAndDestination,
                        onDetourChange = viewModel::updateMaxDetourKm,
                        onTravelModeChange = viewModel::updateTravelMode,
                        onShowAllCategoriesChange = viewModel::setShowAllCategories,
                        onToggleCategory = viewModel::toggleCategory,
                        onKeywordsChange = viewModel::updateKeywords,
                        onToggleKeywordChip = viewModel::toggleKeywordChip,
                        onSelectPreset = viewModel::applyPreset,
                        onExecuteSearch = {
                            focusManager.clearFocus()
                            viewModel.executeRouteSearch(switchToResultsTab = true)
                        },
                        onJumpToTab = viewModel::selectTab
                    )
                }

                AppTab.DISCOVERIES -> {
                    DiscoveriesResultsTabContent(
                        state = state,
                        onSelectCategorySubTab = viewModel::selectResultCategoryTab,
                        onSortOrderChange = viewModel::updateSortOrder,
                        onOpenApiDialog = { viewModel.setShowApiDialog(true) },
                        onResetFilters = {
                            viewModel.setShowAllCategories(true)
                            viewModel.updateKeywords("")
                            viewModel.updateMaxDetourKm(20f)
                        },
                        onCardClick = { item -> viewModel.selectDetailItem(item) },
                        onShowItemOnMap = { item -> viewModel.openItemOnMapTab(item) },
                        onTogglePlanned = viewModel::togglePlannedStop,
                        onAutoCurateTrip = {
                            viewModel.autoCurateIdealTripStops()
                            viewModel.selectTab(AppTab.TRIPS)
                        }
                    )
                }

                AppTab.MAP -> {
                    DedicatedMapTabScreen(
                        route = state.route,
                        items = state.mapDisplayedItems,
                        allCorridorCount = state.displayedItems.size,
                        inTrackCount = state.plannedStops.size,
                        mapDisplayFilter = state.mapDisplayFilter,
                        onMapDisplayFilterChange = viewModel::setMapDisplayFilter,
                        plannedStopIds = state.plannedStopIds,
                        highlightedItemId = state.highlightedMapItemId,
                        maxDetourKm = state.maxDetourKm,
                        isRecalculatingTrack = state.isRecalculatingTrack,
                        isAlreadySaved = state.isCurrentTourAlreadySaved,
                        appLanguage = state.appLanguage,
                        onSelectItem = { item -> viewModel.highlightMapItem(item.id) },
                        onOpenFullDetailSheet = { item -> viewModel.selectDetailItem(item) },
                        onTogglePlanned = viewModel::togglePlannedStop,
                        onLaunchExternalNavigation = {
                            state.route?.let { r ->
                                launchTrackInGoogleMaps(
                                    context = context,
                                    origin = r.origin,
                                    destination = r.destination,
                                    waypoints = state.plannedStops,
                                    travelMode = r.travelMode
                                )
                            }
                        },
                        onOpenSaveDialog = { showSaveTripDialog = true }
                    )
                }

                AppTab.TRIPS -> {
                    PlannedTripsTabScreen(
                        state = state,
                        onAutoCurateStops = viewModel::autoCurateIdealTripStops,
                        onRemovePlannedStop = viewModel::togglePlannedStop,
                        onOpenItemDetail = { item -> viewModel.selectDetailItem(item) },
                        onShowItemOnMap = { item -> viewModel.openItemOnMapTab(item) },
                        onSaveCurrentTrip = viewModel::saveCurrentTrip,
                        onLoadSavedTrip = viewModel::loadSavedTripIntoActiveRoute,
                        onDeleteSavedTrip = viewModel::deleteSavedTrip,
                        onUpdateNotes = viewModel::updateTripNotesDraft,
                        onTransferTrackToGoogleMaps = { origin, dest, stops, mode ->
                            launchTrackInGoogleMaps(
                                context = context,
                                origin = origin,
                                destination = dest,
                                waypoints = stops,
                                travelMode = mode
                            )
                        },
                        onGoToDiscoveriesTab = { viewModel.selectTab(AppTab.DISCOVERIES) },
                        onOpenSaveDialog = { showSaveTripDialog = true }
                    )
                }
            }
        }
    }

    // Save Tour Dialog (Can be opened from Tab "Touren" and from Tab "Karte")
    if (showSaveTripDialog) {
        val defaultName = state.route?.let {
            "${it.origin.name} → ${it.destination.name} (${it.travelMode.label})"
        } ?: "Meine SeizeTheWay Tour"
        var titleInput by remember { mutableStateOf(defaultName) }
        var dateInput by remember { mutableStateOf("Dieses Wochenende") }

        AlertDialog(
            onDismissRequest = { showSaveTripDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color(0xFF006B5E))
                    Spacer(Modifier.width(8.dp))
                    Text("Tour speichern", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Speichert den aktuellen Track inklusive aller ${state.plannedStops.size} ausgewählten Orte & Events.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Name der Tour") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Zeitraum / Datum (z. B. Samstag, 14. Mai)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentTrip(titleInput, dateInput)
                        showSaveTripDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B5E))
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTripDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Full Detail Modal Sheet for Selected Discovery Item
    state.selectedDetailItem?.let { detailItem ->
        DiscoveryDetailBottomSheet(
            item = detailItem,
            isPlanned = detailItem.id in state.plannedStopIds,
            onDismiss = { viewModel.selectDetailItem(null) },
            onTogglePlanned = { viewModel.togglePlannedStop(detailItem.id) },
            onShowOnMap = {
                viewModel.selectDetailItem(null)
                viewModel.openItemOnMapTab(detailItem)
            },
            onOpenInMaps = { openPointInMaps(context, detailItem.point, detailItem.title) },
            onOpenUrl = { url -> openWebUrl(context, url) }
        )
    }

    // API Status & Optional Keys Hub Dialog
    if (state.showApiDialog) {
        ApiHubDialog(
            apiKeys = state.apiKeys,
            apiReports = state.apiReports,
            onDismiss = { viewModel.setShowApiDialog(false) },
            onSaveKeys = viewModel::saveApiKeys
        )
    }
}

@Composable
private fun SeizeTheWayTopBar(
    savedTripsCount: Int,
    plannedStopsCount: Int,
    activeApisCount: Int,
    appLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onOpenPlannedTab: () -> Unit,
    onOpenApiDialog: () -> Unit
) {
    Surface(
        color = Color(0xFF081224),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "SeizeTheWay Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(1.dp, Color(0xFF2CE5C6).copy(alpha = 0.5f), RoundedCornerShape(11.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SeizeTheWay",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
                Text(
                    text = if (appLanguage.isGerman) {
                        "Tracks für Auto, Rad & Wandern inkl. Stopps & Höhenmeter"
                    } else {
                        "Car, Bike & Hike Tracks with Stops & Elevation Profile"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Language Switcher (DE | EN)
            Surface(
                color = Color(0xFF152946),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2CE5C6).copy(alpha = 0.4f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggleLanguage)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (appLanguage.isGerman) Color(0xFF2CE5C6) else Color(0xFF64748B)
                    )
                    Text(
                        text = " | ",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!appLanguage.isGerman) Color(0xFF2CE5C6) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Saved Tours Count Chip (Fixes the flag discrepancy!)
            if (savedTripsCount > 0) {
                Surface(
                    color = Color(0xFF006B5E),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenPlannedTab)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Gespeicherte Touren",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "$savedTripsCount",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
            }

            // Planned Stops on Current Track Chip
            if (plannedStopsCount > 0) {
                Surface(
                    color = Color(0xFFFF8A3D),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenPlannedTab)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Stopps im Track",
                            tint = Color(0xFF081224),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "$plannedStopsCount",
                            color = Color(0xFF081224),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
            }

            // API Hub Chip
            Surface(
                color = Color(0xFF152946),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenApiDialog)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "APIs",
                        tint = Color(0xFF2CE5C6),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "$activeApisCount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterTabContent(
    state: SeizeTheWayUiState,
    onOriginChange: (String) -> Unit,
    onConfirmOriginSuggestion: (GeoPoint) -> Unit,
    onConfirmOriginManual: () -> Unit,
    onDestinationChange: (String) -> Unit,
    onConfirmDestinationSuggestion: (GeoPoint) -> Unit,
    onConfirmDestinationManual: () -> Unit,
    onDetectLocation: () -> Unit,
    onSwapLocations: () -> Unit,
    onDetourChange: (Float) -> Unit,
    onTravelModeChange: (TravelMode) -> Unit,
    onShowAllCategoriesChange: (Boolean) -> Unit,
    onToggleCategory: (WayCategory) -> Unit,
    onKeywordsChange: (String) -> Unit,
    onToggleKeywordChip: (String) -> Unit,
    onSelectPreset: (RoutePreset) -> Unit,
    onExecuteSearch: () -> Unit,
    onJumpToTab: (AppTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Overview Strip of Current Track (km, Höhenmeter, Reisedauer & Stopps)
        state.route?.let { r ->
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2942)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${r.travelMode.trackTypeLabel}: ${r.origin.name} → ${r.destination.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${String.format("%.1f", r.distanceKm)} km • ↑${r.elevationGainMeters} / ↓${r.elevationLossMeters} Hm • Dauer: ${formatDuration(r.durationMinutes)} • ${state.plannedStops.size} Stopps im Track",
                                    color = Color(0xFF2CE5C6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onJumpToTab(AppTab.DISCOVERIES) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2CE5C6),
                                    contentColor = Color(0xFF081224)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("${state.displayedItems.size} Ergebnisse", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { onJumpToTab(AppTab.MAP) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1E3A5F),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Track auf Karte", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Main Search & Filter Configuration Card
        item {
            RouteSearchAndFilterCard(
                state = state,
                onOriginChange = onOriginChange,
                onConfirmOriginSuggestion = onConfirmOriginSuggestion,
                onConfirmOriginManual = onConfirmOriginManual,
                onDestinationChange = onDestinationChange,
                onConfirmDestinationSuggestion = onConfirmDestinationSuggestion,
                onConfirmDestinationManual = onConfirmDestinationManual,
                onDetectLocation = onDetectLocation,
                onSwapLocations = onSwapLocations,
                onDetourChange = onDetourChange,
                onTravelModeChange = onTravelModeChange,
                onShowAllCategoriesChange = onShowAllCategoriesChange,
                onToggleCategory = onToggleCategory,
                onKeywordsChange = onKeywordsChange,
                onToggleKeywordChip = onToggleKeywordChip,
                onSelectPreset = onSelectPreset,
                onExecuteSearch = onExecuteSearch
            )
        }

        if (state.isLoading) {
            item {
                LoadingProgressBanner(maxDetourKm = state.maxDetourKm, travelMode = state.travelMode)
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DiscoveriesResultsTabContent(
    state: SeizeTheWayUiState,
    onSelectCategorySubTab: (WayCategory?) -> Unit,
    onSortOrderChange: (DiscoverySortOrder) -> Unit,
    onOpenApiDialog: () -> Unit,
    onResetFilters: () -> Unit,
    onCardClick: (RouteDiscoveryItem) -> Unit,
    onShowItemOnMap: (RouteDiscoveryItem) -> Unit,
    onTogglePlanned: (String) -> Unit,
    onAutoCurateTrip: () -> Unit
) {
    val itemsToShow = state.tabFilteredItems

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-Tab Strip for Categories within the Results Tab
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allSelected = state.activeResultCategoryTab == null
                Surface(
                    color = if (allSelected) Color(0xFF006B5E) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectCategorySubTab(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = if (allSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Alle (${state.displayedItems.size})",
                            color = if (allSelected) Color.White else Color(0xFF1E293B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                WayCategory.entries.forEach { cat ->
                    val count = state.displayedItems.count {
                        it.category == cat || cat in it.additionalCategories
                    }
                    val isSubSelected = state.activeResultCategoryTab == cat
                    Surface(
                        color = if (isSubSelected) cat.color else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCategorySubTab(cat) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(cat),
                                contentDescription = null,
                                tint = if (isSubSelected) Color.White else cat.color,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "${cat.label} ($count)",
                                color = if (isSubSelected) Color.White else Color(0xFF1E293B),
                                fontSize = 12.sp,
                                fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                item {
                    LoadingProgressBanner(maxDetourKm = state.maxDetourKm, travelMode = state.travelMode)
                }
            }

            item {
                ResultsControlHeader(
                    state = state,
                    shownCount = itemsToShow.size,
                    onSortOrderChange = onSortOrderChange,
                    onOpenApiDialog = onOpenApiDialog,
                    onAutoCurateTrip = onAutoCurateTrip
                )
            }

            if (itemsToShow.isEmpty() && !state.isLoading) {
                item {
                    EmptyDiscoveryStateCard(
                        maxDetourKm = state.maxDetourKm,
                        hasKeywords = state.keywordsQuery.isNotBlank(),
                        onShowAllAndExpandRadius = {
                            onSelectCategorySubTab(null)
                            onResetFilters()
                        }
                    )
                }
            } else {
                items(itemsToShow, key = { it.id }) { item ->
                    DiscoveryStopCard(
                        item = item,
                        routeTotalKm = state.route?.distanceKm ?: 100.0,
                        isPlanned = item.id in state.plannedStopIds,
                        isHighlightedOnMap = item.id == state.highlightedMapItemId,
                        onCardClick = { onCardClick(item) },
                        onFocusOnMap = { onShowItemOnMap(item) },
                        onTogglePlanned = { onTogglePlanned(item.id) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LoadingProgressBanner(maxDetourKm: Float, travelMode: TravelMode) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                strokeWidth = 3.dp,
                color = Color(0xFF006B5E)
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "${travelMode.trackTypeLabel} & Höhenprofil werden berechnet…",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Abfrage von OSM-Track (${travelMode.label}), Höhenmetern, Overpass & Wikipedia (±${maxDetourKm.roundToInt()} km)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteSearchAndFilterCard(
    state: SeizeTheWayUiState,
    onOriginChange: (String) -> Unit,
    onConfirmOriginSuggestion: (GeoPoint) -> Unit,
    onConfirmOriginManual: () -> Unit,
    onDestinationChange: (String) -> Unit,
    onConfirmDestinationSuggestion: (GeoPoint) -> Unit,
    onConfirmDestinationManual: () -> Unit,
    onDetectLocation: () -> Unit,
    onSwapLocations: () -> Unit,
    onDetourChange: (Float) -> Unit,
    onTravelModeChange: (TravelMode) -> Unit,
    onShowAllCategoriesChange: (Boolean) -> Unit,
    onToggleCategory: (WayCategory) -> Unit,
    onKeywordsChange: (String) -> Unit,
    onToggleKeywordChip: (String) -> Unit,
    onSelectPreset: (RoutePreset) -> Unit,
    onExecuteSearch: () -> Unit
) {
    val modePresets = presetsForTravelMode(state.travelMode)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color(0xFF006B5E),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Track- & Entdecker-Konfigurator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Fortbewegungsart, Start, Ziel, max. Abweichung & Interessen",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 1. Travel Mode Selector (Auto, Radfahren, Wandern) -> selects dedicated track profiles & presets!
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Track-Typ wählen (Auto, Radfahren, Wandern):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TravelMode.entries.forEach { mode ->
                        val selected = state.travelMode == mode
                        val icon = when (mode) {
                            TravelMode.CAR -> Icons.Default.DirectionsCar
                            TravelMode.BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
                            TravelMode.HIKE -> Icons.AutoMirrored.Filled.DirectionsWalk
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onTravelModeChange(mode) },
                            label = {
                                Text(
                                    text = mode.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Mode-Specific Recommended Tracks Strip
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Empfohlene ${state.travelMode.label}-Tracks:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    modePresets.forEach { preset ->
                        val isSelected = state.originQuery.equals(preset.originName, ignoreCase = true) &&
                                state.destinationQuery.equals(preset.destinationName, ignoreCase = true)
                        Surface(
                            color = if (isSelected) Color(0xFF006B5E) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPreset(preset) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = preset.title,
                                    color = if (isSelected) Color(0xFF2CE5C6) else Color(0xFF006B5E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preset.originName} → ${preset.destinationName}",
                                    color = if (isSelected) Color.White else Color(0xFF1E293B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Origin & Destination Inputs with Autocomplete Suggestions & Confirmation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LocationAutocompleteField(
                        label = if (state.appLanguage.isGerman) "Ausgangspunkt (Standard: Standort)" else "Origin (Default: Current Location)",
                        query = state.originQuery,
                        placeholder = if (state.appLanguage.isGerman) "z. B. München, Berlin, Bonn…" else "e.g. Munich, Berlin, Bonn…",
                        isConfirmed = state.isOriginConfirmed,
                        suggestions = state.originSuggestions,
                        leadingIcon = Icons.Default.TripOrigin,
                        leadingIconTint = Color(0xFFFF7B29),
                        appLanguage = state.appLanguage,
                        onQueryChange = onOriginChange,
                        onSelectSuggestion = onConfirmOriginSuggestion,
                        onConfirmManual = onConfirmOriginManual,
                        onDetectGpsLocation = onDetectLocation,
                        isDetectingLocation = state.isDetectingLocation,
                        imeAction = ImeAction.Next
                    )

                    LocationAutocompleteField(
                        label = if (state.appLanguage.isGerman) "Reiseziel (Zielort / Adresse)" else "Destination (City / Address)",
                        query = state.destinationQuery,
                        placeholder = if (state.appLanguage.isGerman) "z. B. Garmisch-Partenkirchen, Dresden…" else "e.g. Garmisch, Dresden…",
                        isConfirmed = state.isDestinationConfirmed,
                        suggestions = state.destinationSuggestions,
                        leadingIcon = Icons.Default.Place,
                        leadingIconTint = Color(0xFF006B5E),
                        appLanguage = state.appLanguage,
                        onQueryChange = onDestinationChange,
                        onSelectSuggestion = onConfirmDestinationSuggestion,
                        onConfirmManual = onConfirmDestinationManual,
                        imeAction = ImeAction.Search,
                        onImeAction = onExecuteSearch
                    )
                }

                Spacer(Modifier.width(8.dp))

                FilledIconButton(
                    onClick = onSwapLocations,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFE2E8F0),
                        contentColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Start und Ziel tauschen")
                }
            }

            // Maximum Detour Distance Slider & Quick Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.AltRoute,
                            contentDescription = null,
                            tint = Color(0xFF006B5E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Max. Abweichung vom Track",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Surface(
                        color = Color(0xFF006B5E),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "max. ${state.maxDetourKm.roundToInt()} km",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Slider(
                    value = state.maxDetourKm,
                    onValueChange = onDetourChange,
                    valueRange = 1f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF006B5E),
                        activeTrackColor = Color(0xFF006B5E)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(3f, 5f, 10f, 15f, 25f, 40f).forEach { kmPreset ->
                        val active = state.maxDetourKm.roundToInt() == kmPreset.roundToInt()
                        Surface(
                            color = if (active) Color(0xFF006B5E).copy(alpha = 0.14f) else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (active) Color(0xFF006B5E) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDetourChange(kmPreset) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${kmPreset.roundToInt()} km",
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) Color(0xFF006B5E) else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            // Categories Section ("Alles anzeigen" + Individual Categories)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kategorien wählen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        color = if (state.showAllCategories) Color(0xFF006B5E) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onShowAllCategoriesChange(!state.showAllCategories) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.showAllCategories) Icons.Default.DoneAll else Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (state.showAllCategories) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (state.showAllCategories) "Alles anzeigen (Aktiv)" else "Alles anzeigen",
                                color = if (state.showAllCategories) Color.White else Color(0xFF1E293B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WayCategory.entries.forEach { cat ->
                        val isSelected = state.showAllCategories || cat in state.selectedCategories
                        val countInCorridor = state.allCorridorItems.count { item ->
                            item.detourDistanceKm <= state.maxDetourKm + 0.05 &&
                                    (item.category == cat || cat in item.additionalCategories)
                        }
                        Surface(
                            color = if (isSelected) cat.color.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) cat.color else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleCategory(cat) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(cat),
                                    contentDescription = null,
                                    tint = if (isSelected) cat.color else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = cat.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569)
                                )
                                if (countInCorridor > 0) {
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "($countInCorridor)",
                                        fontSize = 11.sp,
                                        color = if (isSelected) cat.color else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Keywords / Stichworte Input + Quick Suggestion Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.keywordsQuery,
                    onValueChange = onKeywordsChange,
                    label = { Text("Stichworte & Interessen (optional)") },
                    placeholder = { Text("z. B. Wasserfall, Burg, Aussicht, Kaffee, Kostenlos…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF006B5E)
                        )
                    },
                    trailingIcon = {
                        if (state.keywordsQuery.isNotBlank()) {
                            IconButton(onClick = { onKeywordsChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Stichworte löschen")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onExecuteSearch() }),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beliebte Stichworte:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    SUGGESTED_KEYWORDS.forEach { kw ->
                        val active = state.keywordsQuery.lowercase().contains(kw.lowercase())
                        Surface(
                            color = if (active) Color(0xFF0F2942) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleKeywordChip(kw) }
                        ) {
                            Text(
                                text = "#$kw",
                                color = if (active) Color(0xFF2CE5C6) else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Primary Action Button
            Button(
                onClick = onExecuteSearch,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006B5E),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.TravelExplore, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.appLanguage.isGerman) {
                        "${state.travelMode.label}-Track berechnen"
                    } else {
                        "Calculate ${state.travelMode.label} Track"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ResultsControlHeader(
    state: SeizeTheWayUiState,
    shownCount: Int,
    onSortOrderChange: (DiscoverySortOrder) -> Unit,
    onOpenApiDialog: () -> Unit,
    onAutoCurateTrip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$shownCount Entdeckungen ${state.activeResultCategoryTab?.let { "in ${it.label}" } ?: "entlang des Tracks"}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                state.route?.let { r ->
                    Text(
                        text = "${String.format("%.1f", r.distanceKm)} km • ↑${r.elevationGainMeters} Hm • ${formatDuration(r.durationMinutes)} (${r.travelMode.label})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF006B5E)
                    )
                }
            }

            FilledTonalButton(
                onClick = onAutoCurateTrip,
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

        // Sorting & API Source Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscoverySortOrder.entries.forEach { order ->
                val active = state.sortOrder == order
                Surface(
                    color = if (active) Color(0xFF0F172A) else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (active) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSortOrderChange(order) }
                ) {
                    Text(
                        text = order.label,
                        color = if (active) Color.White else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }

            Surface(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenApiDialog)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "API-Quellen",
                        color = Color(0xFF0369A1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryStopCard(
    item: RouteDiscoveryItem,
    routeTotalKm: Double,
    isPlanned: Boolean,
    isHighlightedOnMap: Boolean,
    onCardClick: () -> Unit,
    onFocusOnMap: () -> Unit,
    onTogglePlanned: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlightedOnMap) 5.dp else 2.dp),
        border = if (isPlanned) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF8A3D))
        } else if (isHighlightedOnMap) {
            androidx.compose.foundation.BorderStroke(1.5.dp, item.category.color)
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DiscoveryThumbnail(
                    item = item,
                    size = 86.dp,
                    cornerRadius = 14.dp
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = item.category.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = null,
                                    tint = item.category.color,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = item.category.label,
                                    color = item.category.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (item.isFree || WayCategory.KOSTENLOSES in item.additionalCategories) {
                            Surface(
                                color = Color(0xFF06B6D4).copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Kostenlos",
                                    color = Color(0xFF0891B2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            color = item.source.badgeColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = item.source.title,
                                color = item.source.badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = item.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "+${String.format("%.1f", item.detourDistanceKm)} km Abstecher (~${item.extraDetourMinutes} Min.)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B5E)
                    )
                }
            }

            Text(
                text = item.description,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (item.eventDateText != null || item.openingHours != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (item.eventDateText != null) Icons.Default.Event else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = item.eventDateText ?: "Öffnungszeiten: ${item.openingHours}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBE185D)
                    )
                }
            }

            if (item.matchedKeywords.isNotEmpty() || item.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.matchedKeywords.forEach { matchedKw ->
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✓ Stichwort: $matchedKw",
                                color = Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    item.tags.take(4).forEach { tag ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bei km ${item.routeProgressKm.roundToInt()} von ${routeTotalKm.roundToInt()} km",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.routeProgressFraction },
                        color = item.category.color,
                        trackColor = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onFocusOnMap,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Karte", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPlanned) "Im Track" else "+ In Track",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDiscoveryStateCard(
    maxDetourKm: Float,
    hasKeywords: Boolean,
    onShowAllAndExpandRadius: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExploreOff,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Keine Treffer in diesem Tab / Filter",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = if (hasKeywords) {
                    "Für deine eingegebenen Stichworte wurden im Radius von ±${maxDetourKm.roundToInt()} km keine direkten Treffer gefunden. Erweitere den Radius oder zeige alle Kategorien an."
                } else {
                    "Erhöhe die maximale Abweichung von der Route oder wähle 'Alle' Kategorien."
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Button(
                onClick = onShowAllAndExpandRadius,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B5E))
            ) {
                Text("Alle Kategorien anzeigen & Radius erweitern")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryDetailBottomSheet(
    item: RouteDiscoveryItem,
    isPlanned: Boolean,
    onDismiss: () -> Unit,
    onTogglePlanned: () -> Unit,
    onShowOnMap: () -> Unit,
    onOpenInMaps: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiscoveryThumbnail(
                    item = item,
                    size = 76.dp,
                    cornerRadius = 14.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = item.category.color.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(item.category),
                                contentDescription = null,
                                tint = item.category.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = item.category.label,
                                color = item.category.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = item.subtitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Abweichung", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        "+${String.format("%.1f", item.detourDistanceKm)} km",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF006B5E)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Zeitaufwand", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        "~${item.extraDetourMinutes} Min.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bei Strecken-km", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        "km ${item.routeProgressKm.roundToInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Eintritt", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        if (item.isFree) "Kostenlos" else "Regulär",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.isFree) Color(0xFF0891B2) else Color(0xFF0F172A)
                    )
                }
            }

            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )

            if (item.openingHours != null) {
                Text(
                    text = "Öffnungszeiten: ${item.openingHours}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
            if (item.eventDateText != null) {
                Text(
                    text = "Veranstaltungs-Info: ${item.eventDateText}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFBE185D)
                )
            }
            Text(
                text = "GPS: ${String.format("%.4f", item.point.lat)}, ${String.format("%.4f", item.point.lon)} • Quelle: ${item.source.title}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onShowOnMap) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Karte", fontWeight = FontWeight.Bold)
                }
                item.wikipediaUrl?.let { wiki ->
                    OutlinedButton(onClick = { onOpenUrl(wiki) }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Wikipedia")
                    }
                }
                item.websiteUrl?.let { web ->
                    OutlinedButton(onClick = { onOpenUrl(web) }) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Webseite")
                    }
                }
                OutlinedButton(onClick = onOpenInMaps) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Einzelort in Maps")
                }
            }

            Button(
                onClick = onTogglePlanned,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlanned) Color(0xFFFF8A3D) else Color(0xFF006B5E),
                    contentColor = if (isPlanned) Color(0xFF081224) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = if (isPlanned) Icons.Default.RemoveCircleOutline else Icons.Default.AddLocationAlt,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isPlanned) "Aus dem Track entfernen" else "In den Track & meine Tour einbauen",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ApiHubDialog(
    apiKeys: ApiKeysConfig,
    apiReports: List<ApiStatusReport>,
    onDismiss: () -> Unit,
    onSaveKeys: (ApiKeysConfig) -> Unit
) {
    var orsKey by remember(apiKeys) { mutableStateOf(apiKeys.openRouteServiceKey) }
    var geoKey by remember(apiKeys) { mutableStateOf(apiKeys.geoapifyKey) }
    var otmKey by remember(apiKeys) { mutableStateOf(apiKeys.openTripMapKey) }
    var tmKey by remember(apiKeys) { mutableStateOf(apiKeys.ticketmasterKey) }
    var ebKey by remember(apiKeys) { mutableStateOf(apiKeys.eventbriteKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF006B5E))
                Spacer(Modifier.width(8.dp))
                Text("SeizeTheWay API-Hub & Status", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Live-Status der integrierten APIs (OSRM, Nominatim, Overpass OSM, Open-Meteo Elevation & Wikipedia/Wikidata laufen sofort ohne Schlüssel):",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apiReports) { report ->
                        Surface(
                            color = if (report.active) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (report.active) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = report.source.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (report.active) "Aktiv (${report.latencyMs} ms)" else "Optional",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.active) Color(0xFF15803D) else Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = report.statusMessage,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Optionale API-Keys für erweiterte Provider:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = orsKey,
                            onValueChange = { orsKey = it },
                            label = { Text("OpenRouteService API Key", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = geoKey,
                            onValueChange = { geoKey = it },
                            label = { Text("Geoapify Places API Key", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = otmKey,
                            onValueChange = { otmKey = it },
                            label = { Text("OpenTripMap API Key", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tmKey,
                            onValueChange = { tmKey = it },
                            label = { Text("Ticketmaster Discovery API Key", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = ebKey,
                            onValueChange = { ebKey = it },
                            label = { Text("Eventbrite Private Token", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveKeys(
                        ApiKeysConfig(
                            openRouteServiceKey = orsKey.trim(),
                            geoapifyKey = geoKey.trim(),
                            openTripMapKey = otmKey.trim(),
                            ticketmasterKey = tmKey.trim(),
                            eventbriteKey = ebKey.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B5E))
            ) {
                Text("Speichern & Neu laden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

/**
 * Transfers the full track including Origin, Destination, all selected Waypoints (`waypoints`),
 * and the matching travel mode (`driving`, `bicycling`, `walking`) directly to Google Maps!
 */
private fun launchTrackInGoogleMaps(
    context: Context,
    origin: GeoPoint,
    destination: GeoPoint,
    waypoints: List<RouteDiscoveryItem>,
    travelMode: TravelMode
) {
    val originParam = String.format(java.util.Locale.US, "%.5f,%.5f", origin.lat, origin.lon)
    val destParam = String.format(java.util.Locale.US, "%.5f,%.5f", destination.lat, destination.lon)
    val waypointsParam = if (waypoints.isNotEmpty()) {
        val wpJoined = waypoints.joinToString("|") {
            String.format(java.util.Locale.US, "%.5f,%.5f", it.point.lat, it.point.lon)
        }
        "&waypoints=${Uri.encode(wpJoined)}"
    } else {
        ""
    }
    val url = "https://www.google.com/maps/dir/?api=1" +
            "&origin=$originParam" +
            "&destination=$destParam" +
            waypointsParam +
            "&travelmode=${travelMode.googleMapsTravelMode}"
    openWebUrl(context, url)
}

private fun openPointInMaps(context: Context, pt: GeoPoint, title: String) {
    val encodedTitle = Uri.encode(title)
    val geoUri = Uri.parse("geo:${pt.lat},${pt.lon}?q=${pt.lat},${pt.lon}($encodedTitle)")
    val intent = Intent(Intent.ACTION_VIEW, geoUri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        openWebUrl(context, "https://www.openstreetmap.org/?mlat=${pt.lat}&mlon=${pt.lon}#map=15/${pt.lat}/${pt.lon}")
    }
}

private fun openWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

@Composable
private fun LocationAutocompleteField(
    label: String,
    query: String,
    placeholder: String,
    isConfirmed: Boolean,
    suggestions: List<GeoPoint>,
    leadingIcon: ImageVector,
    leadingIconTint: Color,
    appLanguage: AppLanguage = AppLanguage.DE,
    onQueryChange: (String) -> Unit,
    onSelectSuggestion: (GeoPoint) -> Unit,
    onConfirmManual: () -> Unit,
    onDetectGpsLocation: (() -> Unit)? = null,
    isDetectingLocation: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label, fontSize = 12.sp) },
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    if (onDetectGpsLocation != null) {
                        IconButton(
                            onClick = onDetectGpsLocation,
                            modifier = Modifier.size(34.dp)
                        ) {
                            if (isDetectingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF006B5E)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Standort ermitteln",
                                    tint = Color(0xFF006B5E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (query.isNotBlank()) {
                        if (isConfirmed) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = if (appLanguage.isGerman) "Bestätigt" else "Confirmed",
                                        color = Color(0xFF15803D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            FilledTonalButton(
                                onClick = onConfirmManual,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFFEF3C7),
                                    contentColor = Color(0xFF92400E)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (appLanguage.isGerman) "Bestätigen" else "Confirm",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onSearch = { onImeAction?.invoke() }, onDone = { onImeAction?.invoke() }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown suggestions list if typing and not confirmed
        if (suggestions.isNotEmpty() && !isConfirmed) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF006B5E).copy(alpha = 0.35f)),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = if (appLanguage.isGerman) "Vorschläge (Antippen zum Übernehmen):" else "Suggestions (Tap to select):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B5E),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    suggestions.forEach { suggestion ->
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSuggestion(suggestion) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = leadingIconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = suggestion.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = Color(0xFF006B5E).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF006B5E),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = if (appLanguage.isGerman) "Wählen" else "Select",
                                            color = Color(0xFF006B5E),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        if (suggestion != suggestions.last()) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}
