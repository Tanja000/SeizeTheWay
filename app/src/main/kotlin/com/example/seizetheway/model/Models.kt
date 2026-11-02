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

enum class MapDisplayFilter(val labelDe: String, val labelEn: String) {
    IN_TRACK("Im Track", "In Track"),
    ALL("Alle", "All");

    val label: String get() = labelDe
    fun localized(lang: AppLanguage): String = if (lang.isGerman) labelDe else labelEn
}

data class GeoPoint(
    val lat: Double,
    val lon: Double,
    val name: String = ""
)

enum class TravelMode(
    val id: String,
    val label: String,
    val labelEn: String,
    val trackTypeLabel: String,
    val trackTypeLabelEn: String,
    val osmRoutingServer: String,
    val osrmFallbackProfile: String,
    val orsProfile: String,
    val googleMapsTravelMode: String,
    val avgSpeedKmh: Double
) {
    CAR(
        id = "car",
        label = "Auto",
        labelEn = "Car",
        trackTypeLabel = "Auto-Track & Panorama-Route",
        trackTypeLabelEn = "Car Track & Scenic Route",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-car/route/v1/driving",
        osrmFallbackProfile = "driving",
        orsProfile = "driving-car",
        googleMapsTravelMode = "driving",
        avgSpeedKmh = 72.0
    ),
    BIKE(
        id = "bike",
        label = "Rad",
        labelEn = "Bike",
        trackTypeLabel = "Rad-Track & Radwegenetz",
        trackTypeLabelEn = "Bike Track & Cycle Network",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-bike/route/v1/driving",
        osrmFallbackProfile = "bike",
        orsProfile = "cycling-regular",
        googleMapsTravelMode = "bicycling",
        avgSpeedKmh = 20.0
    ),
    HIKE(
        id = "hike",
        label = "Wandern",
        labelEn = "Hike",
        trackTypeLabel = "Wander-Track & Naturpfade",
        trackTypeLabelEn = "Hiking Track & Nature Trails",
        osmRoutingServer = "https://routing.openstreetmap.de/routed-foot/route/v1/driving",
        osrmFallbackProfile = "foot",
        orsProfile = "foot-hiking",
        googleMapsTravelMode = "walking",
        avgSpeedKmh = 4.6
    );

    fun localizedLabel(lang: AppLanguage): String = if (lang.isGerman) label else labelEn
    fun localizedTrackType(lang: AppLanguage): String = if (lang.isGerman) trackTypeLabel else trackTypeLabelEn
}

enum class WayCategory(
    val id: String,
    val label: String,
    val labelEn: String,
    val shortLabel: String,
    val shortLabelEn: String,
    val subtitle: String,
    val subtitleEn: String,
    val color: Color,
    val secondaryColor: Color,
    val defaultKeywords: List<String>
) {
    NATUR(
        id = "natur",
        label = "Natur",
        labelEn = "Nature",
        shortLabel = "Natur",
        shortLabelEn = "Nature",
        subtitle = "Seen, Wasserfälle, Gipfel, Parks & Aussichtspunkte",
        subtitleEn = "Lakes, waterfalls, peaks, parks & scenic viewpoints",
        color = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        defaultKeywords = listOf("natur", "see", "wasserfall", "aussicht", "gipfel", "park", "wald", "klamm", "berg", "felsen")
    ),
    ERLEBNIS(
        id = "erlebnis",
        label = "Erlebnis",
        labelEn = "Adventure",
        shortLabel = "Erlebnis",
        shortLabelEn = "Adventure",
        subtitle = "Freizeitparks, Kletterwälder, Thermen, Zoo & Action",
        subtitleEn = "Theme parks, climbing forests, thermal baths, zoo & action",
        color = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFB45309),
        defaultKeywords = listOf("erlebnis", "freizeitpark", "kletterwald", "therme", "zoo", "sommerrodelbahn", "abenteuer", "wildpark", "aquarium")
    ),
    TAGESEVENTS(
        id = "tagesevents",
        label = "Tagesevents",
        labelEn = "Events",
        shortLabel = "Events",
        shortLabelEn = "Events",
        subtitle = "Konzerte, Feste, Märkte, Open-Air & Live-Bühnen",
        subtitleEn = "Concerts, festivals, markets, open-air & live stages",
        color = Color(0xFFEC4899),
        secondaryColor = Color(0xFF9D174D),
        defaultKeywords = listOf("event", "festival", "konzert", "markt", "bühne", "veranstaltung", "ausstellung", "festspiele", "show")
    ),
    KOSTENLOSES(
        id = "kostenloses",
        label = "Kostenloses",
        labelEn = "Free",
        shortLabel = "Gratis",
        shortLabelEn = "Free",
        subtitle = "Eintritt frei, Panorama-Orte, Badeseen & Kunst",
        subtitleEn = "Free admission, panorama spots, swimming lakes & art",
        color = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF0E7490),
        defaultKeywords = listOf("kostenlos", "gratis", "eintritt frei", "panorama", "aussichtspunkt", "park", "skulptur", "lehrpfad", "badesee")
    ),
    CAFES(
        id = "cafes",
        label = "Cafés",
        labelEn = "Cafés",
        shortLabel = "Cafés",
        shortLabelEn = "Cafés",
        subtitle = "Kaffeeröstereien, Hofcafés, Kuchen & Eisdielen",
        subtitleEn = "Coffee roasteries, farm cafés, cake & ice cream",
        color = Color(0xFFA855F7),
        secondaryColor = Color(0xFF6B21A8),
        defaultKeywords = listOf("cafe", "café", "kaffee", "kuchen", "rösterei", "eis", "konditorei", "espresso", "hofcafe")
    ),
    RESTAURANTS(
        id = "restaurants",
        label = "Restaurants",
        labelEn = "Restaurants",
        shortLabel = "Essen",
        shortLabelEn = "Dining",
        subtitle = "Regionale Küche, Biergärten, Gasthäuser & Genuss",
        subtitleEn = "Regional cuisine, beer gardens, inns & culinary delights",
        color = Color(0xFFEF4444),
        secondaryColor = Color(0xFFB91C1C),
        defaultKeywords = listOf("restaurant", "biergarten", "gasthof", "wirtshaus", "küche", "essen", "alm", "weingut", "bistro")
    ),
    WANDERUNGEN(
        id = "wanderungen",
        label = "Wanderungen",
        labelEn = "Hikes",
        shortLabel = "Wandern",
        shortLabelEn = "Hiking",
        subtitle = "Wanderwege, Klammen, Berghütten & Rundwege",
        subtitleEn = "Hiking trails, gorges, mountain huts & loop trails",
        color = Color(0xFF84CC16),
        secondaryColor = Color(0xFF3F6212),
        defaultKeywords = listOf("wanderung", "wanderweg", "pfad", "klamm", "schlucht", "hütte", "alm", "rundweg", "steig", "trail")
    ),
    HISTORISCHES(
        id = "historisches",
        label = "Historisches",
        labelEn = "Historic",
        shortLabel = "Historie",
        shortLabelEn = "History",
        subtitle = "Burgen, Schlösser, Ruinen, Denkmäler & Altstädte",
        subtitleEn = "Castles, palaces, ruins, monuments & old towns",
        color = Color(0xFFD97706),
        secondaryColor = Color(0xFF92400E),
        defaultKeywords = listOf("burg", "schloss", "ruine", "denkmal", "historisch", "altstadt", "kloster", "festung", "römer", "mittelalter")
    ),
    KULTURELLES(
        id = "kulturelles",
        label = "Kulturelles",
        labelEn = "Culture",
        shortLabel = "Kultur",
        shortLabelEn = "Culture",
        subtitle = "Museen, Galerien, Theater, Architektur & Kultur",
        subtitleEn = "Museums, galleries, theaters, architecture & arts",
        color = Color(0xFF6366F1),
        secondaryColor = Color(0xFF3730A3),
        defaultKeywords = listOf("kultur", "museum", "galerie", "theater", "kunst", "architektur", "oper", "ausstellung", "stiftung")
    );

    fun localizedLabel(lang: AppLanguage): String = if (lang.isGerman) label else labelEn
    fun localizedSubtitle(lang: AppLanguage): String = if (lang.isGerman) subtitle else subtitleEn
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
    val titleEn: String = title,
    val originName: String,
    val destinationName: String,
    val defaultDetourKm: Float,
    val travelMode: TravelMode,
    val suggestedKeywords: String,
    val highlightText: String,
    val highlightTextEn: String = highlightText,
    val originLat: Double = 0.0,
    val originLon: Double = 0.0,
    val approxDistanceKm: Int = 25
) {
    fun localizedTitle(lang: AppLanguage): String = if (lang.isGerman) title else titleEn
    fun localizedHighlight(lang: AppLanguage): String = if (lang.isGerman) highlightText else highlightTextEn
}

data class RegionalExcursionSpot(
    val name: String,
    val lat: Double,
    val lon: Double,
    val titleDe: String,
    val titleEn: String,
    val keywords: String,
    val highlightDe: String,
    val highlightEn: String
)

val REGIONAL_EXCURSION_SPOTS = listOf(
    // Oberbayern & Schwaben & Allgäu
    RegionalExcursionSpot("Starnberg", 47.9972, 11.3406, "Starnberger See Panorama", "Lake Starnberg Panorama", "See, Aussicht, Biergarten, Schloss", "Seepromenade, Schloss Berg & Alpenblick", "Lakefront promenade, Berg Castle & Alpine view"),
    RegionalExcursionSpot("Herrsching am Ammersee", 47.9992, 11.1714, "Ammersee & Kloster Andechs", "Lake Ammersee & Andechs Abbey", "See, Kloster, Biergarten, Natur", "Ammersee-Uferweg & Klosterberg Andechs", "Ammersee shore path & Andechs Abbey"),
    RegionalExcursionSpot("Bad Tölz", 47.7606, 11.5606, "Isarwinkel & Kalvarienberg", "Isar Valley & Kalvarienberg", "Fluss, Altstadt, Aussicht, Natur", "Historische Marktstraße, Isarauen & Kalvarienberg", "Historic Market Street, Isar meadows & view"),
    RegionalExcursionSpot("Tegernsee", 47.7142, 11.7583, "Tegernseer Tal & Bräustüberl", "Lake Tegernsee Valley", "See, Brauerei, Aussicht, Wanderung", "Seerundweg, Herzogliches Bräustüberl & Wallbergblick", "Lake trail, ducal brewery & mountain view"),
    RegionalExcursionSpot("Wolfratshausen", 47.9136, 11.4278, "Isar-Loisach-Auen Track", "Isar-Loisach River Track", "Fluss, Natur, Biergarten, Familie", "Pupplinger Au, Flößerstadt & Isarmündung", "Pupplinger Au nature reserve & river confluence"),
    RegionalExcursionSpot("Dachau", 48.2603, 11.4342, "Schloss Dachau & Hofgarten", "Dachau Palace & Court Garden", "Schloss, Aussicht, Altstadt, Kultur", "Renaissanceschloss, Hofgarten-Blick & Altstadt", "Renaissance palace, court garden & old town"),
    RegionalExcursionSpot("Freising", 48.4028, 11.7489, "Domberg & Weihenstephan", "Cathedral Hill & Weihenstephan", "Kloster, Brauerei, Garten, Historisch", "Freisinger Domberg, Staudengarten & älteste Brauerei", "Cathedral hill, botanical garden & oldest brewery"),
    RegionalExcursionSpot("Fürstenfeldbruck", 48.1778, 11.2556, "Kloster Fürstenfeld & Amper", "Fürstenfeld Abbey & Amper River", "Kloster, Fluss, Café, Kultur", "Barockkloster Fürstenfeld & Amper-Auenweg", "Baroque Fürstenfeld Abbey & Amper river trail"),
    RegionalExcursionSpot("Wasserburg am Inn", 48.0611, 12.2314, "Innschleife & Altstadt", "Inn River Loop & Old Town", "Altstadt, Fluss, Burg, Café", "Malerische Altstadt-Halbinsel in der Innschleife", "Picturesque Old Town peninsula in the Inn loop"),
    RegionalExcursionSpot("Landsberg am Lech", 48.0528, 10.8689, "Lechwehr & Historische Altstadt", "Lech Weir & Historic Old Town", "Altstadt, Fluss, Turm, Café", "Karolinenwehr, Bayertor & Lech-Uferpfad", "Karolinen weir, Bavarian Gate & Lech river path"),
    RegionalExcursionSpot("Friedberg (Bayern)", 48.3561, 10.9856, "Wittelsbacher Schloss & See", "Wittelsbach Castle & Lake", "Schloss, See, Altstadt, Café", "Wittelsbacher Schloss, Friedberger See & Altstadt", "Wittelsbach Castle, Friedberg Lake & Old Town"),
    RegionalExcursionSpot("Aichach", 48.4589, 11.1294, "Burgplatz Oberwittelsbach", "Oberwittelsbach Castle Site", "Burg, Wald, Historisch, Wanderung", "Stammburg der Wittelsbacher & Paar-Tal", "Ancestral Wittelsbach castle site & Paar valley"),
    RegionalExcursionSpot("Königsbrunn", 48.2694, 10.8894, "Lechfeld & Ilsesee", "Lechfeld & Lake Ilse", "See, Natur, Radweg, Kostenlos", "Lechauen, Mandichosee & Ilsesee-Rundweg", "Lech floodplains, Lake Mandicho & Lake Ilse"),
    RegionalExcursionSpot("Murnau am Staffelsee", 47.6769, 11.2023, "Staffelsee & Murnauer Moos", "Lake Staffelsee & Murnau Moor", "See, Moor, Kunst, Aussicht", "Murnauer Moos Bohlenweg, Schlossmuseum & Seeufer", "Murnau Moor boardwalk, castle museum & lake"),
    RegionalExcursionSpot("Garmisch-Partenkirchen", 47.4921, 11.0958, "Partnachklamm & Wetterstein", "Partnach Gorge & Wetterstein", "Klamm, Berg, Aussicht, Wanderung", "Partnachklamm, Eibsee-Blick & historische Ludwigstraße", "Partnach Gorge, Alpine panorama & Ludwigstraße"),
    RegionalExcursionSpot("Füssen", 47.5714, 10.7018, "Königsschlösser & Lechfall", "Royal Castles & Lech Waterfall", "Schloss, Wasserfall, See, Aussicht", "Schloss Neuschwanstein, Alpsee & Lechfall", "Neuschwanstein Castle, Lake Alpsee & Lechfall"),
    RegionalExcursionSpot("Chiemsee (Prien)", 47.8542, 12.3458, "Chiemsee & Herreninsel", "Lake Chiemsee & Herrenchiemsee", "See, Schloss, Insel, Boot", "Schloss Herrenchiemsee, Fraueninsel & Uferweg", "Herrenchiemsee Palace, Fraueninsel & shore trail"),
    // Franken & Ostbayern
    RegionalExcursionSpot("Forchheim", 49.7197, 11.0581, "Tor zur Fränkischen Schweiz", "Gateway to Franconian Switzerland", "Burg, Kellerwald, Fachwerk, Biergarten", "Kaiserpfalz, Königsbad & berühmter Kellerwald", "Imperial palace, timbered town & beer cellars"),
    RegionalExcursionSpot("Erlangen", 49.5897, 11.0039, "Schlossgarten & Bergkirchweih-Kellern", "Palace Garden & Rock Cellars", "Park, Schloss, Botanik, Café", "Markgräflicher Schlossgarten, Orangerie & Burgberg", "Margravial palace garden, orangery & castle hill"),
    RegionalExcursionSpot("Lauf an der Pegnitz", 49.5103, 11.2772, "Wenzelschloss & Pegnitztal", "Wenzel Castle & Pegnitz Valley", "Burg, Fluss, Altstadt, Historisch", "Wasserburg Wenzelschloss auf der Pegnitzinsel", "Wenzel moated castle on the Pegnitz island"),
    RegionalExcursionSpot("Roth", 49.2464, 11.0919, "Rothsee & Schloss Ratibor", "Lake Rothsee & Ratibor Castle", "See, Schloss, Natur, Radweg", "Rothsee-Uferrundweg & Renaissanceschloss Ratibor", "Lake Rothsee loop & Renaissance Castle Ratibor"),
    RegionalExcursionSpot("Kelheim", 48.9183, 11.8742, "Donaudurchbruch & Befreiungshalle", "Danube Gorge & Liberation Hall", "Fluss, Kloster, Aussicht, Wanderung", "Befreiungshalle, Donaudurchbruch & Kloster Weltenburg", "Liberation Hall, Danube Gorge & Weltenburg Abbey"),
    RegionalExcursionSpot("Kallmünz", 49.1614, 11.9589, "Künstlerort & Burgruine Kallmünz", "Artists' Town & Kallmünz Castle", "Burg, Fluss, Aussicht, Kunst", "Burgruine über Naab & Vils im malerischen Künstlerort", "Castle ruins above the Naab & Vils confluence"),
    // Baden-Württemberg
    RegionalExcursionSpot("Esslingen am Neckar", 48.7406, 9.3108, "Esslinger Burg & Weinberge", "Esslingen Castle & Vineyards", "Burg, Wein, Fachwerk, Aussicht", "Mittelalterliche Altstadt, Esslinger Burg & Neckar", "Medieval old town, Esslingen Castle & Neckar"),
    RegionalExcursionSpot("Ludwigsburg", 48.8975, 9.1919, "Residenzschloss & Blühendes Barock", "Residential Palace & Baroque Gardens", "Schloss, Park, Kultur, Café", "Residenzschloss Ludwigsburg, Seeschloss Monrepos & Park", "Ludwigsburg Palace, Monrepos Lake Castle & park"),
    RegionalExcursionSpot("Tübingen", 48.5216, 9.0576, "Schloss Hohentübingen & Neckarinsel", "Hohentübingen Castle & Neckar Island", "Schloss, Altstadt, Fluss, Boot", "Neckarfront, Platanenallee & Schloss Hohentübingen", "Neckar riverfront, plane tree avenue & castle"),
    RegionalExcursionSpot("Marbach am Neckar", 48.9397, 9.2597, "Schillerhöhe & Neckarschleife", "Schiller Heights & Neckar Loop", "Museum, Aussicht, Wein, Literatur", "Schiller-Nationalmuseum, Fachwerk & Weinberge", "Schiller National Museum, timbered town & vineyards"),
    RegionalExcursionSpot("Schwetzingen", 49.3858, 8.5719, "Schloss & Schlossgarten Schwetzingen", "Schwetzingen Palace & Gardens", "Schloss, Park, Kultur, Café", "Weltberühmter Schlossgarten & Rokokotheater", "World-famous palace gardens & Rococo theater"),
    RegionalExcursionSpot("Neckargemünd", 49.3931, 8.7975, "Burgfeste Dilsberg & Neckartal", "Dilsberg Fortress & Neckar Valley", "Burg, Aussicht, Wanderung, Fluss", "Bergfeste Dilsberg, Neckarsteig & Altstadt", "Dilsberg mountain fortress, Neckarsteig & old town"),
    RegionalExcursionSpot("Baden-Baden", 48.7606, 8.2397, "Lichtentaler Allee & Merkurberg", "Lichtentaler Allee & Mount Merkur", "Park, Burg, Aussicht, Kultur", "Kurhaus, Lichtentaler Allee & Altes Schloss Hohenbaden", "Kurhaus, Lichtentaler Allee & Hohenbaden Castle"),
    RegionalExcursionSpot("Titisee-Neustadt", 47.9022, 8.1567, "Titisee & Hochschwarzwald", "Lake Titisee & Black Forest", "See, Wald, Aussicht, Wanderung", "Titisee-Uferrundweg, Feldbergblick & Schwarzwaldmühlen", "Lake Titisee shore trail & Black Forest panorama"),
    RegionalExcursionSpot("Überlingen", 47.7667, 9.1719, "Bodensee-Promenade & Pfahlbauten", "Lake Constance & Stilt Houses", "See, Garten, Kloster, Aussicht", "Längste Bodensee-Promenade, Birnau & Insel Mainau", "Lake Constance promenade, Birnau & Mainau Island"),
    RegionalExcursionSpot("Blaubeuren", 48.4119, 9.7844, "Blautopf & Rusenschloss", "Blautopf Spring & Rusenschloss", "See, Höhle, Burg, Wanderung", "Türkisblauer Blautopf, Kloster Blaubeuren & Felsensteig", "Turquoise Blautopf karst spring, abbey & rock trail"),
    // Hessen & Rhein-Main
    RegionalExcursionSpot("Kronberg im Taunus", 50.1806, 8.5086, "Burg Kronberg & Opel-Zoo", "Kronberg Castle & Taunus Hills", "Burg, Wald, Aussicht, Familie", "Burg Kronberg, Viktoriapark & Altkönig-Blick", "Kronberg Castle, Victoria Park & Taunus view"),
    RegionalExcursionSpot("Bad Homburg vor der Höhe", 50.2269, 8.6181, "Landgrafenschloss & Saalburg", "Landgrave Castle & Roman Fort", "Schloss, Römer, Park, Kultur", "Schloss Bad Homburg, Kurpark & Römerkastell Saalburg", "Bad Homburg Castle, spa park & Saalburg Roman fort"),
    RegionalExcursionSpot("Eltville am Rhein", 50.0258, 8.1189, "Rheingau & Kloster Eberbach", "Rheingau & Eberbach Abbey", "Kloster, Wein, Burg, Rhein", "Kurfürstliche Burg, Rosengarten & Kloster Eberbach", "Electoral castle, rose garden & Eberbach Abbey"),
    RegionalExcursionSpot("Rüdesheim am Rhein", 49.9789, 7.9239, "Niederwalddenkmal & Drosselgasse", "Niederwald Monument & Rhine", "Aussicht, Wein, Burg, Rhein", "Niederwalddenkmal, Seilbahn & Rheingau-Weinberge", "Niederwald Monument, cable car & vineyards"),
    RegionalExcursionSpot("Seligenstadt", 50.0439, 8.9750, "Einhard-Basilika & Mainufer", "Einhard Basilica & Main River", "Kloster, Fachwerk, Fluss, Café", "Benediktinerabtei, Klostergarten, Mainfähre & Altstadt", "Benedictine abbey, monastery garden & Main ferry"),
    RegionalExcursionSpot("Darmstadt", 49.8728, 8.6512, "Mathildenhöhe & Burg Frankenstein", "Mathildenhöhe & Frankenstein Castle", "Kunst, Burg, Park, Aussicht", "UNESCO-Welterbe Mathildenhöhe, Park Rosenhöhe & Burg", "UNESCO Mathildenhöhe, Rose Heights & castle"),
    // NRW & Rheinland
    RegionalExcursionSpot("Brühl", 50.8278, 6.9047, "UNESCO-Schlösser Augustusburg", "UNESCO Augustusburg Palaces", "Schloss, Park, See, Kultur", "Schloss Augustusburg, Jagdschloss Falkenlust & Schlosspark", "Augustusburg Palace, Falkenlust & baroque gardens"),
    RegionalExcursionSpot("Königswinter", 50.6753, 7.1939, "Drachenfels & Siebengebirge", "Drachenfels & Siebengebirge", "Burg, Aussicht, Rhein, Wanderung", "Schloss Drachenburg, Drachenfels-Plateau & Rheinpromenade", "Drachenburg Castle, Drachenfels & Rhine promenade"),
    RegionalExcursionSpot("Bad Honnef", 50.6451, 7.2272, "Insel Grafenwerth & Löwenburg", "Grafenwerth Island & Löwenburg", "Insel, Rhein, Burg, Biergarten", "Rheininsel Grafenwerth, Rheinsteig & Löwenburg", "Rhine island Grafenwerth, Rheinsteig & castle"),
    RegionalExcursionSpot("Zons (Dormagen)", 51.1211, 6.8481, "Mittelalterliche Zollfeste Zons", "Medieval Fortress Town Zons", "Burg, Rhein, Stadtmauer, Café", "Komplett erhaltene Rhein-Zollfeste mit Windmühle", "Preserved medieval Rhine customs fortress & mill"),
    RegionalExcursionSpot("Kettwig (Essen)", 51.3636, 6.9403, "Baldeneysee & Altstadt Kettwig", "Lake Baldeney & Kettwig Old Town", "See, Schloss, Fachwerk, Fluss", "Villa Hügel, Baldeneysee-Uferweg & Fachwerk-Altstadt", "Villa Hügel, Lake Baldeney trail & timbered old town"),
    RegionalExcursionSpot("Hattingen", 51.3989, 7.1856, "Burg Blankenstein & Ruhrtal", "Blankenstein Castle & Ruhr Valley", "Burg, Fachwerk, Fluss, Industriekultur", "Historische Altstadt, Burg Blankenstein & RuhrtalRadweg", "Historic old town, Blankenstein Castle & Ruhr trail"),
    // Berlin & Brandenburg
    RegionalExcursionSpot("Potsdam", 52.3906, 13.0645, "Schloss Sanssouci & Havelseen", "Sanssouci Palace & Havel Lakes", "Schloss, Park, See, Kultur", "Park Sanssouci, Neues Palais, Cecilienhof & Holländisches Viertel", "Sanssouci Park, New Palace & Dutch Quarter"),
    RegionalExcursionSpot("Wannsee & Pfaueninsel", 52.4294, 13.1408, "Großer Wannsee & Pfaueninsel", "Lake Wannsee & Peacock Island", "See, Insel, Schloss, Aussicht", "Liebermann-Villa, Pfaueninsel, Grunewaldturm & Havelufer", "Liebermann Villa, Peacock Island & Grunewald Tower"),
    RegionalExcursionSpot("Köpenick & Müggelsee", 52.4453, 13.6428, "Großer Müggelsee & Müggelberge", "Lake Müggelsee & Müggel Hills", "See, Aussicht, Schloss, Natur", "Schloss Köpenick, Müggelturm-Panorama & Spreetunnel", "Köpenick Palace, Müggel Tower view & Lake Müggelsee"),
    RegionalExcursionSpot("Oranienburg", 52.7544, 13.2369, "Schloss Oranienburg & Lehnitzsee", "Oranienburg Palace & Lake", "Schloss, Park, See, Radweg", "Barockschloss Oranienburg, Schlosspark & Havelradweg", "Baroque Oranienburg Palace, park & Havel cycle path"),
    RegionalExcursionSpot("Bernau bei Berlin", 52.6792, 13.5878, "Hussitenstadt & Liepnitzsee", "Bernau Old Town & Lake Liepnitz", "See, Stadtmauer, Wald, Wanderung", "Mittelalterliche Stadtmauer & glasklarer Liepnitzsee", "Medieval town wall & crystal-clear Lake Liepnitz"),
    // Norddeutschland (Hamburg, Bremen, Niedersachsen, Ostsee)
    RegionalExcursionSpot("Ahrensburg", 53.6744, 10.2408, "Wasserschloss Ahrensburg", "Ahrensburg Moated Castle", "Schloss, Park, Natur, Café", "Weißes Renaissance-Wasserschloss & Schlosspark", "White Renaissance moated castle & park"),
    RegionalExcursionSpot("Blankenese (Elbufer)", 53.5583, 9.8111, "Treppenviertel & Elbstrand", "Stair Quarter & Elbe Beach", "Aussicht, Strand, Park, Café", "Malerisches Treppenviertel, Leuchtturm & Falkensteiner Ufer", "Picturesque Stair Quarter, lighthouse & Elbe beach"),
    RegionalExcursionSpot("Lüneburg", 53.2464, 10.4115, "Salzstadt Lüneburg & Ilmenau", "Historic Salt Town Lüneburg", "Altstadt, Historisch, Fluss, Café", "Alter Kran im Wasserviertel, Backsteingotik & Kalkberg", "Historic crane, brick Gothic architecture & Kalkberg"),
    RegionalExcursionSpot("Jork (Altes Land)", 53.5347, 9.6819, "Altes Land & Elbdeich", "Altes Land Orchards & Elbe Dike", "Natur, Fachwerk, Fluss, Café", "Prachtvolle Fachwerkhöfe, Obstgärten & Lühe-Anleger", "Half-timbered farmsteads, orchards & Elbe pier"),
    RegionalExcursionSpot("Steinhude (Wunstorf)", 52.4539, 9.3567, "Steinhuder Meer & Insel Wilhelmstein", "Lake Steinhude & Island Fortress", "See, Insel, Festung, Natur", "Uferpromenade, Insel Wilhelmstein & Moorhütten", "Lakeside promenade, Wilhelmstein Island & moor trail"),
    // Sachsen & Mitteldeutschland
    RegionalExcursionSpot("Meißen", 51.1636, 13.4775, "Albrechtsburg & Elbweinberge", "Albrechtsburg & Elbe Vineyards", "Burg, Wein, Porzellan, Elbe", "Albrechtsburg, Dom zu Meißen & Porzellan-Manufaktur", "Albrechtsburg Castle, Cathedral & Porcelain Manufactory"),
    RegionalExcursionSpot("Moritzburg", 51.1672, 13.6794, "Jagdschloss Moritzburg & Leuchtturm", "Moritzburg Castle & Lighthouse", "Schloss, See, Wald, Kultur", "Barockes Wasserschloss Moritzburg & Fasanenschlösschen", "Baroque moated castle Moritzburg & Pheasant Castle"),
    RegionalExcursionSpot("Pirna & Bastei", 50.9622, 13.9419, "Basteibrücke & Sächsische Schweiz", "Bastei Bridge & Saxon Switzerland", "Felsen, Aussicht, Burg, Wanderung", "Basteifelsen, Felsenburg Neurathen & Canaletto-Altstadt", "Bastei rock bridge, Neurathen castle & Pirna old town"),
    RegionalExcursionSpot("Markkleeberg", 51.2783, 12.3719, "Cospudener See & Neuseenland", "Lake Cospuden & Lake District", "See, Aussicht, Strand, Radweg", "Cospudener See, Bistumshöhe-Aussichtsturm & Pier 1", "Lake Cospuden, Bistumshöhe observation tower & marina"),
    // Österreich & Schweiz
    RegionalExcursionSpot("Baden bei Wien", 48.0069, 16.2308, "Helenental & Kurstadt Baden", "Helenental & Spa Town Baden", "Park, Burg, Wein, Wanderung", "Kurpark, Rosarium, Ruine Rauhenstein & Wienerwald", "Spa park, rose garden, Rauhenstein ruins & Vienna Woods"),
    RegionalExcursionSpot("Klosterneuburg", 48.3053, 16.3256, "Stift Klosterneuburg & Donau", "Klosterneuburg Abbey & Danube", "Kloster, Wein, Aussicht, Donau", "Augustiner-Chorherrenstift, Verduner Altar & Leopoldsberg", "Klosterneuburg Monastery, Danube trail & Leopoldsberg"),
    RegionalExcursionSpot("Hallein", 47.6833, 13.0994, "Salzwelten & Untersbergblick", "Salt Worlds & Salzach Valley", "Bergwerk, Altstadt, Berg, Natur", "Keltenstadt am Dürrnberg, Salzach-Uferweg & Hellbrunn", "Celtic salt town, Salzach river path & Hellbrunn Palace"),
    RegionalExcursionSpot("Rapperswil-Jona", 47.2267, 8.8183, "Zürichsee & Schloss Rapperswil", "Lake Zurich & Rapperswil Castle", "See, Schloss, Holzbrücke, Rosen", "Rosenstadt am Zürichsee, mittelalterliches Schloss & Holzsteg", " Town of roses on Lake Zurich, castle & wooden bridge"),
    RegionalExcursionSpot("Baden (Aargau)", 47.4733, 8.3058, "Ruine Stein & Limmattal", "Stein Castle Ruins & Limmat", "Burg, Therme, Altstadt, Aussicht", "Hochwacht Ruine Stein, historische Holzbrücke & Bäderquartier", "Stein Castle ruins, historic wooden bridge & thermal baths")
)

val CAR_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Fünf-Seen-Land Panoramatour",
        titleEn = "Five Lakes Panorama Drive",
        originName = "München",
        destinationName = "Herrsching am Ammersee",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "See, Kloster, Aussicht, Biergarten, Schloss",
        highlightText = "Starnberger See, Kloster Andechs & Ammersee-Promenade (im 50 km Umkreis)",
        highlightTextEn = "Lake Starnberg, Andechs Abbey & Ammersee Promenade (within 50 km)",
        originLat = 48.137154,
        originLon = 11.576124,
        approxDistanceKm = 38
    ),
    RoutePreset(
        title = "Isarwinkel & Voralpen-Tour",
        titleEn = "Isar Valley & Foothills Tour",
        originName = "München",
        destinationName = "Bad Tölz",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Fluss, Aussicht, Altstadt, Kloster, Natur",
        highlightText = "Isartal, Kloster Schäftlarn & historische Marktstraße Bad Tölz",
        highlightTextEn = "Isar Valley, Schäftlarn Abbey & Historic Bad Tölz",
        originLat = 48.137154,
        originLon = 11.576124,
        approxDistanceKm = 46
    ),
    RoutePreset(
        title = "Havel-Schlösser & Seen",
        titleEn = "Havel Palaces & Lakes",
        originName = "Berlin",
        destinationName = "Potsdam",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Schloss, See, Park, Café, Museum",
        highlightText = "Grunewald, Großer Wannsee, Glienicker Brücke & Schloss Sanssouci",
        highlightTextEn = "Grunewald, Lake Wannsee, Glienicke Bridge & Sanssouci Palace",
        originLat = 52.520008,
        originLon = 13.404954,
        approxDistanceKm = 32
    ),
    RoutePreset(
        title = "Taunus-Burgen & Aussicht",
        titleEn = "Taunus Castles & Panorama",
        originName = "Frankfurt am Main",
        destinationName = "Bad Homburg vor der Höhe",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Burg, Schloss, Park, Aussicht, Historisch",
        highlightText = "Burg Kronberg, Landgrafenschloss & Römerkastell Saalburg",
        highlightTextEn = "Kronberg Castle, Landgrave Palace & Saalburg Roman Fort",
        originLat = 50.110924,
        originLon = 8.682127,
        approxDistanceKm = 22
    ),
    RoutePreset(
        title = "Rheinland Schlösser-Route",
        titleEn = "Rhineland Palaces Route",
        originName = "Köln",
        destinationName = "Königswinter",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Schloss, Burg, Rhein, Aussicht, Natur",
        highlightText = "Schloss Augustusburg Brühl, Bonn & Drachenfels im Siebengebirge",
        highlightTextEn = "Augustusburg Palace Brühl, Bonn & Drachenfels Siebengebirge",
        originLat = 50.937531,
        originLon = 6.960279,
        approxDistanceKm = 41
    ),
    RoutePreset(
        title = "Neckartal & Weinberge",
        titleEn = "Neckar Valley & Vineyards",
        originName = "Stuttgart",
        destinationName = "Tübingen",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Schloss, Wein, Altstadt, Aussicht, Fluss",
        highlightText = "Esslinger Burg, Schönbuch-Naturpark & Schloss Hohentübingen",
        highlightTextEn = "Esslingen Castle, Schönbuch Nature Park & Hohentübingen Castle",
        originLat = 48.775845,
        originLon = 9.182932,
        approxDistanceKm = 42
    ),
    RoutePreset(
        title = "Schlösser & Altes Land",
        titleEn = "Castles & Altes Land",
        originName = "Hamburg",
        destinationName = "Lüneburg",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Altstadt, Fluss, Schloss, Café, Historisch",
        highlightText = "Elbmarschen, Winsener Schloss & historische Salzstadt Lüneburg",
        highlightTextEn = "Elbe Marshes, Winsen Castle & Historic Salt Town Lüneburg",
        originLat = 53.551086,
        originLon = 9.993682,
        approxDistanceKm = 48
    ),
    RoutePreset(
        title = "Fränkische Schweiz Tor-Route",
        titleEn = "Franconian Switzerland Gateway",
        originName = "Nürnberg",
        destinationName = "Forchheim",
        defaultDetourKm = 10f,
        travelMode = TravelMode.CAR,
        suggestedKeywords = "Burg, Brauerei, Park, Altstadt, Aussicht",
        highlightText = "Schlossgarten Erlangen, Kaiserpfalz & Forchheimer Kellerwald",
        highlightTextEn = "Erlangen Palace Garden, Imperial Palace & Forchheim Cellar Forest",
        originLat = 49.452103,
        originLon = 11.076665,
        approxDistanceKm = 35
    )
)

val BIKE_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Isar- & Starnberger-See-Radtour",
        titleEn = "Isar & Lake Starnberg Cycle",
        originName = "München",
        destinationName = "Starnberg",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "See, Biergarten, Aussicht, Café, Natur",
        highlightText = "Forstenrieder Park, Schloss Berg & Starnberger Seepromenade",
        highlightTextEn = "Forstenried Park, Berg Castle & Lake Starnberg Promenade",
        originLat = 48.137154,
        originLon = 11.576124,
        approxDistanceKm = 26
    ),
    RoutePreset(
        title = "Havel & Wannsee Radtour",
        titleEn = "Havel & Wannsee Cycle Tour",
        originName = "Berlin",
        destinationName = "Potsdam",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Schloss, See, Park, Café, Natur",
        highlightText = "Grunewald, Wannsee, Glienicker Brücke & Schloss Sanssouci",
        highlightTextEn = "Grunewald, Wannsee, Glienicke Bridge & Sanssouci Palace",
        originLat = 52.520008,
        originLon = 13.404954,
        approxDistanceKm = 28
    ),
    RoutePreset(
        title = "Siebengebirge Rhein-Radweg",
        titleEn = "Siebengebirge Rhine Cycle Path",
        originName = "Bonn",
        destinationName = "Bad Honnef",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Burg, Rhein, Café, Historisch",
        highlightText = "Rheinaue Bonn, Königswinter & Insel Grafenwerth",
        highlightTextEn = "Bonn Rhine Park, Königswinter & Grafenwerth Island",
        originLat = 50.737430,
        originLon = 7.098207,
        approxDistanceKm = 16
    ),
    RoutePreset(
        title = "Elberadweg Klassiker",
        titleEn = "Elbe Cycle Path Classic",
        originName = "Meißen",
        destinationName = "Dresden",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Schloss, Wein, Café, Kultur",
        highlightText = "Albrechtsburg, Schloss Moritzburg & Dresdner Elbwiesen",
        highlightTextEn = "Albrechtsburg, Moritzburg Castle & Dresden Elbe Meadows",
        originLat = 51.1636,
        originLon = 13.4775,
        approxDistanceKm = 27
    ),
    RoutePreset(
        title = "Schloss-Radweg Ahrensburg",
        titleEn = "Ahrensburg Castle Cycle Track",
        originName = "Hamburg",
        destinationName = "Ahrensburg",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "Schloss, See, Café, Kostenlos",
        highlightText = "Alstertal-Radweg, Volksdorfer Wald & Wasserschloss Ahrensburg",
        highlightTextEn = "Alster Valley Cycle Path & Ahrensburg Moated Castle",
        originLat = 53.551086,
        originLon = 9.993682,
        approxDistanceKm = 24
    ),
    RoutePreset(
        title = "Bodensee-Uferradweg",
        titleEn = "Lake Constance Shore Trail",
        originName = "Konstanz",
        destinationName = "Überlingen",
        defaultDetourKm = 5f,
        travelMode = TravelMode.BIKE,
        suggestedKeywords = "See, Blumen, Aussicht, Café, Kloster",
        highlightText = "Blumeninsel Mainau, Pfahlbauten Unteruhldingen & Birnau",
        highlightTextEn = "Mainau Island, Stilt Dwellings & Birnau Pilgrimage Church",
        originLat = 47.660339,
        originLon = 9.175820,
        approxDistanceKm = 22
    )
)

val HIKE_ROUTE_PRESETS = listOf(
    RoutePreset(
        title = "Wetterstein & Klamm-Steig",
        titleEn = "Wetterstein & Gorge Trail",
        originName = "Murnau am Staffelsee",
        destinationName = "Garmisch-Partenkirchen",
        defaultDetourKm = 3f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Wanderung, Klamm, Moor, Kloster, Hütte",
        highlightText = "Murnauer Moos Bohlenweg, Kloster Ettal & Partnachklamm",
        highlightTextEn = "Murnau Moor Boardwalk, Ettal Abbey & Partnach Gorge",
        originLat = 47.6769,
        originLon = 11.2023,
        approxDistanceKm = 22
    ),
    RoutePreset(
        title = "Siebengebirge Rheinsteig",
        titleEn = "Siebengebirge Rhine Trail",
        originName = "Bonn",
        destinationName = "Bad Honnef",
        defaultDetourKm = 3f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Burg, Aussicht, Wanderung, Natur",
        highlightText = "Drachenfels, Petersberg & Löwenburg auf dem Rheinsteig",
        highlightTextEn = "Drachenfels, Petersberg & Löwenburg on the Rheinsteig",
        originLat = 50.737430,
        originLon = 7.098207,
        approxDistanceKm = 15
    ),
    RoutePreset(
        title = "Bodensee-Vulkanpfad",
        titleEn = "Lake Constance Volcanic Path",
        originName = "Singen",
        destinationName = "Konstanz",
        defaultDetourKm = 3f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Burg, Ruine, See, Aussicht, Wanderung",
        highlightText = "Festungsruine Hohentwiel, Untersee & Bodensee-Uferpfad",
        highlightTextEn = "Hohentwiel Fortress Ruins, Lower Lake & Shoreline Trail",
        originLat = 47.7630,
        originLon = 8.8400,
        approxDistanceKm = 28
    ),
    RoutePreset(
        title = "Philosophenweg & Königsstuhl",
        titleEn = "Philosophers' Walk & King's Seat",
        originName = "Heidelberg",
        destinationName = "Neckargemünd",
        defaultDetourKm = 3f,
        travelMode = TravelMode.HIKE,
        suggestedKeywords = "Aussicht, Schloss, Wald, Neckar, Natur",
        highlightText = "Philosophenweg, Schlossblick & Neckartal-Waldpfad",
        highlightTextEn = "Philosophers' Walk, Castle View & Neckar Forest Trail",
        originLat = 49.398750,
        originLon = 8.672434,
        approxDistanceKm = 11
    )
)

fun presetsForTravelMode(mode: TravelMode): List<RoutePreset> = when (mode) {
    TravelMode.CAR -> CAR_ROUTE_PRESETS
    TravelMode.BIKE -> BIKE_ROUTE_PRESETS
    TravelMode.HIKE -> HIKE_ROUTE_PRESETS
}

/**
 * Liefert empfohlene Tracks strikt im Umkreis von maximal 50 km rund um den aktuellen Standort des Nutzers!
 */
fun getNearbyRecommendedPresets(
    mode: TravelMode,
    originQuery: String,
    originPoint: GeoPoint?,
    lang: AppLanguage = AppLanguage.DE,
    nearbyCorridorItems: List<RouteDiscoveryItem> = emptyList()
): List<RoutePreset> {
    val cleanQuery = originQuery
        .replace("(Mein Standort)", "", ignoreCase = true)
        .replace("(My Location)", "", ignoreCase = true)
        .trim()

    val matchedSpot = REGIONAL_EXCURSION_SPOTS.firstOrNull {
        it.name.equals(cleanQuery, ignoreCase = true) || cleanQuery.contains(it.name, ignoreCase = true)
    }

    val refLat = originPoint?.lat ?: matchedSpot?.lat ?: when {
        cleanQuery.contains("berlin", ignoreCase = true) || cleanQuery.contains("potsdam", ignoreCase = true) -> 52.5200
        cleanQuery.contains("hamburg", ignoreCase = true) || cleanQuery.contains("lübeck", ignoreCase = true) -> 53.5510
        cleanQuery.contains("köln", ignoreCase = true) || cleanQuery.contains("bonn", ignoreCase = true) || cleanQuery.contains("düsseldorf", ignoreCase = true) -> 50.9375
        cleanQuery.contains("frankfurt", ignoreCase = true) || cleanQuery.contains("mainz", ignoreCase = true) || cleanQuery.contains("wiesbaden", ignoreCase = true) -> 50.1109
        cleanQuery.contains("stuttgart", ignoreCase = true) || cleanQuery.contains("tübingen", ignoreCase = true) || cleanQuery.contains("esslingen", ignoreCase = true) -> 48.7758
        cleanQuery.contains("nürnberg", ignoreCase = true) || cleanQuery.contains("erlangen", ignoreCase = true) || cleanQuery.contains("fürth", ignoreCase = true) -> 49.4521
        cleanQuery.contains("dresden", ignoreCase = true) || cleanQuery.contains("leipzig", ignoreCase = true) -> 51.0504
        cleanQuery.contains("augsburg", ignoreCase = true) -> 48.3705
        cleanQuery.contains("regensburg", ignoreCase = true) -> 49.0134
        cleanQuery.contains("wien", ignoreCase = true) -> 48.2082
        cleanQuery.contains("zürich", ignoreCase = true) -> 47.3769
        else -> 48.1371 // Default: München
    }
    val refLon = originPoint?.lon ?: matchedSpot?.lon ?: when {
        cleanQuery.contains("berlin", ignoreCase = true) || cleanQuery.contains("potsdam", ignoreCase = true) -> 13.4049
        cleanQuery.contains("hamburg", ignoreCase = true) || cleanQuery.contains("lübeck", ignoreCase = true) -> 9.9936
        cleanQuery.contains("köln", ignoreCase = true) || cleanQuery.contains("bonn", ignoreCase = true) || cleanQuery.contains("düsseldorf", ignoreCase = true) -> 6.9602
        cleanQuery.contains("frankfurt", ignoreCase = true) || cleanQuery.contains("mainz", ignoreCase = true) || cleanQuery.contains("wiesbaden", ignoreCase = true) -> 8.6821
        cleanQuery.contains("stuttgart", ignoreCase = true) || cleanQuery.contains("tübingen", ignoreCase = true) || cleanQuery.contains("esslingen", ignoreCase = true) -> 9.1829
        cleanQuery.contains("nürnberg", ignoreCase = true) || cleanQuery.contains("erlangen", ignoreCase = true) || cleanQuery.contains("fürth", ignoreCase = true) -> 11.0766
        cleanQuery.contains("dresden", ignoreCase = true) || cleanQuery.contains("leipzig", ignoreCase = true) -> 13.7372
        cleanQuery.contains("augsburg", ignoreCase = true) -> 10.8978
        cleanQuery.contains("regensburg", ignoreCase = true) -> 12.1016
        cleanQuery.contains("wien", ignoreCase = true) -> 16.3738
        cleanQuery.contains("zürich", ignoreCase = true) -> 8.5417
        else -> 11.5761 // Default: München
    }

    val startLabel = when {
        cleanQuery.isNotBlank() &&
                !cleanQuery.equals("Aktueller Standort", ignoreCase = true) &&
                !cleanQuery.equals("Mein Standort", ignoreCase = true) &&
                !cleanQuery.equals("Current Location", ignoreCase = true) -> cleanQuery
        originPoint != null && originPoint.name.isNotBlank() &&
                !originPoint.name.equals("Aktueller Standort", ignoreCase = true) -> originPoint.name
        else -> "München"
    }

    val userPt = GeoPoint(refLat, refLon, startLabel)
    val defaultDetour = when (mode) {
        TravelMode.CAR -> 10f
        TravelMode.BIKE -> 5f
        TravelMode.HIKE -> 3f
    }
    val idealDistKm = when (mode) {
        TravelMode.CAR -> 32.0
        TravelMode.BIKE -> 22.0
        TravelMode.HIKE -> 12.0
    }

    // 1. Find regional excursion spots strictly within 50 km radius of the user's location (3 km .. 50 km)
    val within50KmSpots = REGIONAL_EXCURSION_SPOTS
        .map { spot ->
            val dKm = GeoMath.haversineKm(userPt, GeoPoint(spot.lat, spot.lon))
            spot to dKm
        }
        .filter { (spot, dKm) ->
            dKm in 3.0..50.0 && !spot.name.equals(startLabel, ignoreCase = true)
        }
        .sortedBy { (_, dKm) -> kotlin.math.abs(dKm - idealDistKm) }

    val dynamicPresets = mutableListOf<RoutePreset>()
    for ((spot, dKm) in within50KmSpots.take(5)) {
        val approxKm = (dKm * 1.15).roundToInt().coerceIn(4, 50)
        dynamicPresets.add(
            RoutePreset(
                title = spot.titleDe,
                titleEn = spot.titleEn,
                originName = startLabel,
                destinationName = spot.name,
                defaultDetourKm = defaultDetour,
                travelMode = mode,
                suggestedKeywords = spot.keywords,
                highlightText = spot.highlightDe,
                highlightTextEn = spot.highlightEn,
                originLat = userPt.lat,
                originLon = userPt.lon,
                approxDistanceKm = approxKm
            )
        )
    }

    // 2. Also include live discovered POIs within 50 km radius if user is in another region/country
    if (dynamicPresets.size < 4 && nearbyCorridorItems.isNotEmpty()) {
        val liveSpots = nearbyCorridorItems
            .map { item -> item to GeoMath.haversineKm(userPt, item.point) }
            .filter { (item, dKm) ->
                dKm in 4.0..48.0 && !item.title.equals(startLabel, ignoreCase = true)
            }
            .distinctBy { it.first.title.lowercase() }
            .sortedBy { (_, dKm) -> kotlin.math.abs(dKm - idealDistKm) }
            .take(4)

        for ((item, dKm) in liveSpots) {
            if (dynamicPresets.size >= 5) break
            val approxKm = (dKm * 1.15).roundToInt().coerceIn(4, 50)
            dynamicPresets.add(
                RoutePreset(
                    title = "${item.category.localizedLabel(AppLanguage.DE)}-Track",
                    titleEn = "${item.category.localizedLabel(AppLanguage.EN)} Track",
                    originName = startLabel,
                    destinationName = item.title,
                    defaultDetourKm = defaultDetour,
                    travelMode = mode,
                    suggestedKeywords = item.category.defaultKeywords.take(3).joinToString(", "),
                    highlightText = item.subtitle,
                    highlightTextEn = item.subtitle,
                    originLat = userPt.lat,
                    originLon = userPt.lon,
                    approxDistanceKm = approxKm
                )
            )
        }
    }

    // 3. Append closest curated presets (all <= 50 km) so there are always 5 rich options
    val curatedSorted = presetsForTravelMode(mode).sortedBy { preset ->
        GeoMath.haversineKm(userPt, GeoPoint(preset.originLat, preset.originLon))
    }
    for (preset in curatedSorted) {
        if (dynamicPresets.size >= 6) break
        if (dynamicPresets.none { it.destinationName.equals(preset.destinationName, ignoreCase = true) }) {
            dynamicPresets.add(preset.copy(approxDistanceKm = preset.approxDistanceKm.coerceAtMost(50)))
        }
    }

    return dynamicPresets
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
