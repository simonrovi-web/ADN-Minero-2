package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MiningUiState
import com.example.viewmodel.MiningViewModel

@Composable
fun AlertasScreen(
    state: MiningUiState,
    viewModel: MiningViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isModoFaena = state.isModoFaena

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("alertas_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alerta Crítica Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AlertRedContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF690005)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ALERTA METEOROLÓGICA & HSE VIGENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Rachas de viento 45 km/h en Collahuasi y Radiación UV Nivel 11+ en Antofagasta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnAlertRedContainer
                    )
                }

                Button(
                    onClick = { viewModel.openSosDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "SOS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Red Sísmica Faenas CSN / USGS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = CopperBright, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RED SÍSMICA FAENAS CSN / USGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Text(
                        text = "NORMAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialEmerald
                    )
                }
                Text(
                    text = "Eventos superficiales en radio 100km de operaciones y tranques de relaves.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.seismicRecords.forEach { sismo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isModoFaena) Color.Black else SurfaceContainerLowest)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CopperDeep.copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${sismo.magnitude}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CopperBright
                                    )
                                }

                                Column {
                                    Text(
                                        text = sismo.location,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${sismo.district} · Prof ${sismo.depthKm} km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = sismo.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IndustrialEmerald
                                )
                                Text(
                                    text = sismo.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Condición Meteorológica de Faenas de Altura
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "METEOROLOGÍA FAENAS EN ALTURA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Text(
                        text = "ALTA CORDILLERA",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.weatherSites.forEach { site ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isModoFaena) Color.Black else SurfaceContainerLowest)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (site.isAlert) Icons.Default.Air else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (site.isAlert) TickerDown else CopperBright,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column {
                                    Text(
                                        text = site.faena,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${site.region} · ${site.altitudeM} msnm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${site.tempC} °C",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (site.isAlert) CopperBright else OnSurface
                                )
                                Text(
                                    text = site.weatherDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (site.isAlert) FontWeight.Bold else FontWeight.Normal,
                                    color = if (site.isAlert) TickerDown else IndustrialEmerald,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Protocolos de Seguridad Operacional
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LÍMITES OPERACIONALES CRÍTICOS (HSE)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CopperBright
                )

                val rules = listOf(
                    "Izaje y grúas: Paralización preventiva obligatoria a rachas sobre 45 km/h.",
                    "Sismicidad >= 4.0 ML: Inspección ocular inmediata de coronamientos de tranques y taludes.",
                    "Radiación UV Nivel 11+: Reaplicación obligatoria de bloqueador cada 2h y uso de legionario.",
                    "Isoterma 0°C sobre 4.200m: Activación de cuadrilla de monitoreo de quebradas por aluvión."
                )

                rules.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = CopperBright, fontWeight = FontWeight.Bold)
                        Text(text = rule, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
