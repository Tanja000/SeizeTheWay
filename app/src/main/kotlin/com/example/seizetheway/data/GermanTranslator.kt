package com.example.seizetheway.data

import com.example.seizetheway.model.AppLanguage
import com.example.seizetheway.model.RouteDiscoveryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Bidirektionale Übersetzungs-Engine (DE <-> EN & Auto -> DE/EN):
 * - Übersetzt alle Ergebnisse aus Wikipedia, Wikidata, OpenStreetMap (Overpass/Photon),
 *   Ticketmaster, Eventbrite und dem kuratierten Katalog sowohl ins Deutsche als auch ins Englische.
 * - Nutzt mehrstufige Übersetzungs-APIs (Google Neural Translate GTX + MyMemory Translation API)
 *   kombiniert mit einem umfangreichen Offline-Satz- & Fachvokabular-Übersetzer.
 */
object GermanTranslator {

    private val deCache = ConcurrentHashMap<String, String>()
    private val enCache = ConcurrentHashMap<String, String>()

    // Eindeutig deutsche Funktionswörter (ohne mehrdeutige Wörter wie "in" oder "an")
    private val strictGermanWords = setOf(
        "der", "die", "das", "ein", "eine", "einer", "einem", "einen",
        "und", "im", "am", "auf", "aus", "mit", "für", "von", "vom", "zum", "zur",
        "bei", "nach", "über", "unter", "zwischen", "durch", "nicht", "oder",
        "ist", "sind", "war", "waren", "wird", "werden", "wurde", "wurden",
        "sowie", "auch", "sehr", "schön", "groß", "stadt", "gemeinde", "landkreis",
        "entlang", "deiner", "direkt", "kostenlos", "zugänglich", "sehenswerter"
    )

    // Eindeutig englische Funktionswörter
    private val strictEnglishWords = setOf(
        "the", "and", "of", "to", "from", "with", "for", "by", "at", "on",
        "is", "are", "was", "were", "located", "situated", "built", "founded",
        "century", "municipality", "district", "bavaria", "germany", "austria",
        "mountain", "lake", "castle", "palace", "church", "monastery", "abbey",
        "viewpoint", "waterfall", "hiking", "trail", "free", "admission", "open"
    )

    private val enToDePhrases = linkedMapOf(
        "scenic viewpoint" to "Panorama-Aussichtspunkt",
        "panoramic viewpoint" to "Panorama-Aussichtspunkt",
        "viewpoint" to "Aussichtspunkt",
        "waterfalls" to "Wasserfälle",
        "waterfall" to "Wasserfall",
        "castle ruins" to "Burgruine",
        "castle" to "Burg / Schloss",
        "ruins" to "Ruine",
        "fortress" to "Festung",
        "cathedral" to "Kathedrale / Dom",
        "church" to "Kirche",
        "monastery" to "Kloster",
        "abbey" to "Abtei",
        "chapel" to "Kapelle",
        "palace" to "Schloss / Residenz",
        "nature reserve" to "Naturschutzgebiet",
        "national park" to "Nationalpark",
        "hiking trail" to "Wanderweg",
        "mountain peak" to "Berggipfel",
        "mountain hut" to "Berghütte",
        "alpine hut" to "Alpenvereinshütte / Alm",
        "swimming lake" to "Badesee",
        "lake" to "See",
        "gorge" to "Klamm / Schlucht",
        "canyon" to "Schlucht",
        "suspension bridge" to "Hängebrücke",
        "bridge" to "Brücke",
        "observation tower" to "Aussichtsturm",
        "tower" to "Turm",
        "lighthouse" to "Leuchtturm",
        "historic center" to "Historische Altstadt",
        "old town" to "Altstadt",
        "market square" to "Marktplatz",
        "botanical garden" to "Botanischer Garten",
        "amusement park" to "Freizeitpark",
        "theme park" to "Erlebnispark",
        "water park" to "Erlebnisbad / Therme",
        "high ropes course" to "Kletterwald / Hochseilgarten",
        "summer toboggan" to "Sommerrodelbahn",
        "zoo" to "Tierpark / Zoo",
        "brewery" to "Brauerei",
        "beer garden" to "Biergarten",
        "winery" to "Weingut",
        "vineyard" to "Weinberg",
        "coffee roastery" to "Kaffeerösterei",
        "ice cream parlor" to "Eisdiele",
        "exhibition" to "Ausstellung",
        "concert" to "Konzert",
        "festival" to "Festival / Festspiele",
        "open air" to "Open-Air",
        "free admission" to "Eintritt frei",
        "free entry" to "Eintritt frei",
        "is a municipality in" to "ist eine Gemeinde in",
        "is a town in" to "ist eine Stadt in",
        "is a village in" to "ist ein Ort in",
        "is a castle in" to "ist eine Burg bzw. ein Schloss in",
        "is a mountain in" to "ist ein Berg in",
        "is a lake in" to "ist ein See in",
        "in the district of" to "im Landkreis",
        "in upper bavaria" to "in Oberbayern",
        "in bavaria" to "in Bayern",
        "in germany" to "in Deutschland"
    )

    private val deToEnPhrases = linkedMapOf(
        // Vollständige kuratierte Highlight-Texte & häufige OSM/Wikipedia-Sätze
        "Seepromenade & Schlossgarten Starnberg" to "Lakeside Promenade & Starnberg Castle Gardens",
        "Alpenblick am Starnberger See • Kostenlos" to "Alpine view at Lake Starnberg • Free",
        "Weitläufige Uferpromenade am Starnberger See mit freiem Blick bis zur Zugspitze, Badestegen und dem historischen Schlossgarten." to "Spacious lakeside promenade at Lake Starnberg with clear views of the Zugspitze, bathing docks and historic castle gardens.",
        "Buchheim Museum der Phantasie (Bernried)" to "Buchheim Museum of Imagination (Bernried)",
        "Expressionismus & Architektur direkt am Seeufer" to "Expressionism & architecture directly on the lakeshore",
        "Preisgekröntes Museum für Expressionismus, Volkskunst und Skulpturenpark mit einem über dem Starnberger See schwebenden Aussichtssteg." to "Award-winning museum for expressionism, folk art and sculpture park with a scenic walkway suspended over Lake Starnberg.",
        "Murnauer Moos – Bohlenweg-Rundwanderung" to "Murnau Moor – Boardwalk Loop Hike",
        "Größtes zusammenhängendes Moorgebiet Mitteleuropas" to "Central Europe's largest contiguous moorland area",
        "Traumhafte, kostenfreie Rundwanderung über Holzbohlenwege durch das Murnauer Moos mit einzigartiger Flora und Wetterstein-Panorama." to "Scenic, free loop hike across wooden boardwalks through Murnau Moor with unique flora and Wetterstein alpine panorama.",
        "Murnauer Kaffeerösterei & Café" to "Murnau Coffee Roastery & Café",
        "Spezialitätenkaffee & hausgemachte Kuchen in der Fußgängerzone" to "Specialty coffee & homemade cakes in the pedestrian zone",
        "Traditionsreiche Kaffeerösterei im Herzen von Murnau am Staffelsee – ideal für einen Espresso-Stopp und frisch geröstete Bohnen." to "Traditional specialty coffee roastery in the heart of Murnau am Staffelsee – ideal for an espresso stop and freshly roasted beans.",
        "Kloster Ettal & Klostergasthof" to "Ettal Abbey & Monastery Inn",
        "Barocke Benediktinerabtei, Schaukäserei & Brauerei" to "Baroque Benedictine abbey, showcase dairy & brewery",
        "Imposante Barockbasilika von 1330 im Ammergauer Alpenkessel mit traditionellem Klostergasthof, Brauerei und freiem Eintritt in die Basilika." to "Impressive baroque basilica from 1330 in the Ammergau Alps basin with traditional monastery inn, brewery and free basilica admission.",
        "Konzertpavillon & Kurpark-Bühne Garmisch" to "Concert Pavilion & Spa Park Stage Garmisch",
        "Kostenlose Kurkonzerte & Blumenpromenade" to "Free spa concerts & flower promenade",
        "Historischer Konzertpavillon im gepflegten Michael-Ende-Kurpark mit regelmäßigen Live-Freiluftkonzerten und Heilkräutergarten." to "Historic concert pavilion in the landscaped Michael Ende Spa Park featuring regular open-air live concerts and a medicinal herb garden.",
        "Partnachklamm & Graseck-Steig" to "Partnach Gorge & Graseck Trail",
        "Naturdenkmal mit tosenden Wasserfällen & Felsengalerien" to "Natural monument with roaring waterfalls & rock galleries",
        "Weltberühmte Felsenschlucht mit tosenden Wildwassern, Tropfsteingalerien und spektakulären Hängebrücken am Fuß der Wettersteinwand." to "World-famous rock gorge with roaring rapids, dripstone galleries and spectacular suspension bridges at the foot of the Wetterstein wall.",
        // OSM & Wikipedia Satzbausteine
        "Sehenswerter Ort entlang deiner Route" to "Noteworthy highlight along your route",
        "mit spannender Geschichte und regionaler Bedeutung." to "with fascinating history and regional significance.",
        "direkt im gewählten Routen-Korridor." to "located directly within your selected route corridor.",
        "Kostenlos zugänglich ohne Eintrittsgebühr." to "Freely accessible without admission fee.",
        "Quelle: Live OpenStreetMap / Overpass API." to "Source: Live OpenStreetMap / Overpass API.",
        "Quelle: OpenStreetMap & Photon POI-Suche." to "Source: OpenStreetMap & Photon POI Search.",
        "Jährliches Kultur- und Festival-Highlight im Korridor deiner Route." to "Annual cultural and festival highlight within your route corridor.",
        "Entdeckung über Geoapify Places API entlang der Route." to "Discovery via Geoapify Places API along the route.",
        "Aktuelles Programm / Spielplan vor Ort" to "Current program / schedule on site",
        "Saison- & Tagesevent" to "Seasonal & Day Event",
        "Heute / Demnächst" to "Today / Upcoming",
        "Kultur-Event & Festival • Weltweit (Wikidata/OSM)" to "Cultural Event & Festival • Worldwide (Wikidata/OSM)",
        "Wikipedia & Wikidata Highlight" to "Wikipedia & Wikidata Highlight",
        "Panorama-Aussichtspunkt • Natur & Weitblick" to "Panoramic Viewpoint • Nature & Scenic Vista",
        "Wasserfall • Natur-Highlight" to "Waterfall • Nature Highlight",
        "Naturhöhle & Geotop" to "Natural Cave & Geotope",
        "Natur- & Erholungsort entlang der Route" to "Nature & recreation spot along the route",
        "Erlebnis- & Freizeit-Attraktion" to "Adventure & leisure attraction",
        "Veranstaltungsort, Bühne & Kultur-Events" to "Event venue, stage & cultural events",
        "Kostenloses Entdecker-Ziel (Eintritt frei)" to "Free discovery destination (free admission)",
        "Café & Kaffeepause" to "Café & coffee break",
        "Restaurant / Einkehr" to "Restaurant & dining stop",
        "Wanderung, Steig & Einkehrhütte" to "Hike, trail & mountain hut",
        "Historisches Denkmal" to "Historic monument",
        "Kultur, Museum & Kunst" to "Culture, museum & art",
        "Öffnungszeiten:" to "Opening hours:",
        "Panorama-Aussichtspunkt" to "Panoramic Viewpoint",
        "Panorama-Aussicht" to "Panoramic View",
        "Aussichtspunkt" to "Scenic Viewpoint",
        "Aussichtsturm" to "Observation Tower",
        "Aussicht" to "Scenic View",
        "Wasserfälle" to "Waterfalls",
        "Wasserfall" to "Waterfall",
        "Burgruine" to "Castle Ruins",
        "Burg / Schloss" to "Castle & Palace",
        "Schlossgarten" to "Castle Gardens",
        "Schloss" to "Palace / Castle",
        "Burg" to "Castle",
        "Festung" to "Fortress",
        "Kathedrale / Dom" to "Cathedral",
        "Kathedrale" to "Cathedral",
        "Dom" to "Cathedral",
        "Wallfahrtskirche" to "Pilgrimage Church",
        "Pfarrkirche" to "Parish Church",
        "Kirche" to "Church",
        "Kloster" to "Monastery",
        "Abtei" to "Abbey",
        "Kapelle" to "Chapel",
        "Residenz" to "Residence",
        "Naturschutzgebiet" to "Nature Reserve",
        "Nationalpark" to "National Park",
        "Rundwanderung" to "Loop Hike",
        "Wanderwegenetz" to "Hiking Trail Network",
        "Wanderwege" to "Hiking Trails",
        "Wanderweg" to "Hiking Trail",
        "Wanderungen" to "Hikes",
        "Wanderung" to "Hike",
        "Berggipfel" to "Mountain Peak",
        "Berghütte" to "Mountain Hut",
        "Einkehrhütte" to "Mountain Inn",
        "Alpenvereinshütte" to "Alpine Club Hut",
        "Gipfel" to "Peak",
        "Badesee" to "Swimming Lake",
        "Bergsee" to "Alpine Lake",
        "Stausee" to "Reservoir Lake",
        "Seeufer" to "Lakeshore",
        "Seepromenade" to "Lakeside Promenade",
        "Uferpromenade" to "Waterfront Promenade",
        "See" to "Lake",
        "Klamm / Schlucht" to "Gorge & Canyon",
        "Felsenschlucht" to "Rock Gorge",
        "Klamm" to "Gorge",
        "Schlucht" to "Gorge",
        "Hängebrücke" to "Suspension Bridge",
        "Brücke" to "Bridge",
        "Turm" to "Tower",
        "Leuchtturm" to "Lighthouse",
        "Historische Altstadt" to "Historic Old Town",
        "Altstadt" to "Old Town",
        "Marktplatz" to "Market Square",
        "Wochenmarkt" to "Farmers Market",
        "Markt" to "Market",
        "Botanischer Garten" to "Botanical Garden",
        "Kurpark" to "Spa Park",
        "Schlosspark" to "Palace Park",
        "Freizeitpark" to "Amusement Park",
        "Erlebnispark" to "Adventure Park",
        "Kletterwald" to "Climbing Forest",
        "Hochseilgarten" to "High Ropes Course",
        "Sommerrodelbahn" to "Summer Toboggan Run",
        "Therme" to "Thermal Spa",
        "Thermalbad" to "Thermal Bath",
        "Wildpark" to "Wildlife Park",
        "Tierpark / Zoo" to "Zoo & Wildlife Park",
        "Tierpark" to "Wildlife Park",
        "Brauerei" to "Brewery",
        "Biergarten" to "Beer Garden",
        "Weingut" to "Winery",
        "Weinberg" to "Vineyard",
        "Kaffeerösterei" to "Coffee Roastery",
        "Hofcafé" to "Farm Café",
        "Eisdiele" to "Ice Cream Parlor",
        "Konditorei" to "Pastry Shop",
        "Gasthof" to "Traditional Inn",
        "Wirtshaus" to "Bavarian Inn",
        "Ausstellung" to "Exhibition",
        "Konzert" to "Concert",
        "Festspiele" to "Festival",
        "Freilichtbühne" to "Open-Air Stage",
        "Eintritt frei" to "Free Admission",
        "Freier Eintritt" to "Free Admission",
        "Kostenlos" to "Free",
        "Historisches" to "Historic",
        "Kulturelles" to "Culture",
        "Kultur" to "Culture",
        "Erlebnis" to "Adventure",
        "Natur" to "Nature",
        "Tagesevents" to "Day Events",
        "Tagesevent" to "Day Event",
        "ist eine Gemeinde" to "is a municipality",
        "ist eine Stadt" to "is a town",
        "ist ein Ortsteil" to "is a district",
        "ist ein Berg" to "is a mountain",
        "ist ein See" to "is a lake",
        "ist ein Schloss" to "is a castle",
        "ist eine Burg" to "is a castle",
        "im Landkreis" to "in the district of",
        "in Oberbayern" to "in Upper Bavaria",
        "in Bayern" to "in Bavaria",
        "in Deutschland" to "in Germany",
        "Jahrhundert" to "century",
        "erbaut" to "built",
        "gegründet" to "founded",
        "befindet sich" to "is located",
        "liegt" to "is situated",
        "nahe" to "near",
        "südlich von" to "south of",
        "nördlich von" to "north of",
        "westlich von" to "west of",
        "östlich von" to "east of",
        "Samstag" to "Saturday",
        "Sonntag" to "Sunday",
        "Montag" to "Monday",
        "Dienstag" to "Tuesday",
        "Mittwoch" to "Wednesday",
        "Donnerstag" to "Thursday",
        "Freitag" to "Friday",
        "Dieses Wochenende" to "This Weekend",
        "Wochenende" to "Weekend",
        "Heute" to "Today",
        "Morgen" to "Tomorrow",
        "Täglich" to "Daily"
    )

    /**
     * Erkennt zuverlässig, ob ein Text bereits auf Deutsch verfasst ist.
     */
    fun isAlreadyGerman(text: String): Boolean {
        if (text.isBlank()) return true
        val lower = text.lowercase()

        val words = lower.split(Regex("[^\\p{L}]+")).filter { it.length >= 2 }
        if (words.isEmpty()) return true

        val deHits = words.count { it in strictGermanWords }
        val enHits = words.count { it in strictEnglishWords }

        if (enHits > deHits && enHits >= 2) return false
        if (lower.any { it == 'ä' || it == 'ö' || it == 'ü' || it == 'ß' } && deHits >= enHits) {
            return true
        }
        val ratio = deHits.toDouble() / words.size.toDouble()
        return ratio >= 0.14 || (words.size <= 5 && deHits >= 1 && enHits == 0)
    }

    /**
     * Erkennt zuverlässig, ob ein Text bereits auf Englisch verfasst ist.
     */
    fun isAlreadyEnglish(text: String): Boolean {
        if (text.isBlank()) return true
        val lower = text.lowercase()
        if (lower.any { it == 'ä' || it == 'ö' || it == 'ü' || it == 'ß' }) {
            // Kann ein deutscher Eigenname im englischen Satz sein – prüfe Wortverhältnis
            val words = lower.split(Regex("[^\\p{L}]+")).filter { it.length >= 2 }
            val deHits = words.count { it in strictGermanWords }
            val enHits = words.count { it in strictEnglishWords }
            return enHits > deHits && enHits >= 2
        }
        val words = lower.split(Regex("[^\\p{L}]+")).filter { it.length >= 2 }
        if (words.isEmpty()) return true
        val deHits = words.count { it in strictGermanWords }
        val enHits = words.count { it in strictEnglishWords }
        if (deHits > enHits && deHits >= 1) return false
        val ratio = enHits.toDouble() / words.size.toDouble()
        return ratio >= 0.14 || (words.size <= 5 && enHits >= 1 && deHits == 0)
    }

    /**
     * Übersetzt einen Text asynchron ins Deutsche (für Wikipedia, OSM, Wikidata, Ticketmaster etc.).
     */
    suspend fun translateToGerman(rawText: String): String = withContext(Dispatchers.IO) {
        val text = rawText.trim()
        if (text.isBlank()) return@withContext text

        deCache[text]?.let { return@withContext it }

        if (isAlreadyGerman(text)) {
            deCache[text] = text
            return@withContext text
        }

        val lower = text.lowercase()
        enToDePhrases[lower]?.let {
            deCache[text] = it
            return@withContext it
        }

        val apiTranslated = translateViaOnlineApis(text, sourceLang = "auto", targetLang = "de")
        if (!apiTranslated.isNullOrBlank()) {
            deCache[text] = apiTranslated
            return@withContext apiTranslated
        }

        val fallback = translateToGermanSync(text)
        deCache[text] = fallback
        fallback
    }

    /**
     * Übersetzt einen Text asynchron ins Englische (für Wikipedia-Extrakte, OSM-Beschreibungen etc.).
     */
    suspend fun translateToEnglishAsync(rawText: String): String = withContext(Dispatchers.IO) {
        val text = rawText.trim()
        if (text.isBlank()) return@withContext text

        enCache[text]?.let { return@withContext it }

        deToEnPhrases[text]?.let {
            enCache[text] = it
            return@withContext it
        }

        if (isAlreadyEnglish(text)) {
            enCache[text] = text
            return@withContext text
        }

        val apiTranslated = translateViaOnlineApis(text, sourceLang = "de", targetLang = "en")
        if (!apiTranslated.isNullOrBlank()) {
            enCache[text] = apiTranslated
            return@withContext apiTranslated
        }

        val fallback = translateToEnglish(text)
        enCache[text] = fallback
        fallback
    }

    /**
     * Mehrstufige Online-Übersetzung:
     * 1. Google Translate Neural GTX API (schnell & präzise für ganze Wikipedia-Absätze)
     * 2. MyMemory Translation REST API (zuverlässiger Zweit-Provider)
     */
    private fun translateViaOnlineApis(
        text: String,
        sourceLang: String,
        targetLang: String
    ): String? {
        // Provider 1: Google Neural GTX Endpoint
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encoded"
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android 14; Mobile; SeizeTheWayTranslator/2.0)")
            }
            try {
                if (connection.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val sentences = jsonArray.optJSONArray(0)
                    if (sentences != null && sentences.length() > 0) {
                        val sb = StringBuilder()
                        for (i in 0 until sentences.length()) {
                            val part = sentences.optJSONArray(i)
                            if (part != null && part.length() > 0) {
                                sb.append(part.optString(0, ""))
                            }
                        }
                        val result = sb.toString().trim()
                        if (result.isNotBlank() && !result.equals(text, ignoreCase = true)) {
                            return result
                        }
                    }
                }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
        }

        // Provider 2: MyMemory Translation API Fallback
        if (text.length <= 480) {
            try {
                val src = if (sourceLang == "auto") {
                    if (targetLang == "de") "en" else "de"
                } else sourceLang
                val encoded = URLEncoder.encode(text, "UTF-8")
                val urlString = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$src|$targetLang"
                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("User-Agent", "SeizeTheWay-Android-App/2.0")
                }
                try {
                    if (connection.responseCode == 200) {
                        val response = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
                        val json = JSONObject(response)
                        val translated = json.optJSONObject("responseData")?.optString("translatedText", "")?.trim()
                        if (!translated.isNullOrBlank() && !translated.contains("MYMEMORY WARNING", ignoreCase = true)) {
                            return translated
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (_: Exception) {
            }
        }

        return null
    }

    /**
     * Synchroner Deutsch-Fallback für Offline-Betrieb.
     */
    fun translateToGermanSync(text: String): String {
        if (text.isBlank()) return text
        deCache[text]?.let { return it }
        var res = text
        for ((en, de) in enToDePhrases) {
            val regex = Regex("(?i)\\b${Regex.escape(en)}\\b")
            if (regex.containsMatchIn(res)) {
                res = regex.replace(res, de)
            }
        }
        deCache[text] = res
        return res
    }

    /**
     * Synchroner Englisch-Übersetzer (nutzt den durch `translateItemsBatch` gefüllten Cache
     * sowie das umfangreiche Satz- & Phrasen-Wörterbuch).
     */
    fun translateToEnglish(text: String): String {
        if (text.isBlank()) return text
        enCache[text]?.let { return it }

        deToEnPhrases[text]?.let {
            enCache[text] = it
            return it
        }

        var res = text
        for ((de, en) in deToEnPhrases) {
            if (res.contains(de, ignoreCase = true)) {
                val regex = if (de.any { !it.isLetterOrDigit() }) {
                    Regex("(?i)${Regex.escape(de)}")
                } else {
                    Regex("(?i)\\b${Regex.escape(de)}\\b")
                }
                res = regex.replace(res, en)
            }
        }
        enCache[text] = res
        return res
    }

    /**
     * Übersetzt alle relevanten Textfelder eines RouteDiscoveryItem sowohl ins Deutsche
     * als auch ins Englische (bilinguale Vorübersetzung für sofortigen Sprachwechsel).
     */
    suspend fun translateItem(item: RouteDiscoveryItem): RouteDiscoveryItem = withContext(Dispatchers.IO) {
        val deTitle = translateToGerman(item.title)
        val deSub = translateToGerman(item.subtitle)
        val deDesc = translateToGerman(item.description)
        val deEvent = item.eventDateText?.let { translateToGerman(it) }

        val enTitle = if (item.titleEn.isNotBlank()) item.titleEn else translateToEnglishAsync(deTitle)
        val enSub = if (item.subtitleEn.isNotBlank()) item.subtitleEn else translateToEnglishAsync(deSub)
        val enDesc = if (item.descriptionEn.isNotBlank()) item.descriptionEn else translateToEnglishAsync(deDesc)
        val enEvent = item.eventDateTextEn ?: deEvent?.let { translateToEnglishAsync(it) }

        // Lege auch unter den deutschen Schlüsseln im enCache ab, damit synchrone Aufrufe sofort treffen
        enCache[deTitle] = enTitle
        enCache[deSub] = enSub
        enCache[deDesc] = enDesc
        if (deEvent != null && enEvent != null) {
            enCache[deEvent] = enEvent
        }

        item.copy(
            title = deTitle,
            subtitle = deSub,
            description = deDesc,
            titleEn = enTitle,
            subtitleEn = enSub,
            descriptionEn = enDesc,
            eventDateText = deEvent,
            eventDateTextEn = enEvent
        )
    }

    /**
     * Parallele Batch-Übersetzung aller gefundenen POIs/Events entlang der Route,
     * sodass Wikipedia-Artikel, OSM-POIs und Events verzögerungsfrei in DE und EN vorliegen.
     */
    suspend fun translateItemsBatch(items: List<RouteDiscoveryItem>): List<RouteDiscoveryItem> = coroutineScope {
        items.chunked(10).flatMap { chunk ->
            chunk.map { item ->
                async(Dispatchers.IO) {
                    translateItem(item)
                }
            }.awaitAll()
        }
    }
}

fun RouteDiscoveryItem.displayTitle(lang: AppLanguage): String =
    if (lang.isGerman) title else titleEn.ifBlank { GermanTranslator.translateToEnglish(title) }

fun RouteDiscoveryItem.displaySubtitle(lang: AppLanguage): String =
    if (lang.isGerman) subtitle else subtitleEn.ifBlank { GermanTranslator.translateToEnglish(subtitle) }

fun RouteDiscoveryItem.displayDescription(lang: AppLanguage): String =
    if (lang.isGerman) description else descriptionEn.ifBlank { GermanTranslator.translateToEnglish(description) }

fun RouteDiscoveryItem.displayEventDate(lang: AppLanguage): String? =
    if (lang.isGerman) {
        eventDateText
    } else {
        eventDateTextEn?.ifBlank { null } ?: eventDateText?.let { GermanTranslator.translateToEnglish(it) }
    }

fun String.localizedTag(lang: AppLanguage): String =
    if (lang.isGerman) this else GermanTranslator.translateToEnglish(this)
