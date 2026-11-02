package com.example.seizetheway.data

import com.example.seizetheway.model.RouteDiscoveryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Automatischer Übersetzungs-Service:
 * Übersetzt fremdsprachige Ergebnisdaten (Wikipedia, OSM, Ticketmaster, Eventbrite, Wikidata)
 * vollautomatisch ins Deutsche.
 * Nutzt schnelles Online-Caching und ein integriertes Vokabular für POIs und Sehenswürdigkeiten.
 */
object GermanTranslator {

    private val cache = ConcurrentHashMap<String, String>()

    // Häufige deutsche Wörter zur Erkennung, ob ein Text bereits Deutsch ist
    private val germanWordMarkers = setOf(
        "der", "die", "das", "ein", "eine", "einer", "einem", "einen",
        "und", "in", "im", "am", "an", "auf", "aus", "mit", "für", "von",
        "bei", "nach", "über", "unter", "zwischen", "vor", "durch", "nicht",
        "ist", "sind", "war", "waren", "wird", "werden", "wurde", "wurden",
        "sowie", "auch", "sehr", "schön", "groß", "alt", "neu", "stadt", "berg",
        "see", "burg", "schloss", "kirche", "museum", "wanderweg", "aussicht"
    )

    private val poiGlossary = mapOf(
        "viewpoint" to "Aussichtspunkt",
        "scenic viewpoint" to "Panorama-Aussichtspunkt",
        "waterfall" to "Wasserfall",
        "waterfalls" to "Wasserfälle",
        "castle" to "Burg / Schloss",
        "ruins" to "Burgruine",
        "fortress" to "Festung",
        "cathedral" to "Kathedrale / Dom",
        "church" to "Kirche",
        "monastery" to "Kloster",
        "abbey" to "Abtei",
        "chapel" to "Kapelle",
        "palace" to "Palast / Residenz",
        "nature reserve" to "Naturschutzgebiet",
        "national park" to "Nationalpark",
        "hiking trail" to "Wanderweg",
        "mountain peak" to "Berggipfel",
        "lake" to "See",
        "gorge" to "Klamm / Schlucht",
        "canyon" to "Schlucht",
        "bridge" to "Brücke",
        "tower" to "Turm",
        "lighthouse" to "Leuchtturm",
        "historic center" to "Historische Altstadt",
        "old town" to "Altstadt",
        "market square" to "Marktplatz",
        "botanical garden" to "Botanischer Garten",
        "amusement park" to "Freizeitpark",
        "theme park" to "Erlebnispark",
        "zoo" to "Tierpark / Zoo",
        "brewery" to "Brauerei",
        "beer garden" to "Biergarten",
        "winery" to "Weingut",
        "vineyard" to "Weinberg",
        "exhibition" to "Ausstellung",
        "concert" to "Konzert",
        "festival" to "Festival / Festspiele",
        "open air" to "Open-Air",
        "free admission" to "Freier Eintritt",
        "free entry" to "Eintritt frei"
    )

    /**
     * Prüft heuristisch, ob ein Text bereits überwiegend deutsch ist.
     */
    fun isAlreadyGerman(text: String): Boolean {
        if (text.isBlank()) return true
        val lower = text.lowercase()

        // Enthält deutsche Umlaute oder Eszett
        if (lower.any { it == 'ä' || it == 'ö' || it == 'ü' || it == 'ß' }) {
            return true
        }

        val words = lower.split(Regex("[^\\p{L}]+")).filter { it.length >= 2 }
        if (words.isEmpty()) return true

        val germanHits = words.count { it in germanWordMarkers }
        val ratio = germanHits.toDouble() / words.size.toDouble()

        // Wenn mind. 15% der Wörter typisch deutsch sind
        return ratio >= 0.15 || (words.size <= 4 && germanHits >= 1)
    }

    /**
     * Übersetzt einen gegebenen Text asynchron ins Deutsche.
     */
    suspend fun translateToGerman(rawText: String): String = withContext(Dispatchers.IO) {
        val text = rawText.trim()
        if (text.isBlank()) return@withContext text

        // 1. Cache-Prüfung
        cache[text]?.let { return@withContext it }

        // 2. Prüfung, ob bereits Deutsch
        if (isAlreadyGerman(text)) {
            cache[text] = text
            return@withContext text
        }

        // 3. Schnelle lokale Glossar-Ersetzung für Standard-POI-Begriffe
        val lower = text.lowercase()
        poiGlossary[lower]?.let {
            cache[text] = it
            return@withContext it
        }

        // 4. Online-Übersetzung via Google Translate Web-Endpoint
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=de&dt=t&q=$encoded"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3500
                readTimeout = 3500
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; SeizeTheWayApp/1.0)")
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                connection.disconnect()

                val jsonArray = JSONArray(response)
                val sentences = jsonArray.optJSONArray(0)
                if (sentences != null && sentences.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until sentences.length()) {
                        val part = sentences.getJSONArray(i)
                        sb.append(part.getString(0))
                    }
                    val translated = sb.toString().trim()
                    if (translated.isNotBlank()) {
                        cache[text] = translated
                        return@withContext translated
                    }
                }
            }
        } catch (_: Exception) {
            // Bei Netzwerk-Timeout oder Offline-Betrieb: Fallback auf lokales Glossar
        }

        // 5. Fallback: Ersetze bekannte englische Wörter im Text
        var fallbackResult = text
        for ((en, de) in poiGlossary) {
            val regex = Regex("(?i)\\b${Regex.escape(en)}\\b")
            if (regex.containsMatchIn(fallbackResult)) {
                fallbackResult = regex.replace(fallbackResult, de)
            }
        }

        cache[text] = fallbackResult
        return@withContext fallbackResult
    }

    private val germanToEnglishGlossary = mapOf(
        "Seepromenade & Schlossgarten Starnberg" to "Lakeside Promenade & Starnberg Castle Gardens",
        "Alpenblick am Starnberger See • Kostenlos" to "Alpine view at Lake Starnberg • Free",
        "Weitläufige Uferpromenade am Starnberger See mit freiem Blick bis zur Zugspitze, Badestegen und dem historischen Schlossgarten." to "Spacious lakeside promenade at Lake Starnberg with clear views of the Zugspitze, bathing docks and historic castle gardens.",
        "Buchheim Museum der Phantasie (Bernried)" to "Buchheim Museum of Imagination (Bernried)",
        "Expressionismus & Architektur direkt am Seeufer" to "Expressionism & architecture directly on the lakeshore",
        "Preisgekröntes Museum für Expressionismus, Volkskunst und Skulpturenpark mit einem über dem Starnberger See schwebenden Aussichtssteg." to "Award-winning museum for expressionism, folk art and sculpture park with a scenic walkway suspended over Lake Starnberg.",
        "Murnauer Moos – Bohlenweg-Rundwanderung" to "Murnau Moor – Plank Path Loop Hike",
        "Größtes zusammenhängendes Moorgebiet Mitteleuropas" to "Central Europe's largest contiguous moorland area",
        "Traumhafte, kostenfreie Rundwanderung über Holzbohlenwege durch das Murnauer Moos mit einzigartiger Flora und Wetterstein-Panorama." to "Dreamy, free loop hike across wooden boardwalks through Murnau Moor with unique flora and Wetterstein alpine panorama.",
        "Murnauer Kaffeerösterei & Café" to "Murnau Coffee Roastery & Café",
        "Spezialitätenkaffee & hausgemachte Kuchen in der Fußgängerzone" to "Specialty coffee & homemade pastries in pedestrian zone",
        "Traditionsreiche Kaffeerösterei im Herzen von Murnau am Staffelsee – ideal für einen Espresso-Stopp und frisch geröstete Bohnen." to "Traditional specialty coffee roastery in the heart of Murnau am Staffelsee – ideal for an espresso break and freshly roasted beans.",
        "Kloster Ettal & Klostergasthof" to "Ettal Abbey & Monastery Inn",
        "Barocke Benediktinerabtei, Schaukäserei & Brauerei" to "Baroque Benedictine abbey, showcase dairy & brewery",
        "Imposante Barockbasilika von 1330 im Ammergauer Alpenkessel mit traditionellem Klostergasthof, Brauerei und freiem Eintritt in die Basilika." to "Impressive baroque basilica from 1330 in the Ammergau Alps basin with traditional monastery inn, brewery and free basilica admission.",
        "Konzertpavillon & Kurpark-Bühne Garmisch" to "Concert Pavilion & Spa Park Stage Garmisch",
        "Kostenlose Kurkonzerte & Blumenpromenade" to "Free spa concerts & flowering promenade",
        "Historischer Konzertpavillon im gepflegten Michael-Ende-Kurpark mit regelmäßigen Live-Freiluftkonzerten und Heilkräutergarten." to "Historic concert pavilion in the landscaped Michael Ende Spa Park featuring open-air live concerts and herbal garden.",
        "Partnachklamm & Graseck-Steig" to "Partnach Gorge & Graseck Trail",
        "Naturdenkmal mit tosenden Wasserfällen & Felsengalerien" to "Natural monument with roaring waterfalls & rock galleries",
        "Weltberühmte Felsenschlucht mit tosenden Wildwassern, Tropfsteingalerien und spektakulären Hängebrücken am Fuß der Wettersteinwand." to "World-famous gorge with rushing white water, dripstone galleries and spectacular suspension paths beneath the Wetterstein wall.",
        "Panorama-Aussichtspunkt" to "Panoramic Viewpoint",
        "Panorama-Aussicht" to "Panoramic View",
        "Aussichtspunkt" to "Scenic Viewpoint",
        "Aussicht" to "Scenic View",
        "Wasserfälle" to "Waterfalls",
        "Wasserfall" to "Waterfall",
        "Burg / Schloss" to "Castle & Palace",
        "Burgruine" to "Castle Ruins",
        "Burg" to "Castle",
        "Schloss" to "Palace / Castle",
        "Festung" to "Fortress",
        "Kathedrale / Dom" to "Cathedral",
        "Kathedrale" to "Cathedral",
        "Dom" to "Cathedral",
        "Kirche" to "Church",
        "Kloster" to "Monastery",
        "Abtei" to "Abbey",
        "Kapelle" to "Chapel",
        "Palast / Residenz" to "Palace",
        "Palast" to "Palace",
        "Residenz" to "Residence",
        "Naturschutzgebiet" to "Nature Reserve",
        "Nationalpark" to "National Park",
        "Wanderweg" to "Hiking Trail",
        "Wanderungen" to "Hikes",
        "Wanderung" to "Hike",
        "Berggipfel" to "Mountain Peak",
        "Gipfel" to "Peak",
        "See" to "Lake",
        "Badesee" to "Swimming Lake",
        "Klamm / Schlucht" to "Gorge & Canyon",
        "Klamm" to "Gorge",
        "Schlucht" to "Canyon",
        "Brücke" to "Bridge",
        "Turm" to "Tower",
        "Leuchtturm" to "Lighthouse",
        "Historische Altstadt" to "Historic Old Town",
        "Altstadt" to "Old Town",
        "Marktplatz" to "Market Square",
        "Markt" to "Market",
        "Botanischer Garten" to "Botanical Garden",
        "Freizeitpark" to "Amusement Park",
        "Erlebnispark" to "Adventure Park",
        "Tierpark / Zoo" to "Zoo & Wildlife Park",
        "Tierpark" to "Zoo",
        "Zoo" to "Zoo",
        "Brauerei" to "Brewery",
        "Biergarten" to "Beer Garden",
        "Weingut" to "Winery",
        "Weinberg" to "Vineyard",
        "Ausstellung" to "Exhibition",
        "Konzert" to "Concert",
        "Festival / Festspiele" to "Festival",
        "Festspiele" to "Festival",
        "Festival" to "Festival",
        "Open-Air" to "Open-Air",
        "Freier Eintritt" to "Free Admission",
        "Eintritt frei" to "Free Admission",
        "Kostenlos" to "Free",
        "Kultur" to "Culture",
        "Kulturelles" to "Culture",
        "Erlebnis" to "Adventure",
        "Natur" to "Nature",
        "Events" to "Events",
        "Tagesevents" to "Events",
        "Cafés" to "Cafés",
        "Restaurants" to "Restaurants",
        "Historisches" to "History",
        "Samstag" to "Saturday",
        "Sonntag" to "Sunday",
        "Montag" to "Monday",
        "Dienstag" to "Tuesday",
        "Mittwoch" to "Wednesday",
        "Donnerstag" to "Thursday",
        "Freitag" to "Friday",
        "Wochenende" to "Weekend",
        "Dieses Wochenende" to "This Weekend",
        "Heute" to "Today",
        "Morgen" to "Tomorrow",
        "Geplante Tour" to "Planned Tour",
        "24/7 frei zugänglich" to "Open 24/7",
        "Täglich frei zugänglich" to "Open daily free admission",
        "Täglich" to "Daily",
        "Direkt an der Route" to "Directly on route",
        "Abstecher" to "detour",
        "Minuten" to "minutes",
        "Min." to "min"
    )

    fun translateToEnglish(text: String): String {
        if (text.isBlank()) return text
        cache["en_$text"]?.let { return it }

        // Exact match
        germanToEnglishGlossary[text]?.let {
            cache["en_$text"] = it
            return it
        }

        var res = text
        for ((de, en) in germanToEnglishGlossary) {
            val regex = Regex("(?i)\\b${Regex.escape(de)}\\b")
            res = regex.replace(res, en)
        }
        cache["en_$text"] = res
        return res
    }

    /**
     * Übersetzt alle relevanten Textfelder eines RouteDiscoveryItem ins Deutsche.
     */
    suspend fun translateItem(item: RouteDiscoveryItem): RouteDiscoveryItem = withContext(Dispatchers.IO) {
        val transTitle = translateToGerman(item.title)
        val transSubtitle = translateToGerman(item.subtitle)
        val transDesc = translateToGerman(item.description)

        item.copy(
            title = transTitle,
            subtitle = transSubtitle,
            description = transDesc
        )
    }
}

fun RouteDiscoveryItem.displayTitle(lang: com.example.seizetheway.model.AppLanguage): String =
    if (lang.isGerman) title else GermanTranslator.translateToEnglish(title)

fun RouteDiscoveryItem.displaySubtitle(lang: com.example.seizetheway.model.AppLanguage): String =
    if (lang.isGerman) subtitle else GermanTranslator.translateToEnglish(subtitle)

fun RouteDiscoveryItem.displayDescription(lang: com.example.seizetheway.model.AppLanguage): String =
    if (lang.isGerman) description else GermanTranslator.translateToEnglish(description)

fun RouteDiscoveryItem.displayEventDate(lang: com.example.seizetheway.model.AppLanguage): String? =
    eventDateText?.let { if (lang.isGerman) it else GermanTranslator.translateToEnglish(it) }
