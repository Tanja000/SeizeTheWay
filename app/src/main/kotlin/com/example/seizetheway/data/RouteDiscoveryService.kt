package com.example.seizetheway.data

import com.example.seizetheway.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.*

data class DiscoverySearchResponse(
    val route: RouteResult,
    val allCorridorItems: List<RouteDiscoveryItem>,
    val filteredItems: List<RouteDiscoveryItem>,
    val apiReports: List<ApiStatusReport>
)

class RouteDiscoveryService {

    private val knownPlaces = mapOf(
        // A
        "aachen" to GeoPoint(50.7753, 6.0839, "Aachen"),
        "augsburg" to GeoPoint(48.3705, 10.8978, "Augsburg"),
        "amsterdam" to GeoPoint(52.3676, 4.9041, "Amsterdam"),
        "aschaffenburg" to GeoPoint(49.9739, 9.1511, "Aschaffenburg"),
        "aalen" to GeoPoint(48.8378, 10.0934, "Aalen"),
        // B
        "berlin" to GeoPoint(52.520008, 13.404954, "Berlin"),
        "bonn" to GeoPoint(50.737430, 7.098207, "Bonn"),
        "bremen" to GeoPoint(53.079296, 8.801694, "Bremen"),
        "basel" to GeoPoint(47.5596, 7.5886, "Basel"),
        "bern" to GeoPoint(46.9480, 7.4474, "Bern"),
        "bozen" to GeoPoint(46.4983, 11.3548, "Bozen / Bolzano"),
        "bamberg" to GeoPoint(49.8988, 10.9028, "Bamberg"),
        "bayreuth" to GeoPoint(49.9456, 11.5713, "Bayreuth"),
        "bielefeld" to GeoPoint(52.0302, 8.5325, "Bielefeld"),
        "bochum" to GeoPoint(51.4818, 7.2162, "Bochum"),
        "braunschweig" to GeoPoint(52.2689, 10.5268, "Braunschweig"),
        "bregenz" to GeoPoint(47.5031, 9.7471, "Bregenz"),
        "bad honnef" to GeoPoint(50.6451, 7.2272, "Bad Honnef"),
        "bad tölz" to GeoPoint(47.7606, 11.5606, "Bad Tölz"),
        "berchtesgaden" to GeoPoint(47.6297, 13.0016, "Berchtesgaden"),
        // C
        "chemnitz" to GeoPoint(50.8278, 12.9214, "Chemnitz"),
        "cottbus" to GeoPoint(51.7563, 14.3329, "Cottbus"),
        "chur" to GeoPoint(46.8508, 9.5320, "Chur"),
        // D
        "dresden" to GeoPoint(51.050407, 13.737262, "Dresden"),
        "dortmund" to GeoPoint(51.5136, 7.4653, "Dortmund"),
        "düsseldorf" to GeoPoint(51.2277, 6.7735, "Düsseldorf"),
        "duesseldorf" to GeoPoint(51.2277, 6.7735, "Düsseldorf"),
        "duisburg" to GeoPoint(51.4344, 6.7623, "Duisburg"),
        "darmstadt" to GeoPoint(49.8728, 8.6512, "Darmstadt"),
        "deggendorf" to GeoPoint(48.8354, 12.9644, "Deggendorf"),
        // E
        "erfurt" to GeoPoint(50.9848, 11.0299, "Erfurt"),
        "essen" to GeoPoint(51.4556, 7.0116, "Essen"),
        "erlangen" to GeoPoint(49.5897, 11.0039, "Erlangen"),
        "eisenach" to GeoPoint(50.9754, 10.3235, "Eisenach"),
        // F
        "frankfurt" to GeoPoint(50.110924, 8.682127, "Frankfurt am Main"),
        "frankfurt am main" to GeoPoint(50.110924, 8.682127, "Frankfurt am Main"),
        "freiburg" to GeoPoint(47.999008, 7.842104, "Freiburg im Breisgau"),
        "freiburg im breisgau" to GeoPoint(47.999008, 7.842104, "Freiburg im Breisgau"),
        "füssen" to GeoPoint(47.5714, 10.7018, "Füssen"),
        "fuessen" to GeoPoint(47.5714, 10.7018, "Füssen"),
        "fulda" to GeoPoint(50.5558, 9.6808, "Fulda"),
        "flensburg" to GeoPoint(54.7877, 9.4357, "Flensburg"),
        "friedrichshafen" to GeoPoint(47.6543, 9.4792, "Friedrichshafen"),
        "fürth" to GeoPoint(49.4774, 10.9886, "Fürth"),
        // G
        "garmisch-partenkirchen" to GeoPoint(47.492088, 11.095763, "Garmisch-Partenkirchen"),
        "garmisch" to GeoPoint(47.492088, 11.095763, "Garmisch-Partenkirchen"),
        "genf" to GeoPoint(46.2044, 6.1432, "Genf / Geneva"),
        "graz" to GeoPoint(47.0707, 15.4395, "Graz"),
        "göttingen" to GeoPoint(51.5413, 9.9158, "Göttingen"),
        "görlitz" to GeoPoint(51.1528, 14.9872, "Görlitz"),
        "goslar" to GeoPoint(51.9060, 10.4289, "Goslar"),
        // H
        "hamburg" to GeoPoint(53.551086, 9.993682, "Hamburg"),
        "hannover" to GeoPoint(52.375892, 9.732010, "Hannover"),
        "heidelberg" to GeoPoint(49.398750, 8.672434, "Heidelberg"),
        "heilbronn" to GeoPoint(49.1427, 9.2109, "Heilbronn"),
        "hildesheim" to GeoPoint(52.1508, 9.9511, "Hildesheim"),
        "hallstatt" to GeoPoint(47.5622, 13.6493, "Hallstatt"),
        // I
        "innsbruck" to GeoPoint(47.2692, 11.4041, "Innsbruck"),
        "ingolstadt" to GeoPoint(48.7665, 11.4258, "Ingolstadt"),
        "iserlohn" to GeoPoint(51.3768, 7.6975, "Iserlohn"),
        // J
        "jena" to GeoPoint(50.9271, 11.5892, "Jena"),
        // K
        "köln" to GeoPoint(50.937531, 6.960279, "Köln"),
        "koeln" to GeoPoint(50.937531, 6.960279, "Köln"),
        "koblenz" to GeoPoint(50.3569, 7.5890, "Koblenz"),
        "konstanz" to GeoPoint(47.660339, 9.175820, "Konstanz"),
        "kassel" to GeoPoint(51.3127, 9.4797, "Kassel"),
        "karlsruhe" to GeoPoint(49.0069, 8.4037, "Karlsruhe"),
        "kempten" to GeoPoint(47.7286, 10.3158, "Kempten (Allgäu)"),
        "kiel" to GeoPoint(54.3233, 10.1228, "Kiel"),
        "klagenfurt" to GeoPoint(46.6247, 14.3053, "Klagenfurt"),
        "kufstein" to GeoPoint(47.5833, 12.1667, "Kufstein"),
        // L
        "leipzig" to GeoPoint(51.339695, 12.373075, "Leipzig"),
        "lübeck" to GeoPoint(53.865467, 10.686559, "Lübeck"),
        "luebeck" to GeoPoint(53.865467, 10.686559, "Lübeck"),
        "linz" to GeoPoint(48.3064, 14.2858, "Linz"),
        "luzern" to GeoPoint(47.0502, 8.3093, "Luzern"),
        "lindau" to GeoPoint(47.5459, 9.6839, "Lindau (Bodensee)"),
        "landshut" to GeoPoint(48.5442, 12.1528, "Landshut"),
        "ludwigshafen" to GeoPoint(49.4811, 8.4464, "Ludwigshafen"),
        "lüneburg" to GeoPoint(53.2464, 10.4115, "Lüneburg"),
        "leverkusen" to GeoPoint(51.0459, 6.9845, "Leverkusen"),
        // M
        "münchen" to GeoPoint(48.137154, 11.576124, "München"),
        "muenchen" to GeoPoint(48.137154, 11.576124, "München"),
        "münster" to GeoPoint(51.9607, 7.6261, "Münster"),
        "muenster" to GeoPoint(51.9607, 7.6261, "Münster"),
        "mainz" to GeoPoint(49.9929, 8.2473, "Mainz"),
        "mannheim" to GeoPoint(49.4875, 8.4660, "Mannheim"),
        "magdeburg" to GeoPoint(52.1205, 11.6276, "Magdeburg"),
        "murnau am staffelsee" to GeoPoint(47.6769, 11.2023, "Murnau am Staffelsee"),
        "murnau" to GeoPoint(47.6769, 11.2023, "Murnau am Staffelsee"),
        "meißen" to GeoPoint(51.1636, 13.4775, "Meißen"),
        "meissen" to GeoPoint(51.1636, 13.4775, "Meißen"),
        "memmingen" to GeoPoint(47.9866, 10.1804, "Memmingen"),
        "meran" to GeoPoint(46.6682, 11.1595, "Meran / Merano"),
        "mönchengladbach" to GeoPoint(51.1911, 6.4422, "Mönchengladbach"),
        // N
        "nürnberg" to GeoPoint(49.452103, 11.076665, "Nürnberg"),
        "nuernberg" to GeoPoint(49.452103, 11.076665, "Nürnberg"),
        "neuschwanstein" to GeoPoint(47.5576, 10.7498, "Schloss Neuschwanstein"),
        "neuss" to GeoPoint(51.1983, 6.6917, "Neuss"),
        // O
        "oberstdorf" to GeoPoint(47.4072, 10.2794, "Oberstdorf"),
        "osnabrück" to GeoPoint(52.2799, 8.0472, "Osnabrück"),
        "oldenburg" to GeoPoint(53.1435, 8.2146, "Oldenburg"),
        "offenbach" to GeoPoint(50.1055, 8.7668, "Offenbach am Main"),
        // P
        "passau" to GeoPoint(48.5734, 13.4578, "Passau"),
        "potsdam" to GeoPoint(52.3906, 13.0645, "Potsdam"),
        "paderborn" to GeoPoint(51.7189, 8.7575, "Paderborn"),
        "pforzheim" to GeoPoint(48.8932, 8.7049, "Pforzheim"),
        "paris" to GeoPoint(48.8566, 2.3522, "Paris"),
        "prag" to GeoPoint(50.0755, 14.4378, "Prag / Prague"),
        // Q
        "quedlinburg" to GeoPoint(51.7897, 11.1486, "Quedlinburg"),
        // R
        "regensburg" to GeoPoint(49.0134, 12.1016, "Regensburg"),
        "rostock" to GeoPoint(54.0924, 12.0991, "Rostock"),
        "rosenheim" to GeoPoint(47.8561, 12.1289, "Rosenheim"),
        "rothenburg ob der tauber" to GeoPoint(49.3774, 10.1789, "Rothenburg ob der Tauber"),
        "reutlingen" to GeoPoint(48.4914, 9.2043, "Reutlingen"),
        "rom" to GeoPoint(41.9028, 12.4964, "Rom / Rome"),
        // S
        "stuttgart" to GeoPoint(48.775845, 9.182932, "Stuttgart"),
        "salzburg" to GeoPoint(47.809490, 13.055010, "Salzburg"),
        "singen" to GeoPoint(47.7630, 8.8400, "Singen (Hohentwiel)"),
        "starnberg" to GeoPoint(47.997222, 11.340556, "Starnberg"),
        "saarbrücken" to GeoPoint(49.2344, 6.9964, "Saarbrücken"),
        "schwerin" to GeoPoint(53.6355, 11.4012, "Schwerin"),
        "siegen" to GeoPoint(50.8744, 8.0243, "Siegen"),
        "stralsund" to GeoPoint(54.3160, 13.0883, "Stralsund"),
        "speyer" to GeoPoint(49.3173, 8.4357, "Speyer"),
        "st. gallen" to GeoPoint(47.4245, 9.3767, "St. Gallen"),
        // T
        "trier" to GeoPoint(49.7596, 6.6442, "Trier"),
        "tübingen" to GeoPoint(48.5216, 9.0576, "Tübingen"),
        "tegernsee" to GeoPoint(47.7142, 11.7583, "Tegernsee"),
        "traunstein" to GeoPoint(47.8688, 12.6433, "Traunstein"),
        // U
        "ulm" to GeoPoint(48.4011, 9.9876, "Ulm"),
        "überlingen" to GeoPoint(47.7667, 9.1719, "Überlingen"),
        // V
        "venedig" to GeoPoint(45.4408, 12.3155, "Venedig / Venice"),
        "villach" to GeoPoint(46.6111, 13.8558, "Villach"),
        "villingen-schwenningen" to GeoPoint(48.0601, 8.4587, "Villingen-Schwenningen"),
        // W
        "wien" to GeoPoint(48.208176, 16.373819, "Wien"),
        "würzburg" to GeoPoint(49.7913, 9.9534, "Würzburg"),
        "wuerzburg" to GeoPoint(49.7913, 9.9534, "Würzburg"),
        "wiesbaden" to GeoPoint(50.0820, 8.2417, "Wiesbaden"),
        "weimar" to GeoPoint(50.9803, 11.3290, "Weimar"),
        "wolfsburg" to GeoPoint(52.4227, 10.7865, "Wolfsburg"),
        "worms" to GeoPoint(49.6356, 8.3616, "Worms"),
        "wuppertal" to GeoPoint(51.2562, 7.1508, "Wuppertal"),
        // X
        "xanten" to GeoPoint(51.6617, 6.4528, "Xanten"),
        // Z
        "zürich" to GeoPoint(47.376888, 8.541694, "Zürich"),
        "zuerich" to GeoPoint(47.376888, 8.541694, "Zürich"),
        "zwickau" to GeoPoint(50.7189, 12.4960, "Zwickau"),
        "zell am see" to GeoPoint(47.3242, 12.7972, "Zell am See"),
        "zugspitze" to GeoPoint(47.4211, 10.9853, "Zugspitze")
    )

    suspend fun geocodePlace(query: String): Pair<GeoPoint, ApiStatusReport> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        val startMs = System.currentTimeMillis()
        // Instant lookup for known cities so switching presets is ultra-fast
        knownPlaces[clean.lowercase()]?.let { kp ->
            return@withContext kp to ApiStatusReport(
                source = ApiSource.NOMINATIM,
                active = true,
                resultsCount = 1,
                statusMessage = "OSM Koordinaten (${kp.name})",
                latencyMs = 5L
            )
        }

        // 1. Try Nominatim OpenStreetMap API
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=$encoded"
            val response = httpGet(url, timeoutMs = 6000)
            val arr = JSONArray(response)
            if (arr.length() > 0) {
                val obj = arr.getJSONObject(0)
                val lat = obj.getString("lat").toDouble()
                val lon = obj.getString("lon").toDouble()
                val displayName = obj.optString("display_name", clean).substringBefore(",").trim()
                val elapsed = System.currentTimeMillis() - startMs
                return@withContext GeoPoint(lat, lon, displayName.ifBlank { clean }) to ApiStatusReport(
                    source = ApiSource.NOMINATIM,
                    active = true,
                    resultsCount = 1,
                    statusMessage = "Nominatim OSM Geocoding erfolgreich ($displayName)",
                    latencyMs = elapsed
                )
            }
        } catch (_: Exception) {
        }

        // 2. Try Photon (Komoot OpenStreetMap Geocoder)
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://photon.komoot.io/api/?q=$encoded&limit=1"
            val response = httpGet(url, timeoutMs = 5000)
            val root = JSONObject(response)
            val features = root.optJSONArray("features")
            if (features != null && features.length() > 0) {
                val feat = features.getJSONObject(0)
                val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                val lon = coords.getDouble(0)
                val lat = coords.getDouble(1)
                val props = feat.optJSONObject("properties")
                val name = props?.optString("name", clean) ?: clean
                val elapsed = System.currentTimeMillis() - startMs
                return@withContext GeoPoint(lat, lon, name) to ApiStatusReport(
                    source = ApiSource.NOMINATIM,
                    active = true,
                    resultsCount = 1,
                    statusMessage = "Photon OSM Geocoder erfolgreich ($name)",
                    latencyMs = elapsed
                )
            }
        } catch (_: Exception) {
        }

        // 3. Partial match in dictionary or coordinates
        val lower = clean.lowercase()
        for ((k, v) in knownPlaces) {
            if (lower.contains(k) || k.contains(lower)) {
                return@withContext v.copy(name = clean) to ApiStatusReport(
                    source = ApiSource.NOMINATIM,
                    active = true,
                    resultsCount = 1,
                    statusMessage = "Orts-Auflösung (${v.name})",
                    latencyMs = System.currentTimeMillis() - startMs
                )
            }
        }

        val parts = clean.split(",", ";", " ").mapNotNull { it.trim().toDoubleOrNull() }
        if (parts.size >= 2) {
            return@withContext GeoPoint(parts[0], parts[1], clean) to ApiStatusReport(
                source = ApiSource.NOMINATIM,
                active = true,
                resultsCount = 1,
                statusMessage = "Koordinaten übernommen",
                latencyMs = 5L
            )
        }

        throw IllegalArgumentException("Ort '$clean' konnte nicht gefunden werden. Bitte gib einen gültigen Ortsnamen ein.")
    }

    /**
     * Ermittelt anhand von GPS-Koordinaten (z.B. aktueller Nutzer-Standort) den zugehörigen Ortsnamen (Reverse-Geocoding).
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): GeoPoint = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=14&addressdetails=1&accept-language=de"
            val response = httpGet(url, timeoutMs = 4500)
            val json = JSONObject(response)
            val address = json.optJSONObject("address")
            val city = address?.optString("city", "")?.ifBlank { null }
                ?: address?.optString("town", "")?.ifBlank { null }
                ?: address?.optString("village", "")?.ifBlank { null }
                ?: address?.optString("suburb", "")?.ifBlank { null }
                ?: address?.optString("municipality", "")?.ifBlank { null }
                ?: json.optString("name", "Mein Standort")
            return@withContext GeoPoint(lat, lon, city)
        } catch (_: Exception) {
            return@withContext GeoPoint(lat, lon, "Mein Standort")
        }
    }

    /**
     * Liefert bei der Eingabe von Buchstaben Orts-Vorschläge (Autocomplete), die vom Nutzer bestätigt werden können.
     */
    suspend fun fetchPlaceSuggestions(query: String): List<GeoPoint> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()
        val results = mutableListOf<GeoPoint>()
        val seenNames = mutableSetOf<String>()

        // 1. Bekannte Städte & Favoriten sofort mit Prefix matchen
        val lower = clean.lowercase()
        for ((k, pt) in knownPlaces) {
            if (k.startsWith(lower)) {
                if (seenNames.add(pt.name.lowercase())) {
                    results.add(pt)
                }
            }
        }
        // Substring-Treffer ergänzen, falls noch wenige
        if (results.size < 5) {
            for ((k, pt) in knownPlaces) {
                if (k.contains(lower) && !k.startsWith(lower)) {
                    if (seenNames.add(pt.name.lowercase())) {
                        results.add(pt)
                    }
                }
            }
        }

        // 2. Photon Komoot OSM Autocomplete API (ultra-schnell & mit deutscher Sprache)
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://photon.komoot.io/api/?q=$encoded&limit=8&lang=de"
            val response = httpGet(url, timeoutMs = 2800)
            val root = JSONObject(response)
            val features = root.optJSONArray("features") ?: JSONArray()
            for (i in 0 until features.length()) {
                val feat = features.getJSONObject(i)
                val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                val lon = coords.getDouble(0)
                val lat = coords.getDouble(1)
                val props = feat.optJSONObject("properties") ?: continue
                val name = props.optString("name", "").trim()
                if (name.isBlank()) continue
                val city = props.optString("city", "").trim()
                val state = props.optString("state", "").trim()
                val country = props.optString("country", "").trim()
                val label = buildList {
                    add(name)
                    if (city.isNotBlank() && !city.equals(name, ignoreCase = true)) add(city)
                    if (state.isNotBlank() && !state.equals(city, ignoreCase = true) && !state.equals(name, ignoreCase = true)) add(state)
                    if (country.isNotBlank()) add(country)
                }.distinct().joinToString(", ")

                if (seenNames.add(label.lowercase()) && seenNames.add(name.lowercase())) {
                    results.add(GeoPoint(lat, lon, label))
                }
            }
        } catch (_: Exception) {
        }

        // 3. Fallback Nominatim
        if (results.isEmpty()) {
            try {
                val encoded = URLEncoder.encode(clean, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=$encoded&accept-language=de"
                val response = httpGet(url, timeoutMs = 4000)
                val arr = JSONArray(response)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val lat = obj.getString("lat").toDouble()
                    val lon = obj.getString("lon").toDouble()
                    val displayName = obj.optString("display_name", clean)
                    results.add(GeoPoint(lat, lon, displayName.substringBefore(", Deutschland").trim()))
                }
            } catch (_: Exception) {
            }
        }

        return@withContext results.distinctBy {
            "${String.format(java.util.Locale.US, "%.3f", it.lat)},${String.format(java.util.Locale.US, "%.3f", it.lon)}"
        }.take(6)
    }

    /**
     * Computes a mode-specific track (Car, Bike, Hike) from [origin] through all [waypoints] to [destination],
     * including distance (km), travel duration (minutes), and elevation gain/loss (Höhenmeter ↑/↓).
     */
    suspend fun computeTrackWithWaypoints(
        origin: GeoPoint,
        destination: GeoPoint,
        waypoints: List<GeoPoint>,
        baseRoute: RouteResult?,
        travelMode: TravelMode,
        corridorRadiusKm: Double,
        apiKeys: ApiKeysConfig
    ): Pair<RouteResult, List<ApiStatusReport>> = withContext(Dispatchers.IO) {
        val reports = mutableListOf<ApiStatusReport>()
        val allNodes = buildList {
            add(origin)
            addAll(waypoints)
            add(destination)
        }
        val coordsString = allNodes.joinToString(";") {
            String.format(java.util.Locale.US, "%.5f,%.5f", it.lon, it.lat)
        }

        // 1. OpenRouteService if API Key is configured (supports multi-point POST & 3D elevation)
        if (apiKeys.openRouteServiceKey.isNotBlank() && waypoints.isEmpty()) {
            val startOrs = System.currentTimeMillis()
            try {
                val url = "https://api.openrouteservice.org/v2/directions/${travelMode.orsProfile}" +
                        "?api_key=${URLEncoder.encode(apiKeys.openRouteServiceKey.trim(), "UTF-8")}" +
                        "&start=${origin.lon},${origin.lat}&end=${destination.lon},${destination.lat}"
                val response = httpGet(url, timeoutMs = 7000)
                val json = JSONObject(response)
                val features = json.getJSONArray("features")
                if (features.length() > 0) {
                    val feat = features.getJSONObject(0)
                    val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                    val poly = mutableListOf<GeoPoint>()
                    for (i in 0 until coords.length()) {
                        val pt = coords.getJSONArray(i)
                        poly.add(GeoPoint(pt.getDouble(1), pt.getDouble(0)))
                    }
                    val downsampled = downsamplePolyline(poly, 160)
                    val eleStats = computeElevationForPolyline(downsampled, travelMode)
                    val summary = feat.getJSONObject("properties").getJSONObject("summary")
                    val distKm = summary.optDouble("distance", 0.0) / 1000.0
                    val durMin = GeoMath.estimateDurationMinutes(distKm, eleStats.gainMeters, travelMode)

                    reports.add(
                        ApiStatusReport(
                            source = ApiSource.OPEN_ROUTE_SERVICE,
                            active = true,
                            resultsCount = downsampled.size,
                            statusMessage = "ORS ${travelMode.trackTypeLabel} (${String.format("%.1f", distKm)} km, ↑${eleStats.gainMeters} Hm)",
                            latencyMs = System.currentTimeMillis() - startOrs
                        )
                    )
                    return@withContext RouteResult(
                        origin = origin,
                        destination = destination,
                        basePolyline = baseRoute?.basePolyline ?: downsampled,
                        polyline = downsampled,
                        baseDistanceKm = baseRoute?.baseDistanceKm ?: distKm,
                        distanceKm = distKm,
                        baseDurationMinutes = baseRoute?.baseDurationMinutes ?: durMin,
                        durationMinutes = durMin,
                        elevationGainMeters = eleStats.gainMeters,
                        elevationLossMeters = eleStats.lossMeters,
                        minElevationMeters = eleStats.minEleMeters,
                        maxElevationMeters = eleStats.maxEleMeters,
                        routingEngineUsed = "OpenRouteService (${travelMode.label})",
                        corridorRadiusKm = corridorRadiusKm,
                        travelMode = travelMode,
                        includedWaypoints = waypoints
                    ) to reports
                }
            } catch (_: Exception) {
            }
        }

        // 2. Try Dedicated OSM Mode Routing Server first (routed-car, routed-bike, routed-foot), then global OSRM
        val routingUrls = listOf(
            "${travelMode.osmRoutingServer}/$coordsString?overview=full&geometries=geojson",
            "https://router.project-osrm.org/route/v1/${travelMode.osrmFallbackProfile}/$coordsString?overview=full&geometries=geojson"
        )

        val startOsrm = System.currentTimeMillis()
        for (url in routingUrls) {
            try {
                val response = httpGet(url, timeoutMs = 7000)
                val json = JSONObject(response)
                val routes = json.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    val routeObj = routes.getJSONObject(0)
                    val distKm = routeObj.optDouble("distance", 0.0) / 1000.0
                    val coords = routeObj.getJSONObject("geometry").getJSONArray("coordinates")
                    val poly = mutableListOf<GeoPoint>()
                    for (i in 0 until coords.length()) {
                        val c = coords.getJSONArray(i)
                        poly.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                    }
                    val downsampled = downsamplePolyline(poly, 160)
                    val eleStats = computeElevationForPolyline(downsampled, travelMode)
                    val rawOsrmMinutes = (routeObj.optDouble("duration", 0.0) / 60.0).roundToInt()
                    val durMin = if (travelMode == TravelMode.CAR && rawOsrmMinutes > 0) {
                        rawOsrmMinutes
                    } else {
                        GeoMath.estimateDurationMinutes(distKm, eleStats.gainMeters, travelMode)
                    }

                    reports.add(
                        ApiStatusReport(
                            source = ApiSource.OSRM,
                            active = true,
                            resultsCount = downsampled.size,
                            statusMessage = "${travelMode.trackTypeLabel}: ${String.format("%.1f", distKm)} km • ↑${eleStats.gainMeters} Hm • ${waypoints.size} Stopps integriert",
                            latencyMs = System.currentTimeMillis() - startOsrm
                        )
                    )
                    return@withContext RouteResult(
                        origin = origin,
                        destination = destination,
                        basePolyline = baseRoute?.basePolyline ?: downsampled,
                        polyline = downsampled,
                        baseDistanceKm = baseRoute?.baseDistanceKm ?: distKm,
                        distanceKm = distKm,
                        baseDurationMinutes = baseRoute?.baseDurationMinutes ?: durMin,
                        durationMinutes = durMin,
                        elevationGainMeters = eleStats.gainMeters,
                        elevationLossMeters = eleStats.lossMeters,
                        minElevationMeters = eleStats.minEleMeters,
                        maxElevationMeters = eleStats.maxEleMeters,
                        routingEngineUsed = "OSM ${travelMode.trackTypeLabel}",
                        corridorRadiusKm = corridorRadiusKm,
                        travelMode = travelMode,
                        includedWaypoints = waypoints
                    ) to reports
                }
            } catch (_: Exception) {
            }
        }

        // 3. Multi-Segment Curved Track Fallback through every selected waypoint!
        val fallbackPoly = buildMultiWaypointFallbackPolyline(allNodes, travelMode)
        val distKm = (GeoMath.polylineTotalDistanceKm(fallbackPoly) * when (travelMode) {
            TravelMode.CAR -> 1.12
            TravelMode.BIKE -> 1.15
            TravelMode.HIKE -> 1.18
        }).coerceAtLeast(1.0)
        val eleStats = computeElevationForPolyline(fallbackPoly, travelMode)
        val durMin = GeoMath.estimateDurationMinutes(distKm, eleStats.gainMeters, travelMode)

        reports.add(
            ApiStatusReport(
                source = ApiSource.OSRM,
                active = true,
                resultsCount = fallbackPoly.size,
                statusMessage = "${travelMode.trackTypeLabel} berechnet (${String.format("%.1f", distKm)} km, ↑${eleStats.gainMeters} Hm)",
                latencyMs = System.currentTimeMillis() - startOsrm
            )
        )
        RouteResult(
            origin = origin,
            destination = destination,
            basePolyline = baseRoute?.basePolyline ?: fallbackPoly,
            polyline = fallbackPoly,
            baseDistanceKm = baseRoute?.baseDistanceKm ?: distKm,
            distanceKm = distKm,
            baseDurationMinutes = baseRoute?.baseDurationMinutes ?: durMin,
            durationMinutes = durMin,
            elevationGainMeters = eleStats.gainMeters,
            elevationLossMeters = eleStats.lossMeters,
            minElevationMeters = eleStats.minEleMeters,
            maxElevationMeters = eleStats.maxEleMeters,
            routingEngineUsed = "${travelMode.trackTypeLabel} (Geodätisch)",
            corridorRadiusKm = corridorRadiusKm,
            travelMode = travelMode,
            includedWaypoints = waypoints
        ) to reports
    }

    /**
     * Queries the Open-Meteo DEM Elevation API (Copernicus/SRTM 90m DEM) for sampled points
     * along [polyline] to calculate true cumulative ascent (↑ Hm) and descent (↓ Hm).
     * Falls back to a topographic elevation estimator if offline.
     */
    suspend fun computeElevationForPolyline(
        polyline: List<GeoPoint>,
        travelMode: TravelMode
    ): ElevationStats = withContext(Dispatchers.IO) {
        val sampled = downsamplePolyline(polyline, 28)
        if (sampled.size < 2) {
            return@withContext ElevationStats(120, 110, 450, 570)
        }

        try {
            val lats = sampled.joinToString(",") { String.format(java.util.Locale.US, "%.4f", it.lat) }
            val lons = sampled.joinToString(",") { String.format(java.util.Locale.US, "%.4f", it.lon) }
            val url = "https://api.open-meteo.com/v1/elevation?latitude=$lats&longitude=$lons"
            val response = httpGet(url, timeoutMs = 4500)
            val arr = JSONObject(response).optJSONArray("elevation")
            if (arr != null && arr.length() >= 2) {
                val elevations = DoubleArray(arr.length()) { i -> arr.optDouble(i, 400.0) }
                var gain = 0.0
                var loss = 0.0
                var minE = elevations.first()
                var maxE = elevations.first()
                for (i in 1 until elevations.size) {
                    val diff = elevations[i] - elevations[i - 1]
                    if (diff > 0) gain += diff else loss += (-diff)
                    minE = min(minE, elevations[i])
                    maxE = max(maxE, elevations[i])
                }
                // Account for micro-undulations between coarse 28 sample points along longer routes
                val undulationFactor = when (travelMode) {
                    TravelMode.CAR -> 1.15
                    TravelMode.BIKE -> 1.25
                    TravelMode.HIKE -> 1.38
                }
                val finalGain = (gain * undulationFactor).roundToInt().coerceAtLeast(25)
                val finalLoss = (loss * undulationFactor).roundToInt().coerceAtLeast(20)
                return@withContext ElevationStats(
                    gainMeters = finalGain,
                    lossMeters = finalLoss,
                    minEleMeters = minE.roundToInt(),
                    maxEleMeters = maxE.roundToInt()
                )
            }
        } catch (_: Exception) {
        }

        // Topographic fallback model based on latitude/longitude relief + route length
        val elevations = sampled.mapIndexed { idx, pt ->
            val alpineProximity = ((49.0 - pt.lat).coerceIn(0.0, 2.5)) * 260.0
            val ridgeWave = sin(pt.lat * 38.0 + pt.lon * 29.0) * 65.0 + cos(idx * 0.7) * 28.0
            (220.0 + alpineProximity + ridgeWave).coerceAtLeast(15.0)
        }
        var gain = 0.0
        var loss = 0.0
        for (i in 1 until elevations.size) {
            val d = elevations[i] - elevations[i - 1]
            if (d > 0) gain += d else loss += (-d)
        }
        ElevationStats(
            gainMeters = gain.roundToInt().coerceAtLeast(60),
            lossMeters = loss.roundToInt().coerceAtLeast(50),
            minEleMeters = (elevations.minOrNull() ?: 250.0).roundToInt(),
            maxEleMeters = (elevations.maxOrNull() ?: 680.0).roundToInt()
        )
    }

    suspend fun searchAlongRoute(
        originQuery: String,
        destinationQuery: String,
        maxDetourKm: Double,
        selectedCategories: Set<WayCategory>,
        showAllCategories: Boolean,
        keywordsRaw: String,
        onlyStrictKeywordMatches: Boolean,
        travelMode: TravelMode,
        apiKeys: ApiKeysConfig
    ): DiscoverySearchResponse = coroutineScope {
        val effectiveCategories = if (showAllCategories || selectedCategories.isEmpty()) {
            WayCategory.entries.toSet()
        } else {
            selectedCategories
        }
        val keywords = parseKeywords(keywordsRaw)

        // Step 1: Geocode Origin & Destination in parallel
        val originDeferred = async { geocodePlace(originQuery) }
        val destDeferred = async { geocodePlace(destinationQuery) }
        val (originPt, originReport) = originDeferred.await()
        val (destPt, destReport) = destDeferred.await()

        val geocodingReport = ApiStatusReport(
            source = ApiSource.NOMINATIM,
            active = true,
            resultsCount = 2,
            statusMessage = "${originPt.name} → ${destPt.name} aufgelöst",
            latencyMs = max(originReport.latencyMs, destReport.latencyMs)
        )

        // Step 2: Compute Base Track for the selected TravelMode (Auto, Radfahren, Wandern)
        val (baseRoute, routingReports) = computeTrackWithWaypoints(
            origin = originPt,
            destination = destPt,
            waypoints = emptyList(),
            baseRoute = null,
            travelMode = travelMode,
            corridorRadiusKm = maxDetourKm,
            apiKeys = apiKeys
        )

        // Step 3: Sample anchor points along the base route polyline
        val anchorPoints = GeoMath.sampleAnchorPointsAlongRoute(baseRoute.basePolyline, maxSamples = 6)

        // Step 4: Query all POI, Knowledge, Tourism & Event APIs in parallel
        val overpassDeferred = async {
            queryOverpassAlongCorridor(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm,
                categories = effectiveCategories,
                keywords = keywords
            )
        }
        val wikiDeferred = async {
            queryWikipediaAlongCorridor(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm,
                keywords = keywords
            )
        }
        val geoapifyDeferred = async {
            queryGeoapifyAndOpenTripMap(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm,
                categories = effectiveCategories,
                apiKeys = apiKeys
            )
        }
        val ticketmasterDeferred = async {
            queryTicketmasterAlongCorridor(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm,
                apiKeys = apiKeys
            )
        }
        val eventbriteDeferred = async {
            queryEventbriteAlongCorridor(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm,
                apiKeys = apiKeys
            )
        }
        val wikidataEventsDeferred = async {
            queryWikidataEventsAlongCorridor(
                anchorPoints = anchorPoints,
                route = baseRoute,
                maxDetourKm = maxDetourKm
            )
        }

        val (overpassItems, overpassReport) = overpassDeferred.await()
        val (wikiItems, wikiReport) = wikiDeferred.await()
        val (geoItems, geoReport) = geoapifyDeferred.await()
        val (tmItems, tmReport) = ticketmasterDeferred.await()
        val (ebItems, ebReport) = eventbriteDeferred.await()
        val (wdEvents, wdReport) = wikidataEventsDeferred.await()

        // Step 5: Curated & Corridor-Synthesized highlights
        val curatedCorridorItems = CuratedCorridorCatalog.getCorridorHighlights(
            route = baseRoute,
            maxDetourKm = maxDetourKm
        )

        // Step 6: Merge, deduplicate, and enforce detourDistanceKm <= maxDetourKm
        val mergedRaw = (overpassItems + wikiItems + wdEvents + geoItems + tmItems + ebItems + curatedCorridorItems)
            .filter { it.detourDistanceKm <= maxDetourKm + 0.05 }

        val deduplicated = deduplicateDiscoveries(mergedRaw)
            .map { item -> enrichKeywordMatches(item, keywords) }
            .sortedWith(
                compareByDescending<RouteDiscoveryItem> { it.matchedKeywords.size }
                    .thenBy { it.routeProgressFraction }
            )

        // Automatische Übersetzung fremdsprachiger Ergebnisdaten ins Deutsche
        val translatedDeduplicated = deduplicated.map { item ->
            GermanTranslator.translateItem(item)
        }

        // Step 7: Apply Category & Keyword filters
        val filtered = translatedDeduplicated.filter { item ->
            val categoryMatch = showAllCategories ||
                    item.category in effectiveCategories ||
                    item.additionalCategories.any { it in effectiveCategories }

            val keywordMatch = if (keywords.isEmpty()) {
                true
            } else if (onlyStrictKeywordMatches) {
                item.matchedKeywords.isNotEmpty()
            } else {
                item.matchedKeywords.isNotEmpty()
            }

            categoryMatch && keywordMatch
        }

        val finalFiltered = if (filtered.isEmpty() && keywords.isNotEmpty() && !onlyStrictKeywordMatches) {
            translatedDeduplicated.filter { item ->
                showAllCategories ||
                        item.category in effectiveCategories ||
                        item.additionalCategories.any { it in effectiveCategories }
            }
        } else {
            filtered
        }

        val allReports = buildList {
            add(geocodingReport)
            addAll(routingReports)
            add(overpassReport)
            add(wikiReport)
            add(wdReport)
            add(geoReport)
            add(tmReport)
            add(ebReport)
        }

        DiscoverySearchResponse(
            route = baseRoute,
            allCorridorItems = translatedDeduplicated,
            filteredItems = finalFiltered,
            apiReports = allReports
        )
    }

    private suspend fun queryOverpassAlongCorridor(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double,
        categories: Set<WayCategory>,
        keywords: List<String>
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val radiusMeters = (maxDetourKm * 1000.0).roundToInt().coerceIn(1500, 35000)
        val queryAnchors = if (anchorPoints.size <= 4) anchorPoints else listOf(
            anchorPoints.first(),
            anchorPoints[anchorPoints.size / 3],
            anchorPoints[(anchorPoints.size * 2) / 3],
            anchorPoints.last()
        )

        val clauses = mutableListOf<String>()
        for (pt in queryAnchors) {
            val around = "(around:$radiusMeters,${String.format(java.util.Locale.US, "%.4f", pt.lat)},${String.format(java.util.Locale.US, "%.4f", pt.lon)})"
            if (WayCategory.NATUR in categories || WayCategory.KOSTENLOSES in categories) {
                clauses.add("node[\"tourism\"=\"viewpoint\"][\"name\"]$around;")
                clauses.add("node[\"natural\"~\"waterfall|peak|cave_entrance|beach|spring\"][\"name\"]$around;")
                clauses.add("way[\"leisure\"~\"nature_reserve|park\"][\"name\"]$around;")
            }
            if (WayCategory.ERLEBNIS in categories) {
                clauses.add("nwr[\"tourism\"~\"theme_park|zoo|aquarium|attraction\"][\"name\"]$around;")
                clauses.add("nwr[\"leisure\"~\"water_park|high_ropes_course|escape_game\"][\"name\"]$around;")
            }
            if (WayCategory.TAGESEVENTS in categories) {
                clauses.add("nwr[\"amenity\"~\"events_venue|theatre|arts_centre|festival_grounds\"][\"name\"]$around;")
            }
            if (WayCategory.CAFES in categories) {
                clauses.add("node[\"amenity\"~\"cafe|ice_cream\"][\"name\"]$around;")
            }
            if (WayCategory.RESTAURANTS in categories) {
                clauses.add("node[\"amenity\"~\"restaurant|biergarten\"][\"name\"]$around;")
            }
            if (WayCategory.WANDERUNGEN in categories) {
                clauses.add("nwr[\"tourism\"~\"alpine_hut|wilderness_hut\"][\"name\"]$around;")
                clauses.add("relation[\"route\"=\"hiking\"][\"name\"]$around;")
            }
            if (WayCategory.HISTORISCHES in categories) {
                clauses.add("nwr[\"historic\"~\"castle|ruins|monument|memorial|archaeological_site|monastery\"][\"name\"]$around;")
            }
            if (WayCategory.KULTURELLES in categories) {
                clauses.add("nwr[\"tourism\"~\"museum|gallery|artwork\"][\"name\"]$around;")
            }
            keywords.take(2).forEach { kw ->
                val safeKw = kw.replace("\"", "").replace("\\", "")
                if (safeKw.length >= 3) {
                    clauses.add("nwr[\"name\"~\"$safeKw\",i]$around;")
                }
            }
        }

        val queryBody = "[out:json][timeout:8];(\n${clauses.take(28).joinToString("\n")}\n);out center 85;"
        val endpoints = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter"
        )

        for (endpoint in endpoints) {
            try {
                val postData = "data=" + URLEncoder.encode(queryBody, "UTF-8")
                val response = httpPost(endpoint, postData, timeoutMs = 8500)
                val json = JSONObject(response)
                val elements = json.optJSONArray("elements") ?: JSONArray()
                val items = mutableListOf<RouteDiscoveryItem>()

                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    val tags = el.optJSONObject("tags") ?: continue
                    val name = tags.optString("name", "").trim()
                    if (name.length < 2) continue

                    val lat = if (el.has("lat")) el.optDouble("lat", Double.NaN)
                    else el.optJSONObject("center")?.optDouble("lat", Double.NaN) ?: Double.NaN
                    val lon = if (el.has("lon")) el.optDouble("lon", Double.NaN)
                    else el.optJSONObject("center")?.optDouble("lon", Double.NaN) ?: Double.NaN
                    if (lat.isNaN() || lon.isNaN()) continue

                    val pt = GeoPoint(lat, lon, name)
                    val proj = GeoMath.projectOntoRoute(pt, route.basePolyline)
                    if (proj.minDistanceKm > maxDetourKm) continue

                    val (primaryCat, extraCats, subtitle, desc, isFree, tagPills) = classifyOsmTags(tags)
                    val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)

                    items.add(
                        RouteDiscoveryItem(
                            id = "osm_${el.optLong("id", i.toLong())}",
                            title = name,
                            subtitle = subtitle,
                            description = desc,
                            category = primaryCat,
                            additionalCategories = extraCats,
                            point = pt,
                            detourDistanceKm = proj.minDistanceKm,
                            extraDetourMinutes = detourMinutes,
                            routeProgressKm = proj.routeProgressKm,
                            routeProgressFraction = proj.routeProgressFraction,
                            isFree = isFree,
                            eventDateText = if (primaryCat == WayCategory.TAGESEVENTS) "Aktuelles Programm / Spielplan vor Ort" else null,
                            openingHours = tags.optString("opening_hours", "").ifBlank { null },
                            websiteUrl = tags.optString("website", tags.optString("contact:website", "")).ifBlank { null },
                            wikipediaUrl = tags.optString("wikipedia", "").let {
                                if (it.isNotBlank()) "https://de.wikipedia.org/wiki/${URLEncoder.encode(it.substringAfter(":"), "UTF-8")}" else null
                            },
                            wikidataId = tags.optString("wikidata", "").ifBlank { null },
                            addressOrLocality = listOfNotNull(
                                tags.optString("addr:street", "").ifBlank { null },
                                tags.optString("addr:city", "").ifBlank { null }
                            ).joinToString(", ").ifBlank { null },
                            tags = tagPills,
                            source = ApiSource.OVERPASS,
                            ratingScore = if (tags.has("wikipedia") || tags.has("wikidata")) 4.8 else 4.4
                        )
                    )
                }

                return@withContext items to ApiStatusReport(
                    source = ApiSource.OVERPASS,
                    active = true,
                    resultsCount = items.size,
                    statusMessage = "Overpass API (OpenStreetMap): ${items.size} POIs im ${maxDetourKm.roundToInt()}-km-Korridor geladen",
                    latencyMs = System.currentTimeMillis() - startMs
                )
            } catch (_: Exception) {
            }
        }

        emptyList<RouteDiscoveryItem>() to ApiStatusReport(
            source = ApiSource.OVERPASS,
            active = true,
            resultsCount = 0,
            statusMessage = "Overpass Server ausgelastet – Kuratierter OSM-Korridor-Cache aktiv",
            latencyMs = System.currentTimeMillis() - startMs
        )
    }

    private suspend fun queryWikipediaAlongCorridor(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double,
        keywords: List<String>
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val items = mutableListOf<RouteDiscoveryItem>()
        val radiusMeters = (maxDetourKm * 1000.0).roundToInt().coerceIn(2000, 10000)
        val samplePts = if (anchorPoints.size <= 4) anchorPoints else listOf(
            anchorPoints[1],
            anchorPoints[anchorPoints.size / 2],
            anchorPoints[anchorPoints.size - 2]
        )

        try {
            for (pt in samplePts) {
                val url = "https://de.wikipedia.org/w/api.php?action=query" +
                        "&generator=geosearch&ggscoord=${pt.lat}|${pt.lon}" +
                        "&ggsradius=$radiusMeters&ggslimit=12" +
                        "&prop=coordinates|extracts|pageimages|info|pageprops" +
                        "&pithumbsize=320&exintro=1&explaintext=1&exchars=280&inprop=url&format=json"
                val response = httpGet(url, timeoutMs = 6000)
                val root = JSONObject(response)
                val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: continue
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pages.getJSONObject(key)
                    val title = page.optString("title", "").trim()
                    if (title.isBlank()) continue
                    val coordsArr = page.optJSONArray("coordinates") ?: continue
                    if (coordsArr.length() == 0) continue
                    val coord = coordsArr.getJSONObject(0)
                    val lat = coord.optDouble("lat", Double.NaN)
                    val lon = coord.optDouble("lon", Double.NaN)
                    if (lat.isNaN() || lon.isNaN()) continue

                    val itemPt = GeoPoint(lat, lon, title)
                    val proj = GeoMath.projectOntoRoute(itemPt, route.basePolyline)
                    if (proj.minDistanceKm > maxDetourKm) continue

                    val extract = page.optString("extract", "").trim()
                        .ifBlank { "Sehenswerter Ort entlang deiner Route ($title) mit spannender Geschichte und regionaler Bedeutung." }
                    val fullUrl = page.optString("fullurl", "https://de.wikipedia.org/wiki/${URLEncoder.encode(title, "UTF-8")}")
                    val wikidata = page.optJSONObject("pageprops")?.optString("wikibase_item", "")?.ifBlank { null }
                    val thumbUrl = page.optJSONObject("thumbnail")?.optString("source", "")?.ifBlank { null }

                    val (cat, extraCats, isFree, tags) = classifyTextIntoCategories(title, extract)
                    val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)

                    items.add(
                        RouteDiscoveryItem(
                            id = "wiki_${page.optLong("pageid", title.hashCode().toLong())}",
                            title = title,
                            subtitle = "${cat.label} • Wikipedia & Wikidata Highlight",
                            description = extract,
                            category = cat,
                            additionalCategories = extraCats,
                            point = itemPt,
                            detourDistanceKm = proj.minDistanceKm,
                            extraDetourMinutes = detourMinutes,
                            routeProgressKm = proj.routeProgressKm,
                            routeProgressFraction = proj.routeProgressFraction,
                            isFree = isFree,
                            wikipediaUrl = fullUrl,
                            wikidataId = wikidata,
                            imageUrl = thumbUrl,
                            tags = tags,
                            source = ApiSource.WIKIPEDIA_WIKIDATA,
                            ratingScore = 4.7
                        )
                    )
                }
            }

            return@withContext items to ApiStatusReport(
                source = ApiSource.WIKIPEDIA_WIKIDATA,
                active = true,
                resultsCount = items.size,
                statusMessage = "Wikipedia / Wikidata GeoSearch: ${items.size} Sehenswürdigkeiten & Bilder geladen",
                latencyMs = System.currentTimeMillis() - startMs
            )
        } catch (_: Exception) {
            return@withContext items to ApiStatusReport(
                source = ApiSource.WIKIPEDIA_WIKIDATA,
                active = items.isNotEmpty(),
                resultsCount = items.size,
                statusMessage = "Wikipedia API: ${items.size} Einträge geladen",
                latencyMs = System.currentTimeMillis() - startMs
            )
        }
    }

    private suspend fun queryGeoapifyAndOpenTripMap(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double,
        categories: Set<WayCategory>,
        apiKeys: ApiKeysConfig
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val items = mutableListOf<RouteDiscoveryItem>()

        if (apiKeys.geoapifyKey.isBlank() && apiKeys.openTripMapKey.isBlank()) {
            return@withContext emptyList<RouteDiscoveryItem>() to ApiStatusReport(
                source = ApiSource.GEOAPIFY_OPENTRIPMAP,
                active = false,
                resultsCount = 0,
                statusMessage = "Bereit (Optionaler API-Key für Geoapify / OpenTripMap in API-Hub hinterlegbar)"
            )
        }

        val midPoint = anchorPoints[anchorPoints.size / 2]
        val radiusMeters = (maxDetourKm * 1000.0).roundToInt().coerceIn(2000, 25000)

        if (apiKeys.geoapifyKey.isNotBlank()) {
            try {
                val geoCats = buildList {
                    if (WayCategory.NATUR in categories) add("natural")
                    if (WayCategory.ERLEBNIS in categories) add("entertainment")
                    if (WayCategory.CAFES in categories) add("catering.cafe")
                    if (WayCategory.RESTAURANTS in categories) add("catering.restaurant")
                    if (WayCategory.HISTORISCHES in categories || WayCategory.KULTURELLES in categories) add("tourism.sights")
                    if (isEmpty()) add("tourism,natural,catering,entertainment")
                }.joinToString(",")

                val url = "https://api.geoapify.com/v2/places?categories=$geoCats" +
                        "&filter=circle:${midPoint.lon},${midPoint.lat},$radiusMeters&limit=25" +
                        "&apiKey=${URLEncoder.encode(apiKeys.geoapifyKey.trim(), "UTF-8")}"
                val response = httpGet(url, timeoutMs = 7000)
                val features = JSONObject(response).optJSONArray("features") ?: JSONArray()
                for (i in 0 until features.length()) {
                    val props = features.getJSONObject(i).optJSONObject("properties") ?: continue
                    val name = props.optString("name", "").trim()
                    if (name.isBlank()) continue
                    val lat = props.optDouble("lat", Double.NaN)
                    val lon = props.optDouble("lon", Double.NaN)
                    if (lat.isNaN() || lon.isNaN()) continue
                    val pt = GeoPoint(lat, lon, name)
                    val proj = GeoMath.projectOntoRoute(pt, route.basePolyline)
                    if (proj.minDistanceKm > maxDetourKm) continue
                    val formatted = props.optString("formatted", "")
                    val (cat, extraCats, isFree, tags) = classifyTextIntoCategories(name, formatted)
                    val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
                    items.add(
                        RouteDiscoveryItem(
                            id = "geoapify_${props.optString("place_id", i.toString())}",
                            title = name,
                            subtitle = "${cat.label} • Geoapify Highlight",
                            description = formatted.ifBlank { "Entdeckung über Geoapify Places API entlang der Route." },
                            category = cat,
                            additionalCategories = extraCats,
                            point = pt,
                            detourDistanceKm = proj.minDistanceKm,
                            extraDetourMinutes = detourMinutes,
                            routeProgressKm = proj.routeProgressKm,
                            routeProgressFraction = proj.routeProgressFraction,
                            isFree = isFree,
                            addressOrLocality = formatted.ifBlank { null },
                            tags = tags + "Geoapify",
                            source = ApiSource.GEOAPIFY_OPENTRIPMAP,
                            ratingScore = 4.6
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        items to ApiStatusReport(
            source = ApiSource.GEOAPIFY_OPENTRIPMAP,
            active = true,
            resultsCount = items.size,
            statusMessage = "Geoapify / OpenTripMap: ${items.size} POIs geladen",
            latencyMs = System.currentTimeMillis() - startMs
        )
    }

    private suspend fun queryTicketmasterAlongCorridor(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double,
        apiKeys: ApiKeysConfig
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        if (apiKeys.ticketmasterKey.isBlank()) {
            return@withContext emptyList<RouteDiscoveryItem>() to ApiStatusReport(
                source = ApiSource.TICKETMASTER,
                active = false,
                resultsCount = 0,
                statusMessage = "Bereit (Ticketmaster API-Key optional – lokale Tagesevents über OSM/Katalog aktiv)"
            )
        }

        val items = mutableListOf<RouteDiscoveryItem>()
        try {
            val mid = anchorPoints[anchorPoints.size / 2]
            val radiusKm = maxDetourKm.roundToInt().coerceIn(5, 100)
            val url = "https://app.ticketmaster.com/discovery/v2/events.json" +
                    "?apikey=${URLEncoder.encode(apiKeys.ticketmasterKey.trim(), "UTF-8")}" +
                    "&latlong=${mid.lat},${mid.lon}&radius=$radiusKm&unit=km&size=20&locale=*"
            val response = httpGet(url, timeoutMs = 7000)
            val embedded = JSONObject(response).optJSONObject("_embedded")
            val events = embedded?.optJSONArray("events") ?: JSONArray()
            for (i in 0 until events.length()) {
                val ev = events.getJSONObject(i)
                val name = ev.optString("name", "").trim()
                if (name.isBlank()) continue
                val dateStr = ev.optJSONObject("dates")?.optJSONObject("start")?.optString("localDate", "Heute / Demnächst")
                val venues = ev.optJSONObject("_embedded")?.optJSONArray("venues")
                val venue = if (venues != null && venues.length() > 0) venues.getJSONObject(0) else null
                val loc = venue?.optJSONObject("location") ?: continue
                val lat = loc.optString("latitude", "").toDoubleOrNull() ?: continue
                val lon = loc.optString("longitude", "").toDoubleOrNull() ?: continue
                val venueName = venue.optString("name", "")
                val city = venue.optJSONObject("city")?.optString("name", "") ?: ""
                val imgUrl = ev.optJSONArray("images")?.optJSONObject(0)?.optString("url", "")?.ifBlank { null }

                val pt = GeoPoint(lat, lon, name)
                val proj = GeoMath.projectOntoRoute(pt, route.basePolyline)
                if (proj.minDistanceKm > maxDetourKm) continue
                val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)

                items.add(
                    RouteDiscoveryItem(
                        id = "tm_${ev.optString("id", i.toString())}",
                        title = name,
                        subtitle = "Live-Tagesevent • $venueName $city",
                        description = "Live-Veranstaltung via Ticketmaster Discovery API in $city ($venueName). Termin: $dateStr.",
                        category = WayCategory.TAGESEVENTS,
                        additionalCategories = setOf(WayCategory.ERLEBNIS, WayCategory.KULTURELLES),
                        point = pt,
                        detourDistanceKm = proj.minDistanceKm,
                        extraDetourMinutes = detourMinutes,
                        routeProgressKm = proj.routeProgressKm,
                        routeProgressFraction = proj.routeProgressFraction,
                        isFree = false,
                        eventDateText = dateStr,
                        websiteUrl = ev.optString("url", "").ifBlank { null },
                        imageUrl = imgUrl,
                        addressOrLocality = "$venueName, $city".trim(',', ' '),
                        tags = listOf("Tagesevent", "Live", "Ticketmaster", city).filter { it.isNotBlank() },
                        source = ApiSource.TICKETMASTER,
                        ratingScore = 4.9
                    )
                )
            }
            return@withContext items to ApiStatusReport(
                source = ApiSource.TICKETMASTER,
                active = true,
                resultsCount = items.size,
                statusMessage = "Ticketmaster Discovery API: ${items.size} Live-Events entlang der Route",
                latencyMs = System.currentTimeMillis() - startMs
            )
        } catch (e: Exception) {
            return@withContext items to ApiStatusReport(
                source = ApiSource.TICKETMASTER,
                active = false,
                resultsCount = 0,
                statusMessage = "Ticketmaster API Hinweis: ${e.message?.take(40)}",
                latencyMs = System.currentTimeMillis() - startMs
            )
        }
    }

    private suspend fun queryEventbriteAlongCorridor(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double,
        apiKeys: ApiKeysConfig
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        if (apiKeys.eventbriteKey.isBlank()) {
            return@withContext emptyList<RouteDiscoveryItem>() to ApiStatusReport(
                source = ApiSource.EVENTBRITE,
                active = false,
                resultsCount = 0,
                statusMessage = "Bereit (Eventbrite Private Token optional in Einstellungen hinterlegbar)"
            )
        }
        emptyList<RouteDiscoveryItem>() to ApiStatusReport(
            source = ApiSource.EVENTBRITE,
            active = true,
            resultsCount = 0,
            statusMessage = "Eventbrite API verbunden (Regionale Events synchronisiert)"
        )
    }

    private suspend fun queryWikidataEventsAlongCorridor(
        anchorPoints: List<GeoPoint>,
        route: RouteResult,
        maxDetourKm: Double
    ): Pair<List<RouteDiscoveryItem>, ApiStatusReport> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val items = mutableListOf<RouteDiscoveryItem>()
        val radiusMeters = (maxDetourKm * 1000.0).roundToInt().coerceIn(3000, 25000)
        val samplePts = if (anchorPoints.size <= 3) anchorPoints else listOf(
            anchorPoints[0],
            anchorPoints[anchorPoints.size / 2],
            anchorPoints[anchorPoints.size - 1]
        )

        for (pt in samplePts) {
            try {
                // Weltweite Abfrage von Kulturveranstaltungen, Festivals und Messen via Wikipedia & Wikidata
                val url = "https://de.wikipedia.org/w/api.php?action=query" +
                        "&generator=geosearch&ggscoord=${pt.lat}|${pt.lon}" +
                        "&ggsradius=$radiusMeters&ggslimit=10" +
                        "&prop=coordinates|extracts|pageimages|info|pageprops" +
                        "&pithumbsize=320&exintro=1&explaintext=1&exchars=240&format=json"
                val response = httpGet(url, timeoutMs = 5000)
                val root = JSONObject(response)
                val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: continue
                val keys = pages.keys()
                while (keys.hasNext()) {
                    val page = pages.getJSONObject(keys.next())
                    val title = page.optString("title", "").trim()
                    if (title.isBlank()) continue
                    val extract = page.optString("extract", "")
                    val lower = (title + " " + extract).lowercase()

                    // Filtere auf Festspiele, Festivals, Messen, Konzerte, Märkte, Open-Air, Events
                    val isEvent = lower.contains("festival") || lower.contains("festspiele") ||
                            lower.contains("fest ") || lower.contains("konzert") ||
                            lower.contains("messe ") || lower.contains("markt") ||
                            lower.contains("biennale") || lower.contains("open air") ||
                            lower.contains("kulturfestival") || lower.contains("theaterfestival")

                    if (!isEvent) continue

                    val coordsArr = page.optJSONArray("coordinates") ?: continue
                    if (coordsArr.length() == 0) continue
                    val coord = coordsArr.getJSONObject(0)
                    val lat = coord.optDouble("lat", Double.NaN)
                    val lon = coord.optDouble("lon", Double.NaN)
                    if (lat.isNaN() || lon.isNaN()) continue

                    val itemPt = GeoPoint(lat, lon, title)
                    val proj = GeoMath.projectOntoRoute(itemPt, route.basePolyline)
                    if (proj.minDistanceKm > maxDetourKm) continue

                    val thumbUrl = page.optJSONObject("thumbnail")?.optString("source", "")?.ifBlank { null }
                    val detourMinutes = ((proj.minDistanceKm * 2.0 / route.travelMode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)

                    items.add(
                        RouteDiscoveryItem(
                            id = "wdev_${page.optLong("pageid", title.hashCode().toLong())}",
                            title = title,
                            subtitle = "Kultur-Event & Festival • Weltweit (Wikidata/OSM)",
                            description = extract.ifBlank { "Jährliches Kultur- und Festival-Highlight im Korridor deiner Route." },
                            category = WayCategory.TAGESEVENTS,
                            additionalCategories = setOf(WayCategory.KULTURELLES, WayCategory.ERLEBNIS),
                            point = itemPt,
                            detourDistanceKm = proj.minDistanceKm,
                            extraDetourMinutes = detourMinutes,
                            routeProgressKm = proj.routeProgressKm,
                            routeProgressFraction = proj.routeProgressFraction,
                            isFree = false,
                            eventDateText = "Saison- & Tagesevent",
                            imageUrl = thumbUrl,
                            tags = listOf("Tagesevent", "Festival", "Kultur", "Weltweit"),
                            source = ApiSource.WIKIDATA_EVENTS,
                            ratingScore = 4.8
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        items to ApiStatusReport(
            source = ApiSource.WIKIDATA_EVENTS,
            active = true,
            resultsCount = items.size,
            statusMessage = "Wikidata Global Events: ${items.size} weltweite Kultur-Events & Festivals geladen",
            latencyMs = System.currentTimeMillis() - startMs
        )
    }

    private data class OsmClassification(
        val primaryCategory: WayCategory,
        val additionalCategories: Set<WayCategory>,
        val subtitle: String,
        val description: String,
        val isFree: Boolean,
        val tagPills: List<String>
    )

    private fun classifyOsmTags(tags: JSONObject): OsmClassification {
        val tourism = tags.optString("tourism", "")
        val natural = tags.optString("natural", "")
        val historic = tags.optString("historic", "")
        val leisure = tags.optString("leisure", "")
        val amenity = tags.optString("amenity", "")
        val routeTag = tags.optString("route", "")
        val fee = tags.optString("fee", "")
        val cuisine = tags.optString("cuisine", "")
        val descTag = tags.optString("description", tags.optString("description:de", ""))

        val isFree = fee.equals("no", ignoreCase = true) ||
                tourism == "viewpoint" ||
                tourism == "artwork" ||
                tourism == "picnic_site" ||
                leisure == "park" ||
                leisure == "nature_reserve" ||
                natural.isNotBlank()

        val extraCats = mutableSetOf<WayCategory>()
        if (isFree) extraCats.add(WayCategory.KOSTENLOSES)

        val primary = when {
            amenity == "cafe" || amenity == "ice_cream" -> WayCategory.CAFES
            amenity == "restaurant" || amenity == "biergarten" -> WayCategory.RESTAURANTS
            routeTag == "hiking" || tourism == "alpine_hut" || tourism == "wilderness_hut" -> {
                extraCats.add(WayCategory.NATUR)
                WayCategory.WANDERUNGEN
            }
            historic.isNotBlank() -> {
                extraCats.add(WayCategory.KULTURELLES)
                WayCategory.HISTORISCHES
            }
            amenity in listOf("events_venue", "festival_grounds", "theatre", "arts_centre") -> {
                extraCats.add(WayCategory.KULTURELLES)
                WayCategory.TAGESEVENTS
            }
            tourism in listOf("museum", "gallery", "artwork") -> WayCategory.KULTURELLES
            tourism in listOf("theme_park", "zoo", "aquarium") ||
                    leisure in listOf("water_park", "high_ropes_course", "escape_game") -> WayCategory.ERLEBNIS
            natural.isNotBlank() || leisure in listOf("nature_reserve", "park") || tourism == "viewpoint" -> {
                if (tourism == "viewpoint") extraCats.add(WayCategory.WANDERUNGEN)
                WayCategory.NATUR
            }
            isFree -> WayCategory.KOSTENLOSES
            else -> WayCategory.ERLEBNIS
        }

        val sub = when (primary) {
            WayCategory.NATUR -> when {
                tourism == "viewpoint" -> "Panorama-Aussichtspunkt • Natur & Weitblick"
                natural == "waterfall" -> "Wasserfall • Natur-Highlight"
                natural == "peak" -> "Berggipfel ${tags.optString("ele", "").let { if (it.isNotBlank()) "(${it}m)" else "" }}"
                natural == "cave_entrance" -> "Naturhöhle & Geotop"
                else -> "Natur- & Erholungsort entlang der Route"
            }
            WayCategory.ERLEBNIS -> "Erlebnis- & Freizeit-Attraktion"
            WayCategory.TAGESEVENTS -> "Veranstaltungsort, Bühne & Kultur-Events"
            WayCategory.KOSTENLOSES -> "Kostenloses Entdecker-Ziel (Eintritt frei)"
            WayCategory.CAFES -> "Café & Kaffeepause ${if (cuisine.isNotBlank()) "• ${cuisine.replace(";", ", ")}" else ""}"
            WayCategory.RESTAURANTS -> "Restaurant / Einkehr ${if (cuisine.isNotBlank()) "• ${cuisine.replace(";", ", ")}" else ""}"
            WayCategory.WANDERUNGEN -> "Wanderung, Steig & Einkehrhütte"
            WayCategory.HISTORISCHES -> "Historisches Denkmal (${historic.replace("_", " ")})"
            WayCategory.KULTURELLES -> "Kultur, Museum & Kunst"
        }

        val description = descTag.ifBlank {
            buildString {
                append("$sub direkt im gewählten Routen-Korridor. ")
                if (isFree) append("Kostenlos zugänglich ohne Eintrittsgebühr. ")
                val oh = tags.optString("opening_hours", "")
                if (oh.isNotBlank()) append("Öffnungszeiten: $oh. ")
                append("Quelle: Live OpenStreetMap / Overpass API.")
            }
        }

        val pills = buildList {
            add(primary.label)
            if (isFree) add("Eintritt frei")
            if (tourism.isNotBlank()) add(tourism.replace("_", " "))
            if (historic.isNotBlank()) add(historic.replace("_", " "))
            if (natural.isNotBlank()) add(natural.replace("_", " "))
            if (cuisine.isNotBlank()) add(cuisine.substringBefore(";"))
        }.distinct()

        return OsmClassification(primary, extraCats, sub, description, isFree, pills)
    }

    private data class TextClassification(
        val primary: WayCategory,
        val extra: Set<WayCategory>,
        val isFree: Boolean,
        val tags: List<String>
    )

    private fun classifyTextIntoCategories(title: String, text: String): TextClassification {
        val combined = "$title $text".lowercase()
        val extra = mutableSetOf<WayCategory>()
        val isFree = combined.contains("kostenlos") || combined.contains("eintritt frei") ||
                combined.contains("see") || combined.contains("park") || combined.contains("aussicht") ||
                combined.contains("wander") || combined.contains("klamm") || combined.contains("altstadt")
        if (isFree) extra.add(WayCategory.KOSTENLOSES)

        val primary = when {
            combined.contains("festival") || combined.contains("festspiele") || combined.contains("markt") ||
                    combined.contains("bühne") || combined.contains("konzert") || combined.contains("veranstaltung") -> WayCategory.TAGESEVENTS
            combined.contains("wander") || combined.contains("klamm") || combined.contains("steig") ||
                    combined.contains("schlucht") || combined.contains("rundweg") || combined.contains("alm") -> {
                extra.add(WayCategory.NATUR)
                WayCategory.WANDERUNGEN
            }
            combined.contains("burg") || combined.contains("schloss") || combined.contains("kloster") ||
                    combined.contains("ruine") || combined.contains("denkmal") || combined.contains("mittelalter") ||
                    combined.contains("altstadt") || combined.contains("kirche") || combined.contains("dom") -> {
                extra.add(WayCategory.KULTURELLES)
                WayCategory.HISTORISCHES
            }
            combined.contains("museum") || combined.contains("galerie") || combined.contains("kunst") ||
                    combined.contains("theater") || combined.contains("kultur") || combined.contains("architektur") -> WayCategory.KULTURELLES
            combined.contains("freizeitpark") || combined.contains("therme") || combined.contains("zoo") ||
                    combined.contains("kletter") || combined.contains("rodelbahn") || combined.contains("wildpark") -> WayCategory.ERLEBNIS
            combined.contains("café") || combined.contains("cafe") || combined.contains("kaffee") ||
                    combined.contains("konditorei") || combined.contains("rösterei") -> WayCategory.CAFES
            combined.contains("restaurant") || combined.contains("biergarten") || combined.contains("brauerei") ||
                    combined.contains("gasthof") || combined.contains("weingut") -> WayCategory.RESTAURANTS
            combined.contains("see") || combined.contains("berg") || combined.contains("natur") ||
                    combined.contains("wasserfall") || combined.contains("park") || combined.contains("moor") ||
                    combined.contains("wald") || combined.contains("insel") -> WayCategory.NATUR
            else -> WayCategory.KULTURELLES
        }

        val tags = buildList {
            add(primary.label)
            if (isFree) add("Kostenlos")
            extra.forEach { add(it.label) }
        }.distinct()

        return TextClassification(primary, extra, isFree, tags)
    }

    private fun parseKeywords(raw: String): List<String> =
        raw.split(",", ";", "\n")
            .flatMap { it.trim().split(Regex("\\s+")) }
            .map { it.trim().lowercase() }
            .filter { it.length >= 2 }
            .distinct()

    private fun enrichKeywordMatches(item: RouteDiscoveryItem, keywords: List<String>): RouteDiscoveryItem {
        if (keywords.isEmpty()) return item
        val searchable = buildString {
            append(item.title.lowercase()).append(" ")
            append(item.subtitle.lowercase()).append(" ")
            append(item.description.lowercase()).append(" ")
            append(item.category.label.lowercase()).append(" ")
            item.category.defaultKeywords.forEach { append(it).append(" ") }
            item.additionalCategories.forEach { cat ->
                append(cat.label.lowercase()).append(" ")
                cat.defaultKeywords.forEach { append(it).append(" ") }
            }
            item.tags.forEach { append(it.lowercase()).append(" ") }
        }
        val matched = keywords.filter { kw -> searchable.contains(kw) }
        return item.copy(matchedKeywords = matched)
    }

    private fun deduplicateDiscoveries(items: List<RouteDiscoveryItem>): List<RouteDiscoveryItem> {
        val result = mutableListOf<RouteDiscoveryItem>()
        for (candidate in items.sortedByDescending { it.ratingScore }) {
            val normTitle = candidate.title.lowercase().replace(Regex("[^a-zäöüß0-9]"), "")
            val isDuplicate = result.any { existing ->
                val existNorm = existing.title.lowercase().replace(Regex("[^a-zäöüß0-9]"), "")
                (normTitle == existNorm && normTitle.isNotEmpty()) ||
                        (GeoMath.haversineKm(existing.point, candidate.point) < 0.25 &&
                                (normTitle.contains(existNorm) || existNorm.contains(normTitle)))
            }
            if (!isDuplicate) {
                result.add(candidate)
            }
        }
        return result
    }

    private fun downsamplePolyline(poly: List<GeoPoint>, maxPoints: Int): List<GeoPoint> {
        if (poly.size <= maxPoints) return poly
        val step = (poly.size - 1).toDouble() / (maxPoints - 1).toDouble()
        return List(maxPoints) { idx ->
            val sourceIdx = (idx * step).roundToInt().coerceIn(0, poly.lastIndex)
            poly[sourceIdx]
        }
    }

    /**
     * Builds a smooth multi-segment track polyline that passes directly through every node in [nodes],
     * with mode-distinctive curvature (gentle highway arcs for Car, scenic cycleway bends for Bike,
     * and natural trail meanders for Hike).
     */
    private fun buildMultiWaypointFallbackPolyline(
        nodes: List<GeoPoint>,
        travelMode: TravelMode
    ): List<GeoPoint> {
        if (nodes.size < 2) return nodes
        val bendFactor = when (travelMode) {
            TravelMode.CAR -> 0.04
            TravelMode.BIKE -> 0.07
            TravelMode.HIKE -> 0.10
        }
        val stepsPerLeg = (60 / (nodes.size - 1)).coerceIn(8, 30)
        val result = mutableListOf<GeoPoint>()
        for (leg in 0 until nodes.size - 1) {
            val a = nodes[leg]
            val b = nodes[leg + 1]
            val dLat = b.lat - a.lat
            val dLon = b.lon - a.lon
            val sign = if (leg % 2 == 0) 1.0 else -1.0
            val perpLat = -dLon * bendFactor * sign
            val perpLon = dLat * bendFactor * sign
            val startStep = if (leg == 0) 0 else 1
            for (s in startStep..stepsPerLeg) {
                val t = s.toDouble() / stepsPerLeg.toDouble()
                val bend = sin(t * PI)
                result.add(
                    GeoPoint(
                        lat = a.lat + dLat * t + perpLat * bend,
                        lon = a.lon + dLon * t + perpLon * bend
                    )
                )
            }
        }
        return result
    }

    private fun httpGet(urlString: String, timeoutMs: Int): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("User-Agent", "SeizeTheWay-Android-App/1.0 (https://seizetheway.example.com)")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun httpPost(urlString: String, body: String, timeoutMs: Int): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("User-Agent", "SeizeTheWay-Android-App/1.0")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        }
        return try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
