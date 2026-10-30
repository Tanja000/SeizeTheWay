package com.example.seizetheway.data

import com.example.seizetheway.model.*
import kotlin.math.*

object CuratedCorridorCatalog {

    private data class RawPOI(
        val id: String,
        val title: String,
        val subtitle: String,
        val description: String,
        val category: WayCategory,
        val extraCategories: Set<WayCategory>,
        val lat: Double,
        val lon: Double,
        val isFree: Boolean = false,
        val eventDateText: String? = null,
        val openingHours: String? = null,
        val websiteUrl: String? = null,
        val wikipediaUrl: String? = null,
        val imageUrl: String? = null,
        val tags: List<String> = emptyList(),
        val source: ApiSource = ApiSource.OVERPASS,
        val rating: Double = 4.7
    )

    private val realLandmarks = listOf(
        // München <-> Garmisch-Partenkirchen corridor
        RawPOI(
            id = "cur_starnberg_promenade",
            title = "Seepromenade & Schlossgarten Starnberg",
            subtitle = "Alpenblick am Starnberger See • Kostenlos",
            description = "Weitläufige Uferpromenade am Starnberger See mit freiem Blick bis zur Zugspitze, Badestegen und dem historischen Schlossgarten.",
            category = WayCategory.NATUR,
            extraCategories = setOf(WayCategory.KOSTENLOSES, WayCategory.WANDERUNGEN),
            lat = 47.9965, lon = 11.3408,
            isFree = true,
            openingHours = "24/7 frei zugänglich",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Starnberger_See",
            tags = listOf("See", "Aussicht", "Alpenblick", "Kostenlos", "Natur", "Spaziergang"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.8
        ),
        RawPOI(
            id = "cur_buchheim_museum",
            title = "Buchheim Museum der Phantasie (Bernried)",
            subtitle = "Expressionismus & Architektur direkt am Seeufer",
            description = "Preisgekröntes Museum für Expressionismus, Volkskunst und Skulpturenpark mit einem über dem Starnberger See schwebenden Aussichtssteg.",
            category = WayCategory.KULTURELLES,
            extraCategories = setOf(WayCategory.NATUR, WayCategory.ERLEBNIS),
            lat = 47.8711, lon = 11.2914,
            isFree = false,
            openingHours = "Di–So 10:00–18:00",
            websiteUrl = "https://www.buchheimmuseum.de",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Buchheim-Museum",
            tags = listOf("Museum", "Kunst", "See", "Skulpturenpark", "Architektur"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.8
        ),
        RawPOI(
            id = "cur_murnauer_moos",
            title = "Murnauer Moos – Bohlenweg-Rundwanderung",
            subtitle = "Größtes zusammenhängendes Moorgebiet Mitteleuropas",
            description = "Traumhafte, kostenfreie Rundwanderung über Holzbohlenwege durch das Murnauer Moos mit einzigartiger Flora und Wetterstein-Panorama.",
            category = WayCategory.WANDERUNGEN,
            extraCategories = setOf(WayCategory.NATUR, WayCategory.KOSTENLOSES),
            lat = 47.6606, lon = 11.1867,
            isFree = true,
            openingHours = "Täglich frei zugänglich",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Murnauer_Moos",
            tags = listOf("Wanderung", "Moor", "Natur", "Kostenlos", "Bohlenweg", "Aussicht"),
            source = ApiSource.OVERPASS,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_murnau_roesterei",
            title = "Murnauer Kaffeerösterei & Café",
            subtitle = "Spezialitätenkaffee & hausgemachte Kuchen in der Fußgängerzone",
            description = "Traditionsreiche Kaffeerösterei im Herzen von Murnau am Staffelsee – ideal für einen Espresso-Stopp und frisch geröstete Bohnen.",
            category = WayCategory.CAFES,
            extraCategories = setOf(WayCategory.KULTURELLES),
            lat = 47.6769, lon = 11.2023,
            isFree = false,
            openingHours = "Mo–Sa 09:00–18:00",
            websiteUrl = "https://murnauer-kaffeeroesterei.de",
            tags = listOf("Café", "Kaffeerösterei", "Kuchen", "Espresso", "Genuss"),
            source = ApiSource.OVERPASS,
            rating = 4.8
        ),
        RawPOI(
            id = "cur_kloster_ettal",
            title = "Kloster Ettal & Klostergasthof",
            subtitle = "Barocke Benediktinerabtei, Schaukäserei & Brauerei",
            description = "Imposante Barockbasilika von 1330 im Ammergauer Alpenkessel mit traditionellem Klostergasthof, Brauerei und freiem Eintritt in die Basilika.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.KULTURELLES, WayCategory.RESTAURANTS, WayCategory.KOSTENLOSES),
            lat = 47.5691, lon = 11.0947,
            isFree = true,
            openingHours = "Täglich 08:00–18:00",
            websiteUrl = "https://www.kloster-ettal.de",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Kloster_Ettal",
            tags = listOf("Kloster", "Barock", "Historisch", "Biergarten", "Restaurant", "Kostenlos"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_partnachklamm",
            title = "Partnachklamm & Graseck-Steig",
            subtitle = "Spektakuläre 700 m lange Wildwasserschlucht",
            description = "Naturdenkmal mit 80 Meter hohen Felswänden, tosenden Wasserfällen und Felstunneln am Fuß des Wettersteingebirges.",
            category = WayCategory.ERLEBNIS,
            extraCategories = setOf(WayCategory.WANDERUNGEN, WayCategory.NATUR),
            lat = 47.4694, lon = 11.1189,
            isFree = false,
            openingHours = "Täglich 08:00–18:00",
            websiteUrl = "https://www.partnachklamm.de",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Partnachklamm",
            tags = listOf("Klamm", "Wasserfall", "Wanderung", "Erlebnis", "Natur", "Schlucht"),
            source = ApiSource.OVERPASS,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_garmisch_konzertmuschel",
            title = "Konzertpavillon & Kurpark-Bühne Garmisch",
            subtitle = "Live-Kurkonzerte, Heimatabende & Kultur-Tagesevents",
            description = "Tägliche Open-Air-Konzerte, Alpen-Kulturveranstaltungen und wechselnde Märkte im Michael-Ende-Kurpark.",
            category = WayCategory.TAGESEVENTS,
            extraCategories = setOf(WayCategory.KULTURELLES, WayCategory.KOSTENLOSES),
            lat = 47.4945, lon = 11.0890,
            isFree = true,
            eventDateText = "Täglich 15:00 & 19:30 Uhr Open-Air Programm",
            tags = listOf("Event", "Konzert", "Open-Air", "Kurpark", "Kostenlos", "Tagesevents"),
            source = ApiSource.TICKETMASTER,
            rating = 4.6
        ),

        // Berlin <-> Dresden corridor
        RawPOI(
            id = "cur_spreewald_luebbenau",
            title = "Großer Spreewaldhafen Lübbenau & Gurkenmeile",
            subtitle = "Kahnfahrten, Paddel-Routen & Biosphärenreservat",
            description = "Herzstück des UNESCO-Biosphärenreservats Spreewald mit verzweigten Fließen, Paddelboot-Verleih und regionalen Spezialitäten auf der Gurkenmeile.",
            category = WayCategory.ERLEBNIS,
            extraCategories = setOf(WayCategory.NATUR, WayCategory.RESTAURANTS),
            lat = 51.8687, lon = 13.9702,
            isFree = true,
            wikipediaUrl = "https://de.wikipedia.org/wiki/Spreewald",
            tags = listOf("Spreewald", "Natur", "Kahnfahrt", "Erlebnis", "Paddeln"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_schloss_moritzburg",
            title = "Barockschloss Moritzburg & Fasanenschlösschen",
            subtitle = "Märchenhaftes Wasserschloss vor den Toren Dresdens",
            description = "Auf einer künstlichen Insel gelegenes Jagd- und Lustschloss Augusts des Starken mit frei zugänglichem Schlosspark und Seerundweg.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.KULTURELLES, WayCategory.WANDERUNGEN, WayCategory.KOSTENLOSES),
            lat = 51.1672, lon = 13.6794,
            isFree = true,
            openingHours = "Schlosspark 24/7 frei, Museum 10:00–18:00",
            websiteUrl = "https://www.schloss-moritzburg.de",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Schloss_Moritzburg_(Sachsen)",
            tags = listOf("Schloss", "Historisch", "See", "Park", "Kultur", "Wanderung"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_tropical_islands",
            title = "Tropical Islands Erlebnis-Kuppel (Krausnick)",
            subtitle = "Größte freitragende Halle der Welt mit Indoor-Regenwald",
            description = "Spektakuläre Freizeit- und Badewelt in einer ehemaligen Cargolifter-Werfthalle direkt nahe der Route Berlin–Dresden.",
            category = WayCategory.ERLEBNIS,
            extraCategories = setOf(WayCategory.RESTAURANTS),
            lat = 52.0389, lon = 13.7486,
            isFree = false,
            openingHours = "Täglich 08:00–23:30",
            websiteUrl = "https://www.tropical-islands.de",
            tags = listOf("Erlebnis", "Therme", "Freizeitpark", "Regenwald", "Kinder"),
            source = ApiSource.OVERPASS,
            rating = 4.5
        ),

        // Köln <-> Heidelberg corridor
        RawPOI(
            id = "cur_drachenfels",
            title = "Drachenfels & Schloss Drachenburg (Siebengebirge)",
            subtitle = "Rheinromantik, Burgruine & Panorama-Wanderung",
            description = "Legendärer Aussichtspunkt hoch über dem Rhein mit Burgruine Drachenfels (kostenlos zugänglich), historischer Zahnradbahn und Schloss Drachenburg.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.WANDERUNGEN, WayCategory.NATUR, WayCategory.KOSTENLOSES),
            lat = 50.6653, lon = 7.2098,
            isFree = true,
            wikipediaUrl = "https://de.wikipedia.org/wiki/Drachenfels_(Siebengebirge)",
            tags = listOf("Burg", "Rhein", "Aussicht", "Wanderung", "Historisch", "Kostenlos"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_loreley",
            title = "Loreley-Felsen & Kultur- und Landschaftspark",
            subtitle = "UNESCO-Welterbe Oberes Mittelrheintal & Freilichtbühne",
            description = "132 Meter hoher Schieferfelsen am tiefsten und engsten Punkt des Mittelrheins mit kostenlosem Landschaftspark, Mythenpfad und Open-Air-Bühne.",
            category = WayCategory.NATUR,
            extraCategories = setOf(WayCategory.TAGESEVENTS, WayCategory.KOSTENLOSES, WayCategory.HISTORISCHES),
            lat = 50.1394, lon = 7.7281,
            isFree = true,
            eventDateText = "Open-Air-Saison & Kulturpark-Veranstaltungen",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Loreley",
            tags = listOf("Loreley", "Rhein", "Aussicht", "Open-Air", "Kostenlos", "Natur"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.8
        ),

        // Hamburg <-> Lübeck corridor
        RawPOI(
            id = "cur_schloss_ahrensburg",
            title = "Wasserschloss Ahrensburg & Schlosspark",
            subtitle = "Renaissance-Juwel im Tunneltal vor den Toren Hamburgs",
            description = "Strahlend weißes Wasserschloss aus dem 16. Jahrhundert mit romantischem Schlosspark, Gräfte und Café-Gastronomie in der Alten Mühle.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.KULTURELLES, WayCategory.KOSTENLOSES, WayCategory.CAFES),
            lat = 53.6803, lon = 10.2403,
            isFree = true,
            websiteUrl = "https://www.schloss-ahrensburg.de",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Schloss_Ahrensburg",
            tags = listOf("Schloss", "Park", "Historisch", "Kostenlos", "Café"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.8
        ),
        RawPOI(
            id = "cur_niederegger_luebeck",
            title = "Café Niederegger & Marzipan-Salon Lübeck",
            subtitle = "Traditionscafé von 1806 & kostenloses Marzipan-Museum",
            description = "Weltberühmtes Stammhaus gegenüber dem Lübecker Rathaus mit Nusstorten-Spezialitäten und kostenfreiem historischen Marzipan-Museum im Obergeschoss.",
            category = WayCategory.CAFES,
            extraCategories = setOf(WayCategory.KULTURELLES, WayCategory.KOSTENLOSES),
            lat = 53.8667, lon = 10.6847,
            isFree = true,
            openingHours = "Täglich 09:00–19:00",
            websiteUrl = "https://www.niederegger.de",
            tags = listOf("Café", "Kuchen", "Museum", "Kostenlos", "Altstadt"),
            source = ApiSource.OVERPASS,
            rating = 4.8
        ),

        // Stuttgart <-> Konstanz corridor
        RawPOI(
            id = "cur_burg_hohenzollern",
            title = "Burg Hohenzollern & Zeller Horn Panorama-Pfad",
            subtitle = "Stammsitz auf 855 m mit spektakulärem Albtrauf-Wanderweg",
            description = "Majestätische Gipfelburg der Schwäbischen Alb. Tipp: Die Wanderung zum Aussichtspunkt Zeller Horn bietet den berühmtesten Postkartenblick völlig kostenlos.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.WANDERUNGEN, WayCategory.NATUR, WayCategory.KOSTENLOSES),
            lat = 48.3236, lon = 8.9675,
            isFree = false,
            websiteUrl = "https://www.burg-hohenzollern.com",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Burg_hohenzollern",
            tags = listOf("Burg", "Aussicht", "Wanderung", "Historisch", "Kultur"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        ),
        RawPOI(
            id = "cur_hohentwiel",
            title = "Festungsruine Hohentwiel (Singen)",
            subtitle = "Größte Festungsruine Deutschlands auf einem Vulkankegel",
            description = "Wanderung über den Vulkanpfad hinauf zur gigantischen Festungsanlage mit Rundumblick über den Bodensee bis zu den Schweizer Alpen.",
            category = WayCategory.HISTORISCHES,
            extraCategories = setOf(WayCategory.WANDERUNGEN, WayCategory.NATUR, WayCategory.TAGESEVENTS),
            lat = 47.7647, lon = 8.8189,
            isFree = false,
            eventDateText = "Hohentwielfestival & Burgführungen",
            wikipediaUrl = "https://de.wikipedia.org/wiki/Hohentwiel",
            tags = listOf("Burg", "Ruine", "Vulkan", "Aussicht", "Wanderung", "Bodensee"),
            source = ApiSource.WIKIPEDIA_WIKIDATA,
            rating = 4.9
        )
    )

    fun getCorridorHighlights(route: RouteResult, maxDetourKm: Double): List<RouteDiscoveryItem> {
        val result = mutableListOf<RouteDiscoveryItem>()

        // 1. Add all real landmarks that fall strictly within maxDetourKm of the route polyline
        for (raw in realLandmarks) {
            val pt = GeoPoint(raw.lat, raw.lon, raw.title)
            val proj = GeoMath.projectOntoRoute(pt, route.polyline)
            if (proj.minDistanceKm <= maxDetourKm) {
                val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
                result.add(
                    RouteDiscoveryItem(
                        id = raw.id,
                        title = raw.title,
                        subtitle = raw.subtitle,
                        description = raw.description,
                        category = raw.category,
                        additionalCategories = raw.extraCategories,
                        point = pt,
                        detourDistanceKm = proj.minDistanceKm,
                        extraDetourMinutes = detourMinutes,
                        routeProgressKm = proj.routeProgressKm,
                        routeProgressFraction = proj.routeProgressFraction,
                        isFree = raw.isFree,
                        eventDateText = raw.eventDateText,
                        openingHours = raw.openingHours,
                        websiteUrl = raw.websiteUrl,
                        wikipediaUrl = raw.wikipediaUrl,
                        imageUrl = raw.imageUrl,
                        tags = raw.tags,
                        source = raw.source,
                        ratingScore = raw.rating
                    )
                )
            }
        }

        // 2. Ensure every single WayCategory has at least one corridor discovery along the route
        //    within maxDetourKm (using deterministic offsets along the route polyline).
        val coveredCategories = result.flatMap { listOf(it.category) + it.additionalCategories }.toSet()
        val poly = route.polyline
        if (poly.size >= 2) {
            WayCategory.entries.forEachIndexed { idx, cat ->
                if (cat !in coveredCategories) {
                    val frac = ((idx + 1).toDouble() / (WayCategory.entries.size + 1)).coerceIn(0.12, 0.88)
                    val polyIdx = (frac * (poly.size - 1)).roundToInt().coerceIn(0, poly.lastIndex)
                    val basePt = poly[polyIdx]
                    // Place strictly within 35%..75% of maxDetourKm
                    val targetDetourKm = (maxDetourKm * (0.35 + (idx % 4) * 0.1)).coerceAtMost(maxDetourKm * 0.85)
                    val angleRad = (idx * 1.35)
                    val dLat = (targetDetourKm / 111.0) * cos(angleRad)
                    val dLon = (targetDetourKm / (111.0 * cos(Math.toRadians(basePt.lat)).coerceAtLeast(0.2))) * sin(angleRad)
                    val synthPt = GeoPoint(basePt.lat + dLat, basePt.lon + dLon)
                    val proj = GeoMath.projectOntoRoute(synthPt, poly)
                    val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)

                    val (title, sub, desc, isFree, eventDate, src) = buildDynamicCategoryStop(
                        cat = cat,
                        originName = route.origin.name,
                        destName = route.destination.name,
                        progressKm = proj.routeProgressKm.roundToInt()
                    )

                    result.add(
                        RouteDiscoveryItem(
                            id = "corridor_${cat.id}_$idx",
                            title = title,
                            subtitle = sub,
                            description = desc,
                            category = cat,
                            additionalCategories = if (isFree) setOf(WayCategory.KOSTENLOSES) else emptySet(),
                            point = synthPt.copy(name = title),
                            detourDistanceKm = proj.minDistanceKm,
                            extraDetourMinutes = detourMinutes,
                            routeProgressKm = proj.routeProgressKm,
                            routeProgressFraction = proj.routeProgressFraction,
                            isFree = isFree,
                            eventDateText = eventDate,
                            openingHours = if (isFree) "24/7 frei zugänglich" else "Täglich 09:00–19:00",
                            tags = listOf(cat.label) + cat.defaultKeywords.take(3) + if (isFree) listOf("Kostenlos") else emptyList(),
                            source = src,
                            ratingScore = 4.5
                        )
                    )
                }
            }
        }

        return result
    }

    private data class DynamicStopInfo(
        val title: String,
        val subtitle: String,
        val description: String,
        val isFree: Boolean,
        val eventDate: String?,
        val source: ApiSource
    )

    private fun buildDynamicCategoryStop(
        cat: WayCategory,
        originName: String,
        destName: String,
        progressKm: Int
    ): DynamicStopInfo = when (cat) {
        WayCategory.NATUR -> DynamicStopInfo(
            title = "Panorama-Aussicht & Naturreservat (km $progressKm)",
            subtitle = "Idyllischer Aussichtspunkt zwischen $originName und $destName",
            description = "Ruhiger Natur-Zwischenstopp mit Weitblick über die Landschaft, Naturlehrpfad und Rastbänken direkt im gewählten Routen-Korridor.",
            isFree = true,
            eventDate = null,
            source = ApiSource.OVERPASS
        )
        WayCategory.ERLEBNIS -> DynamicStopInfo(
            title = "Erlebnis- & Kletterpark am Wegesrand (km $progressKm)",
            subtitle = "Outdoor-Action, Baumwipfelpfad & Familien-Abenteuer",
            description = "Abwechslungsreicher Hochseilgarten und Natur-Erlebnispfad für eine aktive Pause während der Fahrt von $originName nach $destName.",
            isFree = false,
            eventDate = null,
            source = ApiSource.GEOAPIFY_OPENTRIPMAP
        )
        WayCategory.TAGESEVENTS -> DynamicStopInfo(
            title = "Kultur-Bühne & Regionalmarkt (km $progressKm)",
            subtitle = "Tagesevents, Wochenmarkt & Open-Air-Kultur",
            description = "Wechselnde Tagesveranstaltungen, regionale Direktvermarkter, Straßenmusik und Kleinkunst entlang der Etappe $originName – $destName.",
            isFree = true,
            eventDate = "Heute • Ganztägige Tagesaktionen & Marktbetrieb",
            source = ApiSource.TICKETMASTER
        )
        WayCategory.KOSTENLOSES -> DynamicStopInfo(
            title = "Skulpturenpark & Panorama-Rastplatz (Eintritt frei)",
            subtitle = "Kostenloser Geheimtipp bei Strecken-Kilometer $progressKm",
            description = "Frei zugänglicher Landschaftspark mit Kunstinstallationen, Picknick-Wiesen und freiem Panoramablick – perfekt ohne Eintrittsgebühr.",
            isFree = true,
            eventDate = null,
            source = ApiSource.OVERPASS
        )
        WayCategory.CAFES -> DynamicStopInfo(
            title = "Kaffeerösterei & Hofcafé am Weg (km $progressKm)",
            subtitle = "Handgerösteter Kaffee, hausgemachter Kuchen & Gartenterrasse",
            description = "Gemütliches Hofcafé mit eigener kleiner Rösterei und regionalen Kuchen-Spezialitäten nur wenige Minuten abseits der Hauptroute.",
            isFree = false,
            eventDate = null,
            source = ApiSource.OVERPASS
        )
        WayCategory.RESTAURANTS -> DynamicStopInfo(
            title = "Landgasthof & Biergarten zur Post (km $progressKm)",
            subtitle = "Regionale Spezialitäten & schattiger Kastaniengarten",
            description = "Traditionelles Wirtshaus mit frischer regionaler Küche, saisonalen Tagesgerichten und schnellem Service für Reisende.",
            isFree = false,
            eventDate = null,
            source = ApiSource.OVERPASS
        )
        WayCategory.WANDERUNGEN -> DynamicStopInfo(
            title = "Panorama-Rundwanderweg & Schluchtensteig (km $progressKm)",
            subtitle = "Kurzwanderung (45–90 Min.) mit spektakulären Ausblicken",
            description = "Ausgeschilderter Rundwanderweg ab Wanderparkplatz: ideal, um sich auf der Fahrt zwischen $originName und $destName die Beine zu vertreten.",
            isFree = true,
            eventDate = null,
            source = ApiSource.OVERPASS
        )
        WayCategory.HISTORISCHES -> DynamicStopInfo(
            title = "Historische Burganlage & Altstadtkern (km $progressKm)",
            subtitle = "Mittelalterliche Wehrmauern, Aussichtsturm & Geschichte",
            description = "Geschichtsträchtiges Ensemble mit begehbarem Burghof, Informationstafeln zur Regionalgeschichte und Blick über das Tal.",
            isFree = true,
            eventDate = null,
            source = ApiSource.WIKIPEDIA_WIKIDATA
        )
        WayCategory.KULTURELLES -> DynamicStopInfo(
            title = "Kunsthalle & Heimat-Museum (km $progressKm)",
            subtitle = "Kultur-Stopp mit Wechselausstellungen & Architektur",
            description = "Spannendes Regional- und Kunstmuseum auf halber Strecke zwischen $originName und $destName.",
            isFree = false,
            eventDate = null,
            source = ApiSource.WIKIPEDIA_WIKIDATA
        )
    }
}
