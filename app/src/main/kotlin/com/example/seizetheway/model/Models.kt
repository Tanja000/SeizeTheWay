package com.example.seizetheway.model

import androidx.compose.ui.graphics.Color
import kotlin.math.*

enum class AppLanguage(val code: String, val label: String) {
    DE("de", "Deutsch"),
    EN("en", "English");

    val isGerman: Boolean get() = this == DE
}

enum class AppTab(val labelDe: String, val labelEn: String) {
    SEARCH("Suche", "Search"),
    DISCOVERIES("Ergebnisse", "Results"),
    MAP("Karte", "Map"),
    TRIPS("Touren", "Tours");

    val label: String get() = labelDe

    fun localized(lang: AppLanguage): String = if (lang.isGerman) labelDe else labelEn
}

enum class MapDisplayFilter(val label: String) {
    IN_TRACK("Im Track"),
    ALL("Alle")
}

data class GeoPoint(
    val lat: Double,
    val lon: Double,
    val name: String = ""
)

enum class TravelMode(
    val id: String,
    val label: String,
    val trackTypeLabel: String,
    val osmRoutingServer: String,
    val osrmFallbackProfile: String,
    val orsProfile: String,
    val googleMapsTravelMode: String,
    val avgSpeedKmh: Double
) {
    CAR(
        id = "car",
        label = "Auto",
        trackTypeLabel = "Auto-Track & Panorama-Route",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-car/route/v1/driving",
        osrmFallbackProfile = "driving",
        orsProfile = "driving-car",
        googleMapsTravelMode = "driving",
        avgSpeedKmh = 72.0
    ),
    BIKE(
        id = "bike",
        label = "Radfahren",
        trackTypeLabel = "Rad-Track & Radwegenetz",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-bike/route/v1/driving",
        osrmFallbackProfile = "bike",
        orsProfile = "cycling-regular",
        googleMapsTravelMode = "bicycling",
        avgSpeedKmh = 20.0
    ),
    HIKE(
        id = "hike",
        label = "Wandern",
        trackTypeLabel = "Wander-Track & Naturpfade",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-foot/route/v1/driving",
        osrmFallbackProfile = "foot",
        orsProfile = "foot-hiking",
        googleMapsTravelMode = "walking",
        avgSpeedKmh = 4.6
    )
}

enum class WayCategory(
    val id: String,
    val label: String,
    val shortLabel: String,
    val subtitle: String,
    val color: Color,
    val secondaryColor: Color,
    val defaultKeywords: List<String>
) {
    NATUR(
        id = "natur",
        label = "Natur",
        shortLabel = "Natur",
        subtitle = "Seen, Wasserfälle, Gipfel, Parks & Aussichtspunkte",
        color = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        defaultKeywords = listOf("natur", "see", "wasserfall", "aussicht", "gipfel", "park", "wald", "klamm", "berg", "felsen")
    ),
    ERLEBNIS(
        id = "erlebnis",
        label = "Erlebnis",
        shortLabel = "Erlebnis",
        subtitle = "Freizeitparks, Kletterwälder, Thermen, Zoo & Action",
        color = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFB45309),
        defaultKeywords = listOf("erlebnis", "freizeitpark", "kletterwald", "therme", "zoo", "sommerrodelbahn", "abenteuer", "wildpark", "aquarium")
    ),
    TAGESEVENTS(
        id = "tagesevents",
        label = "Tagesevents",
        shortLabel = "Events",
        subtitle = "Konzerte, Feste, Märkte, Open-Air & Live-Bühnen",
        color = Color(0xFFEC4899),
        secondaryColor = Color(0xFF9D174D),
        defaultKeywords = listOf("event", "festival", "konzert", "markt", "bühne", "veranstaltung", "ausstellung", "festspiele", "show")
    ),
    KOSTENLOSES(
        id = "kostenloses",
        label = "Kostenloses",
        shortLabel = "Gratis",
        subtitle = "Eintritt frei, Panorama-Orte, Badeseen & Kunst",
        color = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF0E7490),
        defaultKeywords = listOf("kostenlos", "gratis", "eintritt frei", "panorama", "aussichtspunkt", "park", "skulptur", "lehrpfad", "badesee")
    ),
    CAFES(
        id = "cafes",
        label = "Cafés",
        shortLabel = "Cafés",
        subtitle = "Kaffeeröstereien, Hofcafés, Kuchen & Eisdielen",
        color = Color(0xFFA855F7),
        secondaryColor = Color(0xFF6B21A8),
        defaultKeywords = listOf("cafe", "café", "kaffee", "kuchen", "rösterei", "eis", "konditorei", "espresso", "hofcafe")
    ),
    RESTAURANTS(
        id = "restaurants",
        label = "Restaurants",
        shortLabel = "Essen",
        subtitle = "Regionale Küche, Biergärten, Gasthäuser & Genuss",
        color = Color(0xFFEF4444),
        secondaryColor = Color(0xFFB91C1C),
        defaultKeywords = listOf("restaurant", "biergarten", "gasthof", "wirtshaus", "küche", "essen", "alm", "weingut", "bistro")
    ),
    WANDERUNGEN(
        id = "wanderungen",
        label = "Wanderungen",
        shortLabel = "Wandern",
        subtitle = "Wanderwege, Klammen, Berghütten & Rundwege",
        color = Color(0xFF84CC16),
        secondaryColor = Color(0xFF3F6212),
        defaultKeywords = listOf("wanderung", "wanderweg", "pfad", "klamm", "schlucht", "hütte", "alm", "rundweg", "steig", "trail")
    ),
    HISTORISCHES(
        id = "historisches",
        label = "Historisches",
        shortLabel = "Historie",
        subtitle = "Burgen, Schlösser, Ruinen, Denkmäler & Altstädte",
        color = Color(0xFFD97706),
        secondaryColor = Color(0xFF92400E),
        defaultKeywords = listOf("burg", "schloss", "ruine", "denkmal", "historisch", "altstadt", "kloster", "festung", "römer", "mittelalter")
    ),
    KULTURELLES(
        id = "kulturelles",
        label = "Kulturelles",
        shortLabel = "Kultur",
        subtitle = "Museen, Galerien, Theater, Architektur & Kultur",
        color = Color(0xFF6366F1),
        secondaryColor = Color(0xFF3730A3),
        defaultKeywords = listOf("kultur", "museum", "galerie", "theater", "kunst", "architektur", "oper", "ausstellung", "stiftung")
    )
}

enum class ApiSource(
    val id: String,
    val title: String,
    val description: String,
    val requiresApiKey: Boolean,
    val badgeColor: Color
) {
    OSRM(
        id = "osrm",
        title = "OSM / OSRM Track-Engine",
        description = "Spezifische Tracks für Auto, Radfahren & Wandern inkl. Zwischenstopps & Höhenprofil",
        requiresApiKey = false,
        badgeColor = Color(0xFF0EA5E9)
    ),
    OPEN_ROUTE_SERVICE(
        id = "ors",
        title = "OpenRouteService",
        description = "Erweitertes Routing & 3D-Höhenmeter-Tracks (Heigit / OSM)",
        requiresApiKey = true,
        badgeColor = Color(0xFF14B8A6)
    ),
    NOMINATIM(
        id = "nominatim",
        title = "Nominatim & Photon",
        description = "OpenStreetMap Geocoding für Start- & Zielorte weltweit",
        requiresApiKey = false,
        badgeColor = Color(0xFF38BDF8)
    ),
    OVERPASS(
        id = "overpass",
        title = "Overpass API (OSM)",
        description = "Echtzeit-Abfrage von POIs, Natur, Cafés, Wanderwegen & Kultur entlang des Tracks",
        requiresApiKey = false,
        badgeColor = Color(0xFF10B981)
    ),
    WIKIPEDIA_WIKIDATA(
        id = "wikipedia",
        title = "Wikidata / Wikipedia API",
        description = "Kuratierte Sehenswürdigkeiten, Bilder, Geschichte & Artikel im Routen-Korridor",
        requiresApiKey = false,
        badgeColor = Color(0xFF8B5CF6)
    ),
    GEOAPIFY_OPENTRIPMAP(
        id = "geoapify",
        title = "Geoapify / OpenTripMap",
        description = "Touristische Highlights, Natur-Spots & POIs mit Kategorien-Filter",
        requiresApiKey = true,
        badgeColor = Color(0xFFF59E0B)
    ),
    TICKETMASTER(
        id = "ticketmaster",
        title = "Ticketmaster Discovery",
        description = "Live-Tagesevents, Konzerte, Shows & Sportveranstaltungen entlang der Strecke",
        requiresApiKey = true,
        badgeColor = Color(0xFFEC4899)
    ),
    EVENTBRITE(
        id = "eventbrite",
        title = "Eventbrite API",
        description = "Regionale Events, Märkte, Festivals & Community-Erlebnisse",
        requiresApiKey = true,
        badgeColor = Color(0xFFF97316)
    ),
    WIKIDATA_EVENTS(
        id = "wikidata_events",
        title = "Wikidata Global Events (Weltweit)",
        description = "Kultur-Events, Festspiele, Festivals & Messen weltweit auf allen Kontinenten",
        requiresApiKey = false,
        badgeColor = Color(0xFFD946EF)
    )
}

data class ApiKeysConfig(
    val openRouteServiceKey: String = "",
    val geoapifyKey: String = "",
    val openTripMapKey: String = "",
    val ticketmasterKey: String = "",
    val eventbriteKey: String = ""
)

data class ApiStatusReport(
    val source: ApiSource,
    val active: Boolean,
    val resultsCount: Int,
    val statusMessage: String,
    val latencyMs: Long = 0L
)

data class ElevationStats(
    val gainMeters: Int,
    val lossMeters: Int,
    val minEleMeters: Int,
    val maxEleMeters: Int
)

data class RouteResult(
    val origin: GeoPoint,
    val destination: GeoPoint,
    val basePolyline: List<GeoPoint>,
    val polyline: List<GeoPoint>,
    val baseDistanceKm: Double,
    val distanceKm: Double,
    val baseDurationMinutes: Int,
    val durationMinutes: Int,
    val elevationGainMeters: Int,
    val elevationLossMeters: Int,
    val minElevationMeters: Int = 500,
    val maxElevationMeters: Int = 750,
    val routingEngineUsed: String,
    val corridorRadiusKm: Double,
    val travelMode: TravelMode,
    val includedWaypoints: List<GeoPoint> = emptyList()
)

data class RouteDiscoveryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: WayCategory,
    val additionalCategories: Set<WayCategory> = emptySet(),
    val point: GeoPoint,
    val detourDistanceKm: Double,
    val extraDetourMinutes: Int,
    val routeProgressKm: Double,
    val routeProgressFraction: Float,
    val isFree: Boolean = false,
    val eventDateText: String? = null,
    val openingHours: String? = null,
    val websiteUrl: String? = null,
    val wikipediaUrl: String? = null,
    val wikidataId: String? = null,
    val imageUrl: String? = null,
    val addressOrLocality: String? = null,
    val tags: List<String> = emptyList(),
    val source: ApiSource,
    val ratingScore: Double = 4.6,
    val matchedKeywords: List<String> = emptyList()
)

data class SavedTrip(
    val id: String,
    val title: String,
    val dateLabel: String,
    val origin: GeoPoint,
    val destination: GeoPoint,
    val trackDistanceKm: Double,
    val trackDurationMinutes: Int,
    val elevationGainMeters: Int,
    val elevationLossMeters: Int,
    val maxDetourKm: Float,
    val travelMode: TravelMode,
    val stops: List<RouteDiscoveryItem>,
    val trackPolyline: List<GeoPoint> = emptyList(),
    val notes: String = ""
) {
    val originName: String get() = origin.name
    val destinationName: String get() = destination.name
    val totalExtraDetourKm: Double
        get() = stops.sumOf { it.detourDistanceKm * 2.0 }
    val eventsCount: Int
        get() = stops.count { it.category == WayCategory.TAGESEVENTS || WayCategory.TAGESEVENTS in it.additionalCategories }
}

data class RoutePreset(
    val title: String,
    val originName: String,
    val destinationName: String,
    val defaultDetourKm: Float,
    val travelMode: TravelMode,
    val suggestedKeywords: String,
    val highlightText: String
)

val CAR_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Alpenvorland Roadtrip",
        originName = "München",
        destinationName = "Garmisch-Partenkirchen",
        defaultDetourKm = 12f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "See, Klamm, Aussicht, Kloster, Biergarten",
        highlightText = "Starnberger See, Murnauer Moos, Partnachklamm & Kloster Ettal"
    ),
    RoutePreset(
        title = "Elbtal & Kultur",
        originName = "Berlin",
        destinationName = "Dresden",
        defaultDetourKm = 15f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Schloss, Natur, Spreewald, Café, Museum",
        highlightText = "Spreewald-Kanäle, Schloss Moritzburg & Elbsandstein"
    ),
    RoutePreset(
        title = "Rhein & Romantik",
        originName = "Köln",
        destinationName = "Heidelberg",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Burg, Rhein, Aussicht, Wein, Historisch",
        highlightText = "Siebengebirge, Loreley, Mittelrheintal-Burgen & Altstadt"
    ),
    RoutePreset(
        title = "Schwarzwald & Bodensee",
        originName = "Stuttgart",
        destinationName = "Konstanz",
        defaultDetourKm = 14f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Burg, Wasserfall, Wanderung, Aussicht, See",
        highlightText = "Burg Hohenzollern, Donautal, Hohentwiel & Bodensee"
    )
)

val BIKE_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Blaue-Land-Radtrack",
        originName = "Starnberg",
        destinationName = "Garmisch-Partenkirchen",
        defaultDetourKm = 8f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "See, Biergarten, Aussicht, Café, Natur",
        highlightText = "Starnberger See-Radweg, Staffelsee, Murnauer Moos & Loisachtal"
    ),
    RoutePreset(
        title = "Rhein-Radweg Etappe",
        originName = "Bonn",
        destinationName = "Koblenz",
        defaultDetourKm = 6f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Burg, Rhein, Café, Historisch",
        highlightText = "Drachenfels, Remagen & Mittelrhein-Panorama-Radweg"
    ),
    RoutePreset(
        title = "Elberadweg Klassiker",
        originName = "Meißen",
        destinationName = "Dresden",
        defaultDetourKm = 7f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Schloss, Wein, Café, Kultur",
        highlightText = "Albrechtsburg, Schloss Moritzburg & Dresdner Elbwiesen"
    ),
    RoutePreset(
        title = "Hanse-Radtour",
        originName = "Hamburg",
        destinationName = "Lübeck",
        defaultDetourKm = 8f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Schloss, See, Café, Kostenlos",
        highlightText = "Wasserschloss Ahrensburg & Trave-Radweg nach Lübeck"
    )
)

val HIKE_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Wetterstein & Klamm-Steig",
        originName = "Murnau am Staffelsee",
        destinationName = "Garmisch-Partenkirchen",
        defaultDetourKm = 6f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Wanderung, Klamm, Moor, Kloster, Hütte",
        highlightText = "Murnauer Moos Bohlenweg, Kloster Ettal & Partnachklamm"
    ),
    RoutePreset(
        title = "Siebengebirge Rheinsteig",
        originName = "Bonn",
        destinationName = "Bad Honnef",
        defaultDetourKm = 5f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Burg, Aussicht, Wanderung, Natur",
        highlightText = "Drachenfels, Petersberg & Löwenburg auf dem Rheinsteig"
    ),
    RoutePreset(
        title = "Bodensee-Vulkanpfad",
        originName = "Singen",
        destinationName = "Konstanz",
        defaultDetourKm = 6f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Burg, Ruine, See, Aussicht, Wanderung",
        highlightText = "Festungsruine Hohentwiel, Untersee & Bodensee-Uferpfad"
    )
)

fun presetsForTravelMode(mode: TravelMode): List<RoutePreset> = when (mode) {
    TravelMode.CAR -> CAR_ROUTE_PRESETS
    TravelMode.BIKE -> BIKE_ROUTE_PRESETS
    TravelMode.HIKE -> HIKE_ROUTE_PRESETS
}

object GeoMath {
    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val sinLat = sin(dLat / 2)
        val sinLon = sin(dLon / 2)
        val c = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
        return 2 * EARTH_RADIUS_KM * asin(sqrt(c.coerceIn(0.0, 1.0)))
    }

    fun polylineTotalDistanceKm(polyline: List<GeoPoint>): Double {
        if (polyline.size < 2) return 0.0
        var sum = 0.0
        for (i in 1 until polyline.size) {
            sum += haversineKm(polyline[i - 1], polyline[i])
        }
        return sum
    }

    /**
     * Estimates realistic travel duration in minutes taking into account distance,
     * travel mode (Car, Bike, Hike), and cumulative elevation gain (Höhenmeter).
     */
    fun estimateDurationMinutes(
        distanceKm: Double,
        elevationGainMeters: Int,
        travelMode: TravelMode
    ): Int = when (travelMode) {
        TravelMode.CAR -> ((distanceKm / travelMode.avgSpeedKmh) * 60.0 + (elevationGainMeters / 350.0)).roundToInt().coerceAtLeast(1)
        TravelMode.BIKE -> ((distanceKm / travelMode.avgSpeedKmh) * 60.0 + (elevationGainMeters / 12.0)).roundToInt().coerceAtLeast(1)
        // DIN 33466 / SAC hiking formula: ~4.5 km/h horizontal + 1h per 350m ascent
        TravelMode.HIKE -> ((distanceKm / 4.5) * 60.0 + (elevationGainMeters / 350.0) * 60.0).roundToInt().coerceAtLeast(1)
    }

    data class RouteProjection(
        val minDistanceKm: Double,
        val routeProgressKm: Double,
        val routeProgressFraction: Float,
        val closestRoutePoint: GeoPoint
    )

    fun projectOntoRoute(p: GeoPoint, polyline: List<GeoPoint>): RouteProjection {
        if (polyline.isEmpty()) {
            return RouteProjection(0.0, 0.0, 0f, p)
        }
        if (polyline.size == 1) {
            val d = haversineKm(p, polyline.first())
            return RouteProjection(d, 0.0, 0f, polyline.first())
        }

        val cumulativeKm = DoubleArray(polyline.size)
        var totalKm = 0.0
        for (i in 1 until polyline.size) {
            totalKm += haversineKm(polyline[i - 1], polyline[i])
            cumulativeKm[i] = totalKm
        }
        if (totalKm <= 0.001) {
            val d = haversineKm(p, polyline.first())
            return RouteProjection(d, 0.0, 0f, polyline.first())
        }

        var bestDistKm = Double.MAX_VALUE
        var bestProgressKm = 0.0
        var bestClosestPoint = polyline.first()

        for (i in 0 until polyline.size - 1) {
            val a = polyline[i]
            val b = polyline[i + 1]
            val cosLat = cos(Math.toRadians((a.lat + b.lat + p.lat) / 3.0))
            val ax = a.lon * cosLat
            val ay = a.lat
            val bx = b.lon * cosLat
            val by = b.lat
            val px = p.lon * cosLat
            val py = p.lat

            val dx = bx - ax
            val dy = by - ay
            val lenSq = dx * dx + dy * dy
            val t = if (lenSq > 1e-12) {
                (((px - ax) * dx + (py - ay) * dy) / lenSq).coerceIn(0.0, 1.0)
            } else {
                0.0
            }

            val projLat = a.lat + t * (b.lat - a.lat)
            val projLon = a.lon + t * (b.lon - a.lon)
            val projPoint = GeoPoint(projLat, projLon)
            val distKm = haversineKm(p, projPoint)
            if (distKm < bestDistKm) {
                bestDistKm = distKm
                val segLen = cumulativeKm[i + 1] - cumulativeKm[i]
                bestProgressKm = cumulativeKm[i] + t * segLen
                bestClosestPoint = projPoint
            }
        }

        val fraction = (bestProgressKm / totalKm).toFloat().coerceIn(0f, 1f)
        return RouteProjection(
            minDistanceKm = bestDistKm,
            routeProgressKm = bestProgressKm,
            routeProgressFraction = fraction,
            closestRoutePoint = bestClosestPoint
        )
    }

    fun sampleAnchorPointsAlongRoute(polyline: List<GeoPoint>, maxSamples: Int = 7): List<GeoPoint> {
        if (polyline.size <= maxSamples) return polyline
        val cumulativeKm = DoubleArray(polyline.size)
        var totalKm = 0.0
        for (i in 1 until polyline.size) {
            totalKm += haversineKm(polyline[i - 1], polyline[i])
            cumulativeKm[i] = totalKm
        }
        if (totalKm <= 1.0) return listOf(polyline.first(), polyline.last())

        val result = mutableListOf<GeoPoint>()
        for (s in 0 until maxSamples) {
            val targetKm = totalKm * (s.toDouble() / (maxSamples - 1).coerceAtLeast(1))
            val idx = cumulativeKm.indexOfFirst { it >= targetKm }.coerceAtLeast(0)
            result.add(polyline[idx])
        }
        return result.distinctBy { "${(it.lat * 100).roundToInt()}_${(it.lon * 100).roundToInt()}" }
    }
}
