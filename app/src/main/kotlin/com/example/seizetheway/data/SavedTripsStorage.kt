package com.example.seizetheway.data

import android.content.Context
import com.example.seizetheway.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistenter lokaler Speicher für gespeicherte Touren.
 * Speichert alle Touren als JSON-Datei im internen App-Speicher (filesDir),
 * sodass gespeicherte Touren auch nach App-Neustart, Schließen und Öffnen erhalten bleiben.
 */
object SavedTripsStorage {

    private const val FILE_NAME = "saved_trips_v1.json"
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    @Synchronized
    fun loadSavedTrips(context: Context? = null): List<SavedTrip> {
        val ctx = context?.applicationContext ?: appContext ?: return emptyList()
        val file = File(ctx.filesDir, FILE_NAME)
        if (!file.exists() || file.length() == 0L) {
            return emptyList()
        }

        return try {
            val jsonString = file.readText(Charsets.UTF_8)
            val jsonArray = JSONArray(jsonString)
            val trips = mutableListOf<SavedTrip>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                deserializeTrip(obj)?.let { trips.add(it) }
            }
            trips
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Synchronized
    fun saveSavedTrips(trips: List<SavedTrip>, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (trip in trips) {
                jsonArray.put(serializeTrip(trip))
            }
            val file = File(ctx.filesDir, FILE_NAME)
            val tempFile = File(ctx.filesDir, "$FILE_NAME.tmp")
            tempFile.writeText(jsonArray.toString(2), Charsets.UTF_8)
            if (tempFile.exists()) {
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun serializeTrip(trip: SavedTrip): JSONObject {
        val json = JSONObject()
        json.put("id", trip.id)
        json.put("title", trip.title)
        json.put("dateLabel", trip.dateLabel)
        json.put("origin", serializeGeoPoint(trip.origin))
        json.put("destination", serializeGeoPoint(trip.destination))
        json.put("trackDistanceKm", trip.trackDistanceKm)
        json.put("trackDurationMinutes", trip.trackDurationMinutes)
        json.put("elevationGainMeters", trip.elevationGainMeters)
        json.put("elevationLossMeters", trip.elevationLossMeters)
        json.put("maxDetourKm", trip.maxDetourKm.toDouble())
        json.put("travelMode", trip.travelMode.name)
        json.put("notes", trip.notes)

        // Stops
        val stopsArray = JSONArray()
        for (stop in trip.stops) {
            stopsArray.put(serializeStop(stop))
        }
        json.put("stops", stopsArray)

        // Polyline
        val polylineArray = JSONArray()
        for (point in trip.trackPolyline) {
            polylineArray.put(serializeGeoPoint(point))
        }
        json.put("trackPolyline", polylineArray)

        return json
    }

    private fun deserializeTrip(obj: JSONObject): SavedTrip? {
        return try {
            val id = obj.optString("id", "")
            if (id.isBlank()) return null
            val title = obj.optString("title", "Meine Tour")
            val dateLabel = obj.optString("dateLabel", "Geplante Tour")
            val origin = deserializeGeoPoint(obj.getJSONObject("origin")) ?: return null
            val destination = deserializeGeoPoint(obj.getJSONObject("destination")) ?: return null
            val trackDistanceKm = obj.optDouble("trackDistanceKm", 10.0)
            val trackDurationMinutes = obj.optInt("trackDurationMinutes", 30)
            val elevationGainMeters = obj.optInt("elevationGainMeters", 0)
            val elevationLossMeters = obj.optInt("elevationLossMeters", 0)
            val maxDetourKm = obj.optDouble("maxDetourKm", 20.0).toFloat()
            val travelModeStr = obj.optString("travelMode", TravelMode.CAR.name)
            val travelMode = try {
                TravelMode.valueOf(travelModeStr)
            } catch (_: Exception) {
                TravelMode.CAR
            }
            val notes = obj.optString("notes", "")

            // Stops
            val stops = mutableListOf<RouteDiscoveryItem>()
            val stopsArray = obj.optJSONArray("stops")
            if (stopsArray != null) {
                for (i in 0 until stopsArray.length()) {
                    val stopObj = stopsArray.optJSONObject(i) ?: continue
                    deserializeStop(stopObj)?.let { stops.add(it) }
                }
            }

            // Polyline
            val polyline = mutableListOf<GeoPoint>()
            val polylineArray = obj.optJSONArray("trackPolyline")
            if (polylineArray != null) {
                for (i in 0 until polylineArray.length()) {
                    val ptObj = polylineArray.optJSONObject(i) ?: continue
                    deserializeGeoPoint(ptObj)?.let { polyline.add(it) }
                }
            }

            SavedTrip(
                id = id,
                title = title,
                dateLabel = dateLabel,
                origin = origin,
                destination = destination,
                trackDistanceKm = trackDistanceKm,
                trackDurationMinutes = trackDurationMinutes,
                elevationGainMeters = elevationGainMeters,
                elevationLossMeters = elevationLossMeters,
                maxDetourKm = maxDetourKm,
                travelMode = travelMode,
                stops = stops,
                trackPolyline = polyline,
                notes = notes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun serializeGeoPoint(point: GeoPoint): JSONObject {
        val json = JSONObject()
        json.put("lat", point.lat)
        json.put("lon", point.lon)
        json.put("name", point.name)
        return json
    }

    private fun deserializeGeoPoint(obj: JSONObject): GeoPoint? {
        val lat = obj.optDouble("lat", Double.NaN)
        val lon = obj.optDouble("lon", Double.NaN)
        if (lat.isNaN() || lon.isNaN()) return null
        val name = obj.optString("name", "")
        return GeoPoint(lat = lat, lon = lon, name = name)
    }

    private fun serializeStop(item: RouteDiscoveryItem): JSONObject {
        val json = JSONObject()
        json.put("id", item.id)
        json.put("title", item.title)
        json.put("subtitle", item.subtitle)
        json.put("description", item.description)
        json.put("titleEn", item.titleEn)
        json.put("subtitleEn", item.subtitleEn)
        json.put("descriptionEn", item.descriptionEn)
        json.put("category", item.category.name)

        val addCats = JSONArray()
        for (cat in item.additionalCategories) {
            addCats.put(cat.name)
        }
        json.put("additionalCategories", addCats)

        json.put("point", serializeGeoPoint(item.point))
        json.put("detourDistanceKm", item.detourDistanceKm)
        json.put("extraDetourMinutes", item.extraDetourMinutes)
        json.put("routeProgressKm", item.routeProgressKm)
        json.put("routeProgressFraction", item.routeProgressFraction.toDouble())
        json.put("isFree", item.isFree)

        if (item.eventDateText != null) json.put("eventDateText", item.eventDateText)
        if (item.eventDateTextEn != null) json.put("eventDateTextEn", item.eventDateTextEn)
        if (item.openingHours != null) json.put("openingHours", item.openingHours)
        if (item.websiteUrl != null) json.put("websiteUrl", item.websiteUrl)
        if (item.wikipediaUrl != null) json.put("wikipediaUrl", item.wikipediaUrl)
        if (item.wikidataId != null) json.put("wikidataId", item.wikidataId)
        if (item.imageUrl != null) json.put("imageUrl", item.imageUrl)
        if (item.addressOrLocality != null) json.put("addressOrLocality", item.addressOrLocality)

        val tagsArray = JSONArray()
        for (tag in item.tags) tagsArray.put(tag)
        json.put("tags", tagsArray)

        json.put("source", item.source.name)
        json.put("ratingScore", item.ratingScore)

        val keywordsArray = JSONArray()
        for (kw in item.matchedKeywords) keywordsArray.put(kw)
        json.put("matchedKeywords", keywordsArray)

        return json
    }

    private fun deserializeStop(obj: JSONObject): RouteDiscoveryItem? {
        return try {
            val id = obj.optString("id", "")
            if (id.isBlank()) return null
            val title = obj.optString("title", "")
            val subtitle = obj.optString("subtitle", "")
            val description = obj.optString("description", "")
            val titleEn = obj.optString("titleEn", "")
            val subtitleEn = obj.optString("subtitleEn", "")
            val descriptionEn = obj.optString("descriptionEn", "")

            val categoryStr = obj.optString("category", WayCategory.NATUR.name)
            val category = try {
                WayCategory.valueOf(categoryStr)
            } catch (_: Exception) {
                WayCategory.NATUR
            }

            val additionalCategories = mutableSetOf<WayCategory>()
            val addCatsArray = obj.optJSONArray("additionalCategories")
            if (addCatsArray != null) {
                for (i in 0 until addCatsArray.length()) {
                    val catName = addCatsArray.optString(i, "")
                    try {
                        additionalCategories.add(WayCategory.valueOf(catName))
                    } catch (_: Exception) {}
                }
            }

            val pointObj = obj.optJSONObject("point") ?: return null
            val point = deserializeGeoPoint(pointObj) ?: return null

            val detourDistanceKm = obj.optDouble("detourDistanceKm", 0.0)
            val extraDetourMinutes = obj.optInt("extraDetourMinutes", 0)
            val routeProgressKm = obj.optDouble("routeProgressKm", 0.0)
            val routeProgressFraction = obj.optDouble("routeProgressFraction", 0.0).toFloat()
            val isFree = obj.optBoolean("isFree", false)

            val eventDateText = if (obj.has("eventDateText")) obj.optString("eventDateText") else null
            val eventDateTextEn = if (obj.has("eventDateTextEn")) obj.optString("eventDateTextEn") else null
            val openingHours = if (obj.has("openingHours")) obj.optString("openingHours") else null
            val websiteUrl = if (obj.has("websiteUrl")) obj.optString("websiteUrl") else null
            val wikipediaUrl = if (obj.has("wikipediaUrl")) obj.optString("wikipediaUrl") else null
            val wikidataId = if (obj.has("wikidataId")) obj.optString("wikidataId") else null
            val imageUrl = if (obj.has("imageUrl")) obj.optString("imageUrl") else null
            val addressOrLocality = if (obj.has("addressOrLocality")) obj.optString("addressOrLocality") else null

            val tags = mutableListOf<String>()
            val tagsArray = obj.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.optString(i, ""))
                }
            }

            val sourceStr = obj.optString("source", ApiSource.OSRM.name)
            val source = try {
                ApiSource.valueOf(sourceStr)
            } catch (_: Exception) {
                ApiSource.OSRM
            }

            val ratingScore = obj.optDouble("ratingScore", 4.6)

            val matchedKeywords = mutableListOf<String>()
            val keywordsArray = obj.optJSONArray("matchedKeywords")
            if (keywordsArray != null) {
                for (i in 0 until keywordsArray.length()) {
                    matchedKeywords.add(keywordsArray.optString(i, ""))
                }
            }

            RouteDiscoveryItem(
                id = id,
                title = title,
                subtitle = subtitle,
                description = description,
                titleEn = titleEn,
                subtitleEn = subtitleEn,
                descriptionEn = descriptionEn,
                category = category,
                additionalCategories = additionalCategories,
                point = point,
                detourDistanceKm = detourDistanceKm,
                extraDetourMinutes = extraDetourMinutes,
                routeProgressKm = routeProgressKm,
                routeProgressFraction = routeProgressFraction,
                isFree = isFree,
                eventDateText = eventDateText,
                eventDateTextEn = eventDateTextEn,
                openingHours = openingHours,
                websiteUrl = websiteUrl,
                wikipediaUrl = wikipediaUrl,
                wikidataId = wikidataId,
                imageUrl = imageUrl,
                addressOrLocality = addressOrLocality,
                tags = tags,
                source = source,
                ratingScore = ratingScore,
                matchedKeywords = matchedKeywords
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
