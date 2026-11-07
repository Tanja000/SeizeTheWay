package com.example.seizetheway.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seizetheway.model.AppLanguage
import com.example.seizetheway.model.TravelMode
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class TravelFact(val de: String, val en: String)

private val TRAVEL_FACTS = listOf(
    TravelFact(
        "Der Rennsteig im Thüringer Wald ist mit rund 170 km einer der ältesten Höhenwanderwege Deutschlands.",
        "The Rennsteig in the Thuringian Forest, at about 170 km, is one of Germany's oldest ridge hiking trails."
    ),
    TravelFact(
        "Wanderschuhe sollte man vor einer langen Tour einlaufen – Blasen sind der häufigste Grund für Tourabbrüche.",
        "Break in your hiking boots before a long trip – blisters are the most common reason for cutting tours short."
    ),
    TravelFact(
        "Beim Radfahren verbrennt man pro Stunde etwa 400–600 kcal – ein Stück Kuchen im Café ist also völlig legitim.",
        "Cycling burns roughly 400–600 kcal per hour – a slice of cake at a café is perfectly justified."
    ),
    TravelFact(
        "Der Europäische Fernwanderweg E1 führt vom Nordkap bis nach Sizilien – über 7.000 Kilometer.",
        "The European long-distance path E1 runs from the North Cape to Sicily – over 7,000 kilometres."
    ),
    TravelFact(
        "Die Zugspitze (2.962 m) ist Deutschlands höchster Berg. Auf Radtour erreichst du sie aber nur bis zur Talstation.",
        "The Zugspitze (2,962 m) is Germany's highest mountain – by bike you only get as far as the valley station."
    ),
    TravelFact(
        "Pausen alle 60–90 Minuten halten Konzentration und Laune auf langen Autofahrten hoch.",
        "A break every 60–90 minutes keeps concentration and mood up on long car journeys."
    ),
    TravelFact(
        "Das EuroVelo-Netz umfasst 17 Fernradrouten mit zusammen über 90.000 Kilometern quer durch Europa.",
        "The EuroVelo network consists of 17 long-distance cycle routes totalling over 90,000 kilometres across Europe."
    ),
    TravelFact(
        "Pro 100 Höhenmeter Aufstieg rechnet man beim Wandern etwa 10–15 Minuten zusätzlich.",
        "When hiking, add roughly 10–15 minutes for every 100 metres of ascent."
    ),
    TravelFact(
        "Entlang deiner Route gleichen wir gerade Hunderte Punkte aus OpenStreetMap, Wikipedia und Event-Portalen ab.",
        "Right now we are matching hundreds of places along your route from OpenStreetMap, Wikipedia and event portals."
    )
)

/**
 * Ladekarte während der Routen- und Ortsberechnung:
 * Zeigt den Live-Fortschritt der Quellen (OSM, Wikipedia, Events), informiert den Nutzer,
 * dass weitere Ergebnisse noch nachgeladen werden, und bietet während der Wartezeit
 * eine interaktive Tour-Packliste sowie wechselnde Reise- & Routenfakten (ohne Ratespiel).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchLoadingCard(
    appLanguage: AppLanguage,
    travelMode: TravelMode,
    maxDetourKm: Float,
    stageText: String?,
    fraction: Float,
    foundCount: Int,
    routeKm: Double?,
    doneSources: Int = 0,
    totalSources: Int = 7,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isDe = appLanguage.isGerman
    val isLongRoute = (routeKm ?: 0.0) >= 120.0
    var showFun by remember { mutableStateOf(!compact) }

    var factIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            factIndex = (factIndex + 1) % TRAVEL_FACTS.size
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF006B5E).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp,
                    color = Color(0xFF006B5E)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (foundCount > 0) {
                            if (isDe) "Erste Ergebnisse da – weitere laden noch nach…"
                            else "First results ready – loading more…"
                        } else {
                            if (isDe) "Deine Route & Korridor-Orte werden berechnet…"
                            else "Calculating your route & corridor places…"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = stageText ?: if (isDe) {
                            "${travelMode.localizedTrackType(AppLanguage.DE)} & Korridor (±${maxDetourKm.roundToInt()} km)"
                        } else {
                            "${travelMode.localizedTrackType(AppLanguage.EN)} & corridor (±${maxDetourKm.roundToInt()} km)"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF006B5E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0.06f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF006B5E),
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(Modifier.height(8.dp))
            // Live-Quellen-Pills (Track, OSM, Wikipedia, Events)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val trackReady = routeKm != null
                val osmReady = doneSources >= 2
                val wikiReady = doneSources >= 3
                val eventsReady = doneSources >= 6
                SourceProgressPill(label = if (isDe) "Track" else "Track", done = trackReady)
                SourceProgressPill(label = "OpenStreetMap", done = osmReady)
                SourceProgressPill(label = "Wikipedia", done = wikiReady)
                SourceProgressPill(label = "Events", done = eventsReady)
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isDe) "$doneSources/$totalSources Quellen" else "$doneSources/$totalSources sources",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (foundCount > 0) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFE6F6F3),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF006B5E).copy(alpha = 0.25f))
                ) {
                    Text(
                        text = if (isDe) {
                            "✓ Schon $foundCount Treffer gefunden und direkt unterhalb sichtbar! Weitere Orte entlang der Route (≤ ${maxDetourKm.roundToInt()} km Abweichung) werden kontinuierlich nachgeladen…"
                        } else {
                            "✓ Already $foundCount results found and shown right below! More places along your route (≤ ${maxDetourKm.roundToInt()} km detour) are continuously being added…"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF006B5E),
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = if (isLongRoute) Color(0xFFFFF7ED) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isDe) {
                            if (isLongRoute) {
                                "Lange Strecke (${routeKm!!.roundToInt()} km): Wir prüfen gerade über OpenStreetMap, Wikipedia und Event-Portale alle Orte mit max. ${maxDetourKm.roundToInt()} km Abweichung. Sobald der erste Treffer da ist, siehst du ihn sofort!"
                            } else {
                                "Wir gleichen gerade OpenStreetMap, Wikipedia und Events entlang deiner Route ab (max. ${maxDetourKm.roundToInt()} km Abweichung). Das erste Ergebnis erscheint sofort, der Rest wird laufend ergänzt."
                            }
                        } else {
                            if (isLongRoute) {
                                "Long route (${routeKm!!.roundToInt()} km): Checking OpenStreetMap, Wikipedia and event portals for places within ${maxDetourKm.roundToInt()} km detour. The first result will appear right away!"
                            } else {
                                "Checking OpenStreetMap, Wikipedia and events along your route (max. ${maxDetourKm.roundToInt()} km detour). The first result appears immediately, more follow continuously."
                            }
                        },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDe) "🎒 Tour-Check & Wissenswertes" else "🎒 Tour Check & Travel Facts",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                TextButton(onClick = { showFun = !showFun }) {
                    Text(
                        text = if (showFun) {
                            if (isDe) "Einklappen ▲" else "Collapse ▲"
                        } else {
                            if (isDe) "Ausklappen ▼" else "Expand ▼"
                        },
                        color = Color(0xFF006B5E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (showFun) {
                PackingChecklistBlock(isDe = isDe, travelMode = travelMode)

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDe) "💡 Wusstest du schon?" else "💡 Did you know?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (isDe) "Nächster Fakt →" else "Next fact →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006B5E),
                        modifier = Modifier.clickable {
                            factIndex = (factIndex + 1) % TRAVEL_FACTS.size
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Crossfade(targetState = factIndex, label = "fact") { idx ->
                    Text(
                        text = TRAVEL_FACTS[idx].let { if (isDe) it.de else it.en },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.heightIn(min = 38.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceProgressPill(label: String, done: Boolean) {
    Surface(
        color = if (done) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = (if (done) "✓ " else "⏳ ") + label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (done) Color(0xFF065F46) else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PackingChecklistBlock(isDe: Boolean, travelMode: TravelMode) {
    val items = when (travelMode) {
        TravelMode.CAR -> if (isDe) {
            listOf("Getränke", "Snacks", "Powerbank", "Sonnenbrille", "Picknickdecke", "Playlist")
        } else {
            listOf("Drinks", "Snacks", "Power bank", "Sunglasses", "Picnic blanket", "Playlist")
        }
        TravelMode.BIKE -> if (isDe) {
            listOf("Helm", "Wasser", "Ersatzschlauch", "Regenjacke", "Sonnencreme", "Powerbank")
        } else {
            listOf("Helmet", "Water", "Spare tube", "Rain jacket", "Sunscreen", "Power bank")
        }
        TravelMode.HIKE -> if (isDe) {
            listOf("Wanderschuhe", "Wasser", "Regenjacke", "Snacks", "Pflaster", "Karte offline")
        } else {
            listOf("Hiking boots", "Water", "Rain jacket", "Snacks", "Plasters", "Offline map")
        }
    }
    var checked by remember(travelMode) { mutableStateOf(setOf<String>()) }

    Text(
        text = (if (isDe) "🎒 Packliste – tippe zum Abhaken" else "🎒 Packing list – tap to tick off") +
                "  (${checked.size}/${items.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color(0xFF0F172A)
    )
    Spacer(Modifier.height(6.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            val on = item in checked
            Surface(
                color = if (on) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { checked = if (on) checked - item else checked + item }
            ) {
                Text(
                    text = (if (on) "✓ " else "") + item,
                    fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
    if (checked.size == items.size) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isDe) "🎉 Alles gepackt – du bist startklar!" else "🎉 All packed – you're ready to go!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006B5E)
        )
    }
}
