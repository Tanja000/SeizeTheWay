package com.example.seizetheway.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seizetheway.data.RouteDiscoveryService
import com.example.seizetheway.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DiscoverySortOrder(val label: String) {
    ROUTE_PROGRESS("Entlang der Route"),
    MIN_DETOUR("Kleinster Umweg"),
    TOP_RATED("Top-Empfehlungen")
}

data class SeizeTheWayUiState(
    val activeTab: AppTab = AppTab.SEARCH,
    val activeResultCategoryTab: WayCategory? = null,
    val mapDisplayFilter: MapDisplayFilter = MapDisplayFilter.ALL,
    val originQuery: String = "Aktueller Standort",
    val destinationQuery: String = "Garmisch-Partenkirchen",
    val isOriginConfirmed: Boolean = true,
    val isDestinationConfirmed: Boolean = true,
    val originSuggestions: List<GeoPoint> = emptyList(),
    val destinationSuggestions: List<GeoPoint> = emptyList(),
    val isDetectingLocation: Boolean = false,
    val maxDetourKm: Float = 12f,
    val travelMode: TravelMode = TravelMode.CAR,
    val showAllCategories: Boolean = true,
    val selectedCategories: Set<WayCategory> = WayCategory.entries.toSet(),
    val keywordsQuery: String = "",
    val strictKeywordFilter: Boolean = true,
    val sortOrder: DiscoverySortOrder = DiscoverySortOrder.ROUTE_PROGRESS,
    val isLoading: Boolean = false,
    val isRecalculatingTrack: Boolean = false,
    val errorMessage: String? = null,
    val route: RouteResult? = null,
    val allCorridorItems: List<RouteDiscoveryItem> = emptyList(),
    val displayedItems: List<RouteDiscoveryItem> = emptyList(),
    val plannedStopIds: Set<String> = emptySet(),
    val savedTrips: List<SavedTrip> = emptyList(),
    val tripNotesDraft: String = "Picknickdecke, Kamera & passende Ausrüstung einpacken",
    val selectedDetailItem: RouteDiscoveryItem? = null,
    val highlightedMapItemId: String? = null,
    val appLanguage: AppLanguage = AppLanguage.DE,
    val apiKeys: ApiKeysConfig = ApiKeysConfig(),
    val apiReports: List<ApiStatusReport> = emptyList(),
    val showApiDialog: Boolean = false
) {
    val isCurrentTourAlreadySaved: Boolean
        get() {
            val r = route ?: return false
            return savedTrips.any { saved ->
                saved.origin.name.equals(r.origin.name, ignoreCase = true) &&
                        saved.destination.name.equals(r.destination.name, ignoreCase = true) &&
                        saved.travelMode == r.travelMode &&
                        saved.stops.map { it.id }.toSet() == plannedStopIds
            }
        }

    val plannedStops: List<RouteDiscoveryItem>
        get() = allCorridorItems
            .filter { it.id in plannedStopIds }
            .sortedBy { it.routeProgressFraction }

    val mapDisplayedItems: List<RouteDiscoveryItem>
        get() = when (mapDisplayFilter) {
            MapDisplayFilter.IN_TRACK -> plannedStops
            MapDisplayFilter.ALL -> displayedItems
        }

    val totalExtraDetourKm: Double
        get() = route?.let { (it.distanceKm - it.baseDistanceKm).coerceAtLeast(0.0) }
            ?: plannedStops.sumOf { it.detourDistanceKm * 2.0 }

    val totalExtraDetourMinutes: Int
        get() = route?.let { (it.durationMinutes - it.baseDurationMinutes).coerceAtLeast(0) }
            ?: plannedStops.sumOf { it.extraDetourMinutes }

    val tabFilteredItems: List<RouteDiscoveryItem>
        get() = if (activeResultCategoryTab == null) {
            displayedItems
        } else {
            displayedItems.filter {
                it.category == activeResultCategoryTab || activeResultCategoryTab in it.additionalCategories
            }
        }
}

class SeizeTheWayViewModel(
    private val service: RouteDiscoveryService = RouteDiscoveryService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeizeTheWayUiState())
    val uiState: StateFlow<SeizeTheWayUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var trackUpdateJob: Job? = null
    private var originSuggestJob: Job? = null
    private var destSuggestJob: Job? = null

    init {
        executeRouteSearch(switchToResultsTab = false)
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleAppLanguage() {
        _uiState.update {
            it.copy(appLanguage = if (it.appLanguage == AppLanguage.DE) AppLanguage.EN else AppLanguage.DE)
        }
    }

    fun setAppLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(appLanguage = lang) }
    }

    fun setMapDisplayFilter(filter: MapDisplayFilter) {
        _uiState.update { state ->
            val itemsForFilter = if (filter == MapDisplayFilter.IN_TRACK) state.plannedStops else state.displayedItems
            state.copy(
                mapDisplayFilter = filter,
                highlightedMapItemId = itemsForFilter.firstOrNull()?.id ?: state.highlightedMapItemId
            )
        }
    }

    fun selectResultCategoryTab(category: WayCategory?) {
        _uiState.update { it.copy(activeResultCategoryTab = category) }
    }

    fun detectUserLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true) }
            try {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                val isGpsEnabled = locManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true
                val isNetEnabled = locManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true

                var lastLoc: android.location.Location? = null
                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    if (isGpsEnabled) {
                        lastLoc = locManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    }
                    if (lastLoc == null && isNetEnabled) {
                        lastLoc = locManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    }
                }

                if (lastLoc != null) {
                    val reversed = service.reverseGeocode(lastLoc.latitude, lastLoc.longitude)
                    val label = "${reversed.name} (Mein Standort)"
                    _uiState.update {
                        it.copy(
                            originQuery = label,
                            isOriginConfirmed = true,
                            originSuggestions = emptyList(),
                            isDetectingLocation = false
                        )
                    }
                    executeRouteSearch(switchToResultsTab = false)
                } else {
                    _uiState.update {
                        it.copy(
                            originQuery = "Aktueller Standort (München)",
                            isOriginConfirmed = true,
                            isDetectingLocation = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        originQuery = "Aktueller Standort (München)",
                        isOriginConfirmed = true,
                        isDetectingLocation = false
                    )
                }
            }
        }
    }

    fun updateOrigin(value: String) {
        _uiState.update {
            it.copy(
                originQuery = value,
                isOriginConfirmed = false,
                errorMessage = null
            )
        }
        originSuggestJob?.cancel()
        val clean = value.trim()
        if (clean.isNotEmpty()) {
            originSuggestJob = viewModelScope.launch {
                kotlinx.coroutines.delay(80)
                val suggestions = service.fetchPlaceSuggestions(clean)
                _uiState.update { it.copy(originSuggestions = suggestions) }
            }
        } else {
            _uiState.update { it.copy(originSuggestions = emptyList()) }
        }
    }

    fun confirmOriginSuggestion(suggestion: GeoPoint) {
        originSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                originQuery = suggestion.name,
                isOriginConfirmed = true,
                originSuggestions = emptyList(),
                errorMessage = null
            )
        }
    }

    fun confirmOriginManual() {
        originSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                isOriginConfirmed = true,
                originSuggestions = emptyList()
            )
        }
    }

    fun updateDestination(value: String) {
        _uiState.update {
            it.copy(
                destinationQuery = value,
                isDestinationConfirmed = false,
                errorMessage = null
            )
        }
        destSuggestJob?.cancel()
        val clean = value.trim()
        if (clean.isNotEmpty()) {
            destSuggestJob = viewModelScope.launch {
                kotlinx.coroutines.delay(80)
                val suggestions = service.fetchPlaceSuggestions(clean)
                _uiState.update { it.copy(destinationSuggestions = suggestions) }
            }
        } else {
            _uiState.update { it.copy(destinationSuggestions = emptyList()) }
        }
    }

    fun confirmDestinationSuggestion(suggestion: GeoPoint) {
        destSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                destinationQuery = suggestion.name,
                isDestinationConfirmed = true,
                destinationSuggestions = emptyList(),
                errorMessage = null
            )
        }
    }

    fun confirmDestinationManual() {
        destSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                isDestinationConfirmed = true,
                destinationSuggestions = emptyList()
            )
        }
    }

    fun swapOriginAndDestination() {
        _uiState.update {
            it.copy(
                originQuery = it.destinationQuery,
                destinationQuery = it.originQuery,
                isOriginConfirmed = true,
                isDestinationConfirmed = true,
                originSuggestions = emptyList(),
                destinationSuggestions = emptyList(),
                errorMessage = null
            )
        }
        executeRouteSearch(switchToResultsTab = false)
    }

    fun updateMaxDetourKm(km: Float) {
        val clamped = km.coerceIn(1f, 50f)
        _uiState.update { state ->
            val updated = state.copy(maxDetourKm = clamped)
            recomputeDisplayedItems(updated)
        }
    }

    fun updateTravelMode(mode: TravelMode) {
        val current = _uiState.value
        val allPresets = CAR_ROUTE_PRESETS + BIKE_ROUTE_PRESETS + HIKE_ROUTE_PRESETS
        val wasUsingPreset = allPresets.any {
            it.originName.equals(current.originQuery, ignoreCase = true) &&
                    it.destinationName.equals(current.destinationQuery, ignoreCase = true)
        }
        val modeDefaultPreset = presetsForTravelMode(mode).first()

        _uiState.update { state ->
            if (wasUsingPreset) {
                state.copy(
                    travelMode = mode,
                    originQuery = modeDefaultPreset.originName,
                    destinationQuery = modeDefaultPreset.destinationName,
                    maxDetourKm = modeDefaultPreset.defaultDetourKm
                )
            } else {
                state.copy(travelMode = mode)
            }
        }
        executeRouteSearch(switchToResultsTab = false, preservePlannedStops = true)
    }

    fun setShowAllCategories(showAll: Boolean) {
        _uiState.update { state ->
            val updated = if (showAll) {
                state.copy(
                    showAllCategories = true,
                    selectedCategories = WayCategory.entries.toSet(),
                    activeResultCategoryTab = null
                )
            } else {
                state.copy(
                    showAllCategories = false,
                    selectedCategories = setOf(WayCategory.NATUR, WayCategory.ERLEBNIS, WayCategory.CAFES)
                )
            }
            recomputeDisplayedItems(updated)
        }
    }

    fun toggleCategory(category: WayCategory) {
        _uiState.update { state ->
            val nextSet = if (state.showAllCategories) {
                setOf(category)
            } else {
                val mutable = state.selectedCategories.toMutableSet()
                if (category in mutable) {
                    mutable.remove(category)
                } else {
                    mutable.add(category)
                }
                mutable
            }
            val nowAll = nextSet.size == WayCategory.entries.size || nextSet.isEmpty()
            val updated = state.copy(
                showAllCategories = nowAll,
                selectedCategories = if (nowAll) WayCategory.entries.toSet() else nextSet,
                activeResultCategoryTab = null
            )
            recomputeDisplayedItems(updated)
        }
    }

    fun updateKeywords(keywords: String) {
        _uiState.update { state ->
            val updated = state.copy(keywordsQuery = keywords)
            recomputeDisplayedItems(updated)
        }
    }

    fun toggleKeywordChip(keyword: String) {
        val current = _uiState.value.keywordsQuery
        val parts = current.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        val existingIdx = parts.indexOfFirst { it.equals(keyword, ignoreCase = true) }
        if (existingIdx >= 0) {
            parts.removeAt(existingIdx)
        } else {
            parts.add(keyword)
        }
        updateKeywords(parts.joinToString(", "))
    }

    fun updateSortOrder(order: DiscoverySortOrder) {
        _uiState.update { state ->
            recomputeDisplayedItems(state.copy(sortOrder = order))
        }
    }

    fun applyPreset(preset: RoutePreset) {
        originSuggestJob?.cancel()
        destSuggestJob?.cancel()
        _uiState.update {
            it.copy(
                originQuery = preset.originName,
                destinationQuery = preset.destinationName,
                isOriginConfirmed = true,
                isDestinationConfirmed = true,
                originSuggestions = emptyList(),
                destinationSuggestions = emptyList(),
                maxDetourKm = preset.defaultDetourKm,
                travelMode = preset.travelMode,
                keywordsQuery = "",
                showAllCategories = true,
                selectedCategories = WayCategory.entries.toSet(),
                activeResultCategoryTab = null,
                plannedStopIds = emptySet(),
                errorMessage = null
            )
        }
        executeRouteSearch(switchToResultsTab = false)
    }

    fun togglePlannedStop(itemId: String) {
        _uiState.update { state ->
            val mutable = state.plannedStopIds.toMutableSet()
            if (itemId in mutable) mutable.remove(itemId) else mutable.add(itemId)
            state.copy(plannedStopIds = mutable)
        }
        recalculateActiveTrackThroughPlannedStops()
    }

    fun autoCurateIdealTripStops() {
        _uiState.update { state ->
            val pool = state.displayedItems.ifEmpty { state.allCorridorItems }
            if (pool.isEmpty()) return@update state

            val chosen = mutableSetOf<String>()
            pool.filter { it.category in setOf(WayCategory.NATUR, WayCategory.WANDERUNGEN, WayCategory.KOSTENLOSES) }
                .minByOrNull { kotlin.math.abs(it.routeProgressFraction - 0.25f) }
                ?.let { chosen.add(it.id) }

            pool.filter { it.category in setOf(WayCategory.CAFES, WayCategory.RESTAURANTS) }
                .minByOrNull { kotlin.math.abs(it.routeProgressFraction - 0.50f) }
                ?.let { chosen.add(it.id) }

            pool.filter { it.category in setOf(WayCategory.HISTORISCHES, WayCategory.KULTURELLES, WayCategory.ERLEBNIS) }
                .minByOrNull { kotlin.math.abs(it.routeProgressFraction - 0.72f) }
                ?.let { chosen.add(it.id) }

            pool.firstOrNull { it.category == WayCategory.TAGESEVENTS || WayCategory.TAGESEVENTS in it.additionalCategories }
                ?.let { chosen.add(it.id) }

            state.copy(plannedStopIds = chosen)
        }
        recalculateActiveTrackThroughPlannedStops()
    }

    private fun recalculateActiveTrackThroughPlannedStops() {
        val snapshot = _uiState.value
        val currentRoute = snapshot.route ?: return
        val orderedWaypointGeoPoints = snapshot.plannedStops.map { it.point }

        trackUpdateJob?.cancel()
        trackUpdateJob = viewModelScope.launch {
            _uiState.update { it.copy(isRecalculatingTrack = true) }
            try {
                val (updatedTrack, _) = service.computeTrackWithWaypoints(
                    origin = currentRoute.origin,
                    destination = currentRoute.destination,
                    waypoints = orderedWaypointGeoPoints,
                    baseRoute = currentRoute,
                    travelMode = snapshot.travelMode,
                    corridorRadiusKm = snapshot.maxDetourKm.toDouble(),
                    apiKeys = snapshot.apiKeys
                )
                _uiState.update {
                    it.copy(
                        route = updatedTrack,
                        isRecalculatingTrack = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isRecalculatingTrack = false) }
            }
        }
    }

    fun updateTripNotesDraft(notes: String) {
        _uiState.update { it.copy(tripNotesDraft = notes) }
    }

    fun saveCurrentTrip(customTitle: String, dateLabel: String) {
        _uiState.update { state ->
            if (state.isCurrentTourAlreadySaved) return@update state
            val r = state.route ?: return@update state
            val stopsToSave = state.plannedStops
            val title = customTitle.trim().ifBlank { "${r.origin.name} → ${r.destination.name} (${state.travelMode.label})" }
            val newTrip = SavedTrip(
                id = "trip_${System.currentTimeMillis()}",
                title = title,
                dateLabel = dateLabel.trim().ifBlank { "Geplante Tour" },
                origin = r.origin,
                destination = r.destination,
                trackDistanceKm = r.distanceKm,
                trackDurationMinutes = r.durationMinutes,
                elevationGainMeters = r.elevationGainMeters,
                elevationLossMeters = r.elevationLossMeters,
                maxDetourKm = state.maxDetourKm,
                travelMode = state.travelMode,
                stops = stopsToSave,
                trackPolyline = r.polyline,
                notes = state.tripNotesDraft
            )
            state.copy(
                savedTrips = listOf(newTrip) + state.savedTrips,
                activeTab = AppTab.TRIPS
            )
        }
    }

    fun deleteSavedTrip(tripId: String) {
        _uiState.update { state ->
            state.copy(savedTrips = state.savedTrips.filterNot { it.id == tripId })
        }
    }

    /**
     * Loads a saved tour into the map view and sets `mapDisplayFilter = MapDisplayFilter.IN_TRACK`
     * so that immediately ONLY the saved events/stops of this tour are shown on the map!
     */
    fun loadSavedTripIntoActiveRoute(trip: SavedTrip) {
        val savedIds = trip.stops.map { it.id }.toSet()
        val fallbackPoly = if (trip.trackPolyline.size >= 2) {
            trip.trackPolyline
        } else {
            listOf(trip.origin) + trip.stops.sortedBy { it.routeProgressKm }.map { it.point } + listOf(trip.destination)
        }
        val instantRoute = RouteResult(
            origin = trip.origin,
            destination = trip.destination,
            basePolyline = listOf(trip.origin, trip.destination),
            polyline = fallbackPoly,
            baseDistanceKm = (trip.trackDistanceKm - trip.totalExtraDetourKm).coerceAtLeast(1.0),
            distanceKm = trip.trackDistanceKm,
            baseDurationMinutes = trip.trackDurationMinutes,
            durationMinutes = trip.trackDurationMinutes,
            elevationGainMeters = trip.elevationGainMeters,
            elevationLossMeters = trip.elevationLossMeters,
            routingEngineUsed = "Gespeicherte Tour (${trip.travelMode.label})",
            corridorRadiusKm = trip.maxDetourKm.toDouble(),
            travelMode = trip.travelMode,
            includedWaypoints = trip.stops.sortedBy { it.routeProgressKm }.map { it.point }
        )
        _uiState.update { state ->
            val mergedItems = (trip.stops + state.allCorridorItems).distinctBy { it.id }
            state.copy(
                originQuery = trip.origin.name,
                destinationQuery = trip.destination.name,
                maxDetourKm = trip.maxDetourKm,
                travelMode = trip.travelMode,
                route = instantRoute,
                allCorridorItems = mergedItems,
                plannedStopIds = savedIds,
                tripNotesDraft = trip.notes,
                mapDisplayFilter = MapDisplayFilter.IN_TRACK,
                highlightedMapItemId = trip.stops.firstOrNull()?.id,
                activeTab = AppTab.MAP
            )
        }
        executeRouteSearch(
            switchToResultsTab = false,
            preservePlannedStops = true,
            preservedStopsList = trip.stops
        )
    }

    fun selectDetailItem(item: RouteDiscoveryItem?) {
        _uiState.update {
            it.copy(
                selectedDetailItem = item,
                highlightedMapItemId = item?.id ?: it.highlightedMapItemId
            )
        }
    }

    fun highlightMapItem(itemId: String?) {
        _uiState.update { it.copy(highlightedMapItemId = itemId) }
    }

    fun openItemOnMapTab(item: RouteDiscoveryItem) {
        _uiState.update { state ->
            val nextFilter = if (state.mapDisplayFilter == MapDisplayFilter.IN_TRACK && item.id !in state.plannedStopIds) {
                MapDisplayFilter.ALL
            } else {
                state.mapDisplayFilter
            }
            state.copy(
                highlightedMapItemId = item.id,
                mapDisplayFilter = nextFilter,
                activeTab = AppTab.MAP
            )
        }
    }

    fun setShowApiDialog(show: Boolean) {
        _uiState.update { it.copy(showApiDialog = show) }
    }

    fun saveApiKeys(config: ApiKeysConfig) {
        _uiState.update { it.copy(apiKeys = config, showApiDialog = false) }
        executeRouteSearch(switchToResultsTab = false, preservePlannedStops = true)
    }

    fun executeRouteSearch(
        switchToResultsTab: Boolean = true,
        preservePlannedStops: Boolean = false,
        preservedStopsList: List<RouteDiscoveryItem> = emptyList()
    ) {
        val snapshot = _uiState.value
        if (snapshot.originQuery.isBlank() || snapshot.destinationQuery.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Bitte gib sowohl einen Ausgangspunkt als auch ein Reiseziel ein.")
            }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val fetchRadiusKm = snapshot.maxDetourKm.toDouble().coerceAtLeast(18.0)
                val response = service.searchAlongRoute(
                    originQuery = snapshot.originQuery,
                    destinationQuery = snapshot.destinationQuery,
                    maxDetourKm = fetchRadiusKm,
                    selectedCategories = WayCategory.entries.toSet(),
                    showAllCategories = true,
                    keywordsRaw = snapshot.keywordsQuery,
                    onlyStrictKeywordMatches = false,
                    travelMode = snapshot.travelMode,
                    apiKeys = snapshot.apiKeys
                )

                val combinedCorridorItems = (preservedStopsList + response.allCorridorItems).distinctBy { it.id }
                val currentIds = _uiState.value.plannedStopIds
                val effectiveStopIds = if (preservePlannedStops && currentIds.isNotEmpty()) {
                    currentIds
                } else if (currentIds.isEmpty()) {
                    response.filteredItems.take(3).map { it.id }.toSet()
                } else {
                    currentIds.intersect(combinedCorridorItems.map { it.id }.toSet())
                        .ifEmpty { response.filteredItems.take(2).map { it.id }.toSet() }
                }

                val selectedWaypoints = combinedCorridorItems
                    .filter { it.id in effectiveStopIds }
                    .sortedBy { it.routeProgressFraction }

                val (trackWithStops, _) = if (selectedWaypoints.isNotEmpty()) {
                    service.computeTrackWithWaypoints(
                        origin = response.route.origin,
                        destination = response.route.destination,
                        waypoints = selectedWaypoints.map { it.point },
                        baseRoute = response.route,
                        travelMode = snapshot.travelMode,
                        corridorRadiusKm = snapshot.maxDetourKm.toDouble(),
                        apiKeys = snapshot.apiKeys
                    )
                } else {
                    response.route to emptyList()
                }

                _uiState.update { current ->
                    val initialSavedTrips = if (current.savedTrips.isEmpty() && response.filteredItems.isNotEmpty()) {
                        listOf(
                            SavedTrip(
                                id = "trip_initial_1",
                                title = "${trackWithStops.origin.name} → ${trackWithStops.destination.name} (${current.travelMode.label})",
                                dateLabel = "Geplante Wochenend-Tour",
                                origin = trackWithStops.origin,
                                destination = trackWithStops.destination,
                                trackDistanceKm = trackWithStops.distanceKm,
                                trackDurationMinutes = trackWithStops.durationMinutes,
                                elevationGainMeters = trackWithStops.elevationGainMeters,
                                elevationLossMeters = trackWithStops.elevationLossMeters,
                                maxDetourKm = current.maxDetourKm,
                                travelMode = current.travelMode,
                                stops = selectedWaypoints,
                                trackPolyline = trackWithStops.polyline,
                                notes = "Kaffeepause auf halber Strecke, Natur-Spaziergang & Abendveranstaltung am Zielort."
                            )
                        )
                    } else {
                        current.savedTrips
                    }

                    val withData = current.copy(
                        isLoading = false,
                        errorMessage = null,
                        route = trackWithStops.copy(corridorRadiusKm = current.maxDetourKm.toDouble()),
                        allCorridorItems = combinedCorridorItems,
                        apiReports = response.apiReports,
                        plannedStopIds = effectiveStopIds,
                        savedTrips = initialSavedTrips,
                        highlightedMapItemId = if (current.mapDisplayFilter == MapDisplayFilter.IN_TRACK) {
                            selectedWaypoints.firstOrNull()?.id
                        } else {
                            response.filteredItems.firstOrNull()?.id
                        },
                        activeTab = if (switchToResultsTab) AppTab.DISCOVERIES else current.activeTab
                    )
                    recomputeDisplayedItems(withData)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Fehler bei der Routen- oder API-Abfrage."
                    )
                }
            }
        }
    }

    private fun recomputeDisplayedItems(state: SeizeTheWayUiState): SeizeTheWayUiState {
        val keywords = state.keywordsQuery
            .split(",", ";", "\n")
            .flatMap { it.trim().split(Regex("\\s+")) }
            .map { it.trim().lowercase() }
            .filter { it.length >= 2 }
            .distinct()

        val maxKm = state.maxDetourKm.toDouble()
        val filtered = state.allCorridorItems
            .filter { it.detourDistanceKm <= maxKm + 0.05 }
            .map { item ->
                if (keywords.isEmpty()) {
                    item.copy(matchedKeywords = emptyList())
                } else {
                    val searchable = buildString {
                        append(item.title.lowercase()).append(" ")
                        append(item.subtitle.lowercase()).append(" ")
                        append(item.description.lowercase()).append(" ")
                        append(item.category.label.lowercase()).append(" ")
                        item.category.defaultKeywords.forEach { append(it).append(" ") }
                        item.additionalCategories.forEach { c ->
                            append(c.label.lowercase()).append(" ")
                            c.defaultKeywords.forEach { append(it).append(" ") }
                        }
                        item.tags.forEach { append(it.lowercase()).append(" ") }
                    }
                    item.copy(matchedKeywords = keywords.filter { kw -> searchable.contains(kw) })
                }
            }
            .filter { item ->
                val catOk = state.showAllCategories ||
                        item.category in state.selectedCategories ||
                        item.additionalCategories.any { it in state.selectedCategories }
                val kwOk = if (keywords.isEmpty()) true else item.matchedKeywords.isNotEmpty()
                catOk && kwOk
            }

        val sorted = when (state.sortOrder) {
            DiscoverySortOrder.ROUTE_PROGRESS -> filtered.sortedBy { it.routeProgressFraction }
            DiscoverySortOrder.MIN_DETOUR -> filtered.sortedBy { it.detourDistanceKm }
            DiscoverySortOrder.TOP_RATED -> filtered.sortedWith(
                compareByDescending<RouteDiscoveryItem> { it.matchedKeywords.size }
                    .thenByDescending { it.ratingScore }
                    .thenBy { it.detourDistanceKm }
            )
        }

        return state.copy(
            displayedItems = sorted,
            route = state.route?.copy(corridorRadiusKm = maxKm)
        )
    }
}
