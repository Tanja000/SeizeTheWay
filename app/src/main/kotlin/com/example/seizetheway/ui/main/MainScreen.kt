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
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.seizetheway.data.*
import kotlin.math.roundToInt

private val SUGGESTED_KEYWORDS = listOf(
    "See", "Burg", "Wasserfall", "Aussicht", "Kostenlos",
    "Klamm", "Kaffee", "Biergarten", "Museum", "Konzert", "Schloss", "Moor"
)

@Composable
fun seizeInputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF0F172A),
    unfocusedTextColor = Color(0xFF0F172A),
    disabledTextColor = Color(0xFF0F172A),
    focusedContainerColor = Color(0xFFF8FAFC),
    unfocusedContainerColor = Color(0xFFF8FAFC),
    focusedBorderColor = Color(0xFF006B5E),
    unfocusedBorderColor = Color(0xFF94A3B8),
    focusedLabelColor = Color(0xFF006B5E),
    unfocusedLabelColor = Color(0xFF475569),
    focusedPlaceholderColor = Color(0xFF64748B),
    unfocusedPlaceholderColor = Color(0xFF64748B),
    cursorColor = Color(0xFF006B5E)
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
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    // Standorterkennung als Default für den Ausgangspunkt
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        showLocationPermissionDialog = false
        viewModel.detectUserLocation(context)
    }

    val requestOrDetectLocation = {
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
            showLocationPermissionDialog = true
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
            showLocationPermissionDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F7FB),
        topBar = {
            SeizeTheWayTopBar(
                activeApisCount = state.apiReports.count { it.active },
                appLanguage = state.appLanguage,
                onToggleLanguage = viewModel::toggleAppLanguage,
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
                        onDetectLocation = requestOrDetectLocation,
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

    // Location Permission Dialog on Installation / First Launch
    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showLocationPermissionDialog = false
                viewModel.detectUserLocation(context)
            },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF006B5E),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (state.appLanguage.isGerman) "Standort verwenden?" else "Use your location?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Darf SeizeTheWay deinen aktuellen Standort ermitteln und deinen Ortsnamen automatisch in das Feld »Ausgangspunkt« eintragen?"
                        } else {
                            "Allow SeizeTheWay to detect your current location and automatically enter your town name into the 'Origin' field?"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 20.sp
                    )
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "So können dir sofort passende Auto-, Rad- und Wander-Tracks im Umkreis von 50 km vorgeschlagen werden."
                        } else {
                            "This allows us to immediately suggest matching car, bike, and hike tracks within a 50 km radius of your location."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006B5E),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (state.appLanguage.isGerman) "Standort freigeben" else "Allow Location",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        viewModel.detectUserLocation(context)
                    }
                ) {
                    Text(
                        text = if (state.appLanguage.isGerman) "Später" else "Not now",
                        color = Color(0xFF64748B)
                    )
                }
            }
        )
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
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color(0xFF006B5E))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (state.appLanguage.isGerman) "Tour speichern" else "Save Tour",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Speichert den aktuellen Track inklusive aller ${state.plannedStops.size} ausgewählten Orte & Events."
                        } else {
                            "Saves the current track including all ${state.plannedStops.size} selected places & events."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text(if (state.appLanguage.isGerman) "Name der Tour" else "Tour Name") },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = seizeInputFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text(if (state.appLanguage.isGerman) "Zeitraum / Datum (z. B. Samstag, 14. Mai)" else "Date / Period") },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = seizeInputFieldColors(),
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
                    Text(if (state.appLanguage.isGerman) "Speichern" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTripDialog = false }) {
                    Text(if (state.appLanguage.isGerman) "Abbrechen" else "Cancel")
                }
            }
        )
    }

    // Full Detail Modal Sheet for Selected Discovery Item
    state.selectedDetailItem?.let { detailItem ->
        DiscoveryDetailBottomSheet(
            item = detailItem,
            isPlanned = detailItem.id in state.plannedStopIds,
            appLanguage = state.appLanguage,
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
            appLanguage = state.appLanguage,
            onDismiss = { viewModel.setShowApiDialog(false) },
            onSaveKeys = viewModel::saveApiKeys
        )
    }
}

@Composable
private fun SeizeTheWayTopBar(
    activeApisCount: Int,
    appLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
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
                        "Tracks für Autos, Rad & Wandern"
                    } else {
                        "Tracks for Cars, Bike & Hike"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Language Switcher with Flags (German 🇩🇪 | British 🇬🇧)
            Surface(
                color = Color(0xFF152946),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2CE5C6).copy(alpha = 0.45f)),
                onClick = onToggleLanguage
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Surface(
                        color = if (appLanguage.isGerman) Color(0xFF2CE5C6).copy(alpha = 0.22f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🇩🇪",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "·",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = if (!appLanguage.isGerman) Color(0xFF2CE5C6).copy(alpha = 0.22f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🇬🇧",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(6.dp))

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
                            text = "${if (state.appLanguage.isGerman) "Alle" else "All"} (${state.displayedItems.size})",
                            color = if (allSelected) Color.White else Color(0xFF1E293B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
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
                                text = "${cat.localizedLabel(state.appLanguage)} ($count)",
                                color = if (isSubSelected) Color.White else Color(0xFF1E293B),
                                fontSize = 12.sp,
                                fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
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
                    LoadingProgressBanner(
                        maxDetourKm = state.maxDetourKm,
                        travelMode = state.travelMode,
                        appLanguage = state.appLanguage
                    )
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
                        appLanguage = state.appLanguage,
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
                        appLanguage = state.appLanguage,
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
private fun LoadingProgressBanner(
    maxDetourKm: Float,
    travelMode: TravelMode,
    appLanguage: AppLanguage = AppLanguage.DE
) {
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
                    text = if (appLanguage.isGerman) {
                        "${travelMode.localizedTrackType(AppLanguage.DE)} & Höhenprofil werden berechnet…"
                    } else {
                        "Calculating ${travelMode.localizedTrackType(AppLanguage.EN)} & elevation profile…"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = if (appLanguage.isGerman) {
                        "Abfrage von OSM-Track (${travelMode.localizedLabel(AppLanguage.DE)}), Höhenmetern, Overpass & Wikipedia (±${maxDetourKm.roundToInt()} km)"
                    } else {
                        "Querying OSM track (${travelMode.localizedLabel(AppLanguage.EN)}), elevation, Overpass & Wikipedia (±${maxDetourKm.roundToInt()} km)"
                    },
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
    val modePresets = getNearbyRecommendedPresets(
        mode = state.travelMode,
        originQuery = state.originQuery,
        originPoint = state.userLocationPoint ?: state.route?.origin,
        lang = state.appLanguage,
        nearbyCorridorItems = state.allCorridorItems
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF006B5E).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = Color(0xFF006B5E),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (state.appLanguage.isGerman) "Track- & Entdecker-Konfigurator" else "Track & Explorer Configurator",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Fortbewegungsart, Start, Ziel, max. Abweichung & Interessen"
                        } else {
                            "Travel mode, origin, destination, max detour & interests"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            // 1. Travel Mode Selector (Auto, Rad, Wandern) -> selects dedicated track profiles & presets!
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (state.appLanguage.isGerman) "Fortbewegungsart wählen:" else "Choose travel mode:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
                        Surface(
                            color = if (selected) Color(0xFF006B5E) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (selected) Color(0xFF006B5E) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onTravelModeChange(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = mode.localizedLabel(state.appLanguage),
                                    color = if (selected) Color.White else Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Mode-Specific Recommended Tracks Strip strictly within 50 km radius of user location!
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.appLanguage.isGerman) {
                            "Empfohlene ${state.travelMode.localizedLabel(state.appLanguage)}-Tracks im Umkreis von 50 km:"
                        } else {
                            "Recommended ${state.travelMode.localizedLabel(state.appLanguage)} tracks within 50 km:"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        color = Color(0xFFCCFBF1),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "≤ 50 km",
                            color = Color(0xFF006B5E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    modePresets.forEach { preset ->
                        val isSelected = state.originQuery.equals(preset.originName, ignoreCase = true) &&
                                state.destinationQuery.equals(preset.destinationName, ignoreCase = true)
                        Surface(
                            color = if (isSelected) Color(0xFF006B5E) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF006B5E) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectPreset(preset) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.localizedTitle(state.appLanguage),
                                        color = if (isSelected) Color(0xFF2CE5C6) else Color(0xFF006B5E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Surface(
                                        color = if (isSelected) Color.White.copy(alpha = 0.18f) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "~${preset.approxDistanceKm} km",
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Text(
                                    text = "${preset.originName} → ${preset.destinationName}",
                                    color = if (isSelected) Color.White else Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // 3. Full-width Origin & Destination Inputs (Crystal clear text, full width)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LocationAutocompleteField(
                    label = if (state.appLanguage.isGerman) "Ausgangspunkt (Standort)" else "Origin (Current Location)",
                    query = state.originQuery,
                    placeholder = if (state.appLanguage.isGerman) "Ort oder Adresse eingeben…" else "Enter city or address…",
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

                // Sleek Swap Row between Origin & Destination so both fields have 100% width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        onClick = onSwapLocations,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = Color(0xFF006B5E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (state.appLanguage.isGerman) "Start & Ziel tauschen" else "Swap Origin & Destination",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                }

                LocationAutocompleteField(
                    label = if (state.appLanguage.isGerman) "Reiseziel (Zielort / Adresse)" else "Destination (City / Address)",
                    query = state.destinationQuery,
                    placeholder = if (state.appLanguage.isGerman) "Zielort eingeben…" else "Enter destination…",
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
                            text = if (state.appLanguage.isGerman) "Max. Abweichung vom Track" else "Max. Track Detour",
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
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
                    listOf(1f, 3f, 5f, 10f, 25f, 50f).forEach { kmPreset ->
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
                                    color = if (active) Color(0xFF006B5E) else Color(0xFF475569),
                                    maxLines = 1,
                                    softWrap = false
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
                        text = if (state.appLanguage.isGerman) "Kategorien wählen" else "Select Categories",
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
                                text = if (state.showAllCategories) {
                                    if (state.appLanguage.isGerman) "Alles anzeigen (Aktiv)" else "Show All (Active)"
                                } else {
                                    if (state.appLanguage.isGerman) "Alles anzeigen" else "Show All"
                                },
                                color = if (state.showAllCategories) Color.White else Color(0xFF1E293B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
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
                                    text = cat.localizedLabel(state.appLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (countInCorridor > 0) {
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "($countInCorridor)",
                                        fontSize = 11.sp,
                                        color = if (isSelected) cat.color else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Keywords / Stichworte Input + Quick Suggestion Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (state.appLanguage.isGerman) "Stichworte & Interessen (optional):" else "Keywords & Interests (optional):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                OutlinedTextField(
                    value = state.keywordsQuery,
                    onValueChange = onKeywordsChange,
                    placeholder = {
                        Text(
                            text = if (state.appLanguage.isGerman) "z. B. Wasserfall, Burg, Aussicht, Kaffee, Kostenlos…" else "e.g. Waterfall, Castle, Viewpoint, Coffee, Free…",
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
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF006B5E)
                        )
                    },
                    trailingIcon = {
                        if (state.keywordsQuery.isNotBlank()) {
                            IconButton(onClick = { onKeywordsChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = if (state.appLanguage.isGerman) "Stichworte löschen" else "Clear keywords",
                                    tint = Color(0xFF475569)
                                )
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
                        text = if (state.appLanguage.isGerman) "Beliebte Stichworte:" else "Popular keywords:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        softWrap = false
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
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                maxLines = 1,
                                softWrap = false
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
                        "${state.travelMode.localizedLabel(state.appLanguage)}-Track berechnen"
                    } else {
                        "Calculate ${state.travelMode.localizedLabel(state.appLanguage)} Track"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false
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
                    text = if (state.appLanguage.isGerman) {
                        "$shownCount Entdeckungen ${state.activeResultCategoryTab?.let { "in ${it.localizedLabel(AppLanguage.DE)}" } ?: "entlang des Tracks"}"
                    } else {
                        "$shownCount Discoveries ${state.activeResultCategoryTab?.let { "in ${it.localizedLabel(AppLanguage.EN)}" } ?: "along the track"}"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                state.route?.let { r ->
                    Text(
                        text = "${String.format("%.1f", r.distanceKm)} km • ↑${r.elevationGainMeters} Hm • ${formatDuration(r.durationMinutes)} (${r.travelMode.localizedLabel(state.appLanguage)})",
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
                Text(
                    text = if (state.appLanguage.isGerman) "Optimaler Mix" else "Optimal Mix",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
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
                val icon = when (order) {
                    DiscoverySortOrder.ROUTE_PROGRESS -> Icons.Default.Timeline
                    DiscoverySortOrder.MIN_DETOUR -> Icons.Default.NearMe
                    DiscoverySortOrder.TOP_RATED -> Icons.Default.Star
                }
                Surface(
                    color = if (active) Color(0xFF0F2942) else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (active) Color(0xFF2CE5C6) else Color(0xFFCBD5E1)
                    ),
                    onClick = { onSortOrderChange(order) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) Color(0xFF2CE5C6) else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = order.localized(state.appLanguage),
                            color = if (active) Color.White else Color(0xFF334155),
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Surface(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(14.dp),
                onClick = onOpenApiDialog
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
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
                        text = if (state.appLanguage.isGerman) "API-Quellen" else "API Sources",
                        color = Color(0xFF0369A1),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryStopCard(
    item: RouteDiscoveryItem,
    routeTotalKm: Double,
    isPlanned: Boolean,
    isHighlightedOnMap: Boolean,
    appLanguage: AppLanguage = AppLanguage.DE,
    onCardClick: () -> Unit,
    onFocusOnMap: () -> Unit,
    onTogglePlanned: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlightedOnMap) 5.dp else 3.dp),
        border = if (isPlanned) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF8A3D))
        } else if (isHighlightedOnMap) {
            androidx.compose.foundation.BorderStroke(1.5.dp, item.category.color)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: Thumbnail + Category/Rating Badges + Clear Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                DiscoveryThumbnail(
                    item = item,
                    size = 76.dp,
                    cornerRadius = 14.dp
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = item.category.color.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = null,
                                    tint = item.category.color,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = item.category.localizedLabel(appLanguage),
                                    color = item.category.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f", item.ratingScore),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        if (item.isFree || WayCategory.KOSTENLOSES in item.additionalCategories) {
                            Surface(
                                color = Color(0xFF06B6D4).copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (appLanguage.isGerman) "Kostenlos" else "Free",
                                    color = Color(0xFF0891B2),
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
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.displaySubtitle(appLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. Full-width Description Text
            Text(
                text = item.displayDescription(appLanguage),
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = Color(0xFF1E293B),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // 3. Full-width Vertically Stacked »Strecken-Details« Box (No vertical text squishing!)
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row A: Position along the track + full-width progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = Color(0xFF006B5E),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (appLanguage.isGerman) "Position auf der Strecke:" else "Position on track:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                            Text(
                                text = if (appLanguage.isGerman) {
                                    "bei km ${item.routeProgressKm.roundToInt()} von ${routeTotalKm.roundToInt()} km"
                                } else {
                                    "at km ${item.routeProgressKm.roundToInt()} of ${routeTotalKm.roundToInt()} km"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { item.routeProgressFraction },
                            color = item.category.color,
                            trackColor = Color(0xFFE2E8F0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Row B: Detour distance & extra time (Full-width row)
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
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Abweichung / Abstecher:" else "Track Detour:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = if (appLanguage.isGerman) {
                                if (item.detourDistanceKm < 0.15) {
                                    "Direkt an der Route (0 km)"
                                } else {
                                    "+${String.format(java.util.Locale.US, "%.1f", item.detourDistanceKm * 2.0)} km (~${item.extraDetourMinutes} Min.)"
                                }
                            } else {
                                if (item.detourDistanceKm < 0.15) {
                                    "Directly on route (0 km)"
                                } else {
                                    "+${String.format(java.util.Locale.US, "%.1f", item.detourDistanceKm * 2.0)} km (~${item.extraDetourMinutes} min)"
                                }
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF006B5E)
                        )
                    }

                    // Row C: Event date / Opening hours / Source (Full-width row)
                    val displayDate = item.displayEventDate(appLanguage)
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (displayDate != null) Icons.Default.Event else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (displayDate != null) Color(0xFFEC4899) else Color(0xFF0284C7),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = displayDate
                                    ?: item.openingHours?.let {
                                        if (appLanguage.isGerman) "Öffnungszeiten: $it" else "Hours: $it"
                                    }
                                    ?: (if (appLanguage.isGerman) "Quelle: ${item.source.title}" else "Source: ${item.source.title}"),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (displayDate != null) Color(0xFFBE185D) else Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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
                                text = if (appLanguage.isGerman) "✓ Stichwort: $matchedKw" else "✓ Keyword: $matchedKw",
                                color = Color(0xFFB45309),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                softWrap = false
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
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 4. Full-width Action Bar (Spacious, readable buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onFocusOnMap,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (appLanguage.isGerman) "Auf Karte" else "Show on Map",
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onTogglePlanned,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlanned) Color(0xFFFF8A3D) else Color(0xFF006B5E),
                        contentColor = if (isPlanned) Color(0xFF081224) else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlanned) Icons.Default.Check else Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isPlanned) {
                            if (appLanguage.isGerman) "✓ Im Track" else "✓ In Track"
                        } else {
                            if (appLanguage.isGerman) "+ In den Track" else "+ Add to Track"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDiscoveryStateCard(
    maxDetourKm: Float,
    hasKeywords: Boolean,
    appLanguage: AppLanguage = AppLanguage.DE,
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
                text = if (appLanguage.isGerman) "Keine Treffer in diesem Tab / Filter" else "No results for this tab / filter",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = if (hasKeywords) {
                    if (appLanguage.isGerman) {
                        "Für deine eingegebenen Stichworte wurden im Radius von ±${maxDetourKm.roundToInt()} km keine direkten Treffer gefunden. Erweitere den Radius oder zeige alle Kategorien an."
                    } else {
                        "No matches found for your keywords within ±${maxDetourKm.roundToInt()} km. Expand your detour radius or show all categories."
                    }
                } else {
                    if (appLanguage.isGerman) {
                        "Erhöhe die maximale Abweichung von der Route oder wähle 'Alle' Kategorien."
                    } else {
                        "Increase max detour distance or select 'Show All' categories."
                    }
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Button(
                onClick = onShowAllAndExpandRadius,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B5E))
            ) {
                Text(
                    text = if (appLanguage.isGerman) "Alle Kategorien anzeigen & Radius erweitern" else "Show all categories & expand radius",
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryDetailBottomSheet(
    item: RouteDiscoveryItem,
    isPlanned: Boolean,
    appLanguage: AppLanguage = AppLanguage.DE,
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
                                text = item.category.localizedLabel(appLanguage),
                                color = item.category.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.displayTitle(appLanguage),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = item.displaySubtitle(appLanguage),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage.isGerman) "Abweichung vom Track:" else "Track Detour:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = if (item.detourDistanceKm < 0.15) {
                                if (appLanguage.isGerman) "Direkt an der Route (0 km)" else "Directly on route (0 km)"
                            } else {
                                "+${String.format(java.util.Locale.US, "%.1f", item.detourDistanceKm * 2.0)} km (~${item.extraDetourMinutes} Min.)"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF006B5E)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage.isGerman) "Position auf der Strecke:" else "Position on track:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "km ${item.routeProgressKm.roundToInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage.isGerman) "Eintritt & Bewertung:" else "Admission & Rating:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "${if (item.isFree) (if (appLanguage.isGerman) "Kostenlos" else "Free") else (if (appLanguage.isGerman) "Regulär" else "Regular")} • ★ ${String.format(java.util.Locale.US, "%.1f", item.ratingScore)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (item.isFree) Color(0xFF0891B2) else Color(0xFF0F172A)
                        )
                    }
                }
            }

            Text(
                text = item.displayDescription(appLanguage),
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )

            if (item.openingHours != null) {
                Text(
                    text = if (appLanguage.isGerman) "Öffnungszeiten: ${item.openingHours}" else "Opening hours: ${item.openingHours}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
            val bsheetDate = item.displayEventDate(appLanguage)
            if (bsheetDate != null) {
                Text(
                    text = if (appLanguage.isGerman) "Veranstaltungs-Info: $bsheetDate" else "Event info: $bsheetDate",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFBE185D)
                )
            }
            Text(
                text = "GPS: ${String.format("%.4f", item.point.lat)}, ${String.format("%.4f", item.point.lon)} • ${if (appLanguage.isGerman) "Quelle" else "Source"}: ${item.source.title}",
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
                    Text(if (appLanguage.isGerman) "Karte" else "Map", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
                item.wikipediaUrl?.let { wiki ->
                    OutlinedButton(onClick = { onOpenUrl(wiki) }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Wikipedia", maxLines = 1, softWrap = false)
                    }
                }
                item.websiteUrl?.let { web ->
                    OutlinedButton(onClick = { onOpenUrl(web) }) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (appLanguage.isGerman) "Webseite" else "Website", maxLines = 1, softWrap = false)
                    }
                }
                OutlinedButton(onClick = onOpenInMaps) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (appLanguage.isGerman) "Einzelort in Maps" else "View in Google Maps", maxLines = 1, softWrap = false)
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
                    text = if (isPlanned) {
                        if (appLanguage.isGerman) "Aus dem Track entfernen" else "Remove from Tour Track"
                    } else {
                        if (appLanguage.isGerman) "In den Track & meine Tour einbauen" else "Add to Tour Track"
                    },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ApiHubDialog(
    apiKeys: ApiKeysConfig,
    apiReports: List<ApiStatusReport>,
    appLanguage: AppLanguage = AppLanguage.DE,
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
                Text(
                    text = if (appLanguage.isGerman) "SeizeTheWay API-Hub & Status" else "SeizeTheWay API Hub & Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    softWrap = false
                )
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
                    text = if (appLanguage.isGerman) {
                        "Live-Status der integrierten APIs (OSRM, Nominatim, Overpass OSM, Open-Meteo Elevation & Wikipedia/Wikidata laufen sofort ohne Schlüssel):"
                    } else {
                        "Live status of integrated APIs (OSRM, Nominatim, Overpass OSM, Open-Meteo Elevation & Wikipedia/Wikidata work out-of-the-box without keys):"
                    },
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
                                        text = if (report.active) "${if (appLanguage.isGerman) "Aktiv" else "Active"} (${report.latencyMs} ms)" else (if (appLanguage.isGerman) "Optional" else "Optional"),
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
                            text = if (appLanguage.isGerman) "Optionale API-Keys für erweiterte Provider:" else "Optional API keys for extended providers:",
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
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            colors = seizeInputFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = geoKey,
                            onValueChange = { geoKey = it },
                            label = { Text("Geoapify Places API Key", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            colors = seizeInputFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = otmKey,
                            onValueChange = { otmKey = it },
                            label = { Text("OpenTripMap API Key", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            colors = seizeInputFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tmKey,
                            onValueChange = { tmKey = it },
                            label = { Text("Ticketmaster Discovery API Key", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            colors = seizeInputFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = ebKey,
                            onValueChange = { ebKey = it },
                            label = { Text("Eventbrite Private Token", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            colors = seizeInputFieldColors(),
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
                Text(
                    text = if (appLanguage.isGerman) "Speichern & Neu laden" else "Save & Reload",
                    maxLines = 1,
                    softWrap = false
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (appLanguage.isGerman) "Schließen" else "Close",
                    maxLines = 1,
                    softWrap = false
                )
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
    var hadFocus by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header row ABOVE the input field so the input field itself has 100% width for text!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onDetectGpsLocation != null) {
                    Surface(
                        color = Color(0xFFCCFBF1),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF006B5E).copy(alpha = 0.4f)),
                        onClick = onDetectGpsLocation
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDetectingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF006B5E)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Mein Standort",
                                    tint = Color(0xFF006B5E),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage.isGerman) "Mein Standort" else "My Location",
                                color = Color(0xFF006B5E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (query.isNotBlank()) {
                    if (isConfirmed) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = if (appLanguage.isGerman) "Bestätigt" else "Confirmed",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            onClick = onConfirmManual
                        ) {
                            Text(
                                text = if (appLanguage.isGerman) "✓ Bestätigen" else "✓ Confirm",
                                color = Color(0xFF92400E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color(0xFF0F172A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            colors = seizeInputFieldColors(),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = if (appLanguage.isGerman) "Löschen" else "Clear",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onSearch = { onImeAction?.invoke() }, onDone = { onImeAction?.invoke() }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !hadFocus) {
                        if (query.isNotBlank()) {
                            onQueryChange("")
                        }
                    }
                    hadFocus = focusState.isFocused
                }
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
