package com.example.seizetheway.ui.main

import com.example.seizetheway.model.GeoMath
import com.example.seizetheway.model.GeoPoint
import com.example.seizetheway.model.TravelMode
import com.example.seizetheway.model.WayCategory
import com.example.seizetheway.model.presetsForTravelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {
    @Test
    fun geoMath_projectsPointOntoRouteAccurately() {
        val munich = GeoPoint(48.137154, 11.576124, "München")
        val garmisch = GeoPoint(47.492088, 11.095763, "Garmisch-Partenkirchen")
        val starnberg = GeoPoint(47.9965, 11.3408, "Starnberg")

        val proj = GeoMath.projectOntoRoute(starnberg, listOf(munich, garmisch))
        assertTrue("Starnberg should be within 12 km of Munich-Garmisch line", proj.minDistanceKm < 12.0)
        assertTrue("Progress fraction should be between 0.1 and 0.5", proj.routeProgressFraction in 0.1f..0.5f)
    }

    @Test
    fun travelModes_haveDedicatedPresetsAndDurationCalculation() {
        TravelMode.entries.forEach { mode ->
            val presets = presetsForTravelMode(mode)
            assertTrue("Mode ${mode.label} should have dedicated track presets", presets.isNotEmpty())
        }
        val carMin = GeoMath.estimateDurationMinutes(50.0, 400, TravelMode.CAR)
        val bikeMin = GeoMath.estimateDurationMinutes(50.0, 400, TravelMode.BIKE)
        val hikeMin = GeoMath.estimateDurationMinutes(50.0, 400, TravelMode.HIKE)
        assertTrue(carMin < bikeMin && bikeMin < hikeMin)
    }

    @Test
    fun categories_containAllRequestedCoreCategories() {
        val labels = WayCategory.entries.map { it.label }
        assertTrue(labels.contains("Natur"))
        assertTrue(labels.contains("Erlebnis"))
        assertTrue(labels.contains("Tagesevents"))
        assertTrue(labels.contains("Kostenloses"))
        assertTrue(labels.contains("Cafés"))
        assertTrue(labels.contains("Restaurants"))
        assertTrue(labels.contains("Wanderungen"))
        assertTrue(labels.contains("Historisches"))
        assertTrue(labels.contains("Kulturelles"))
        assertEquals(9, labels.size)
    }
}
