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
