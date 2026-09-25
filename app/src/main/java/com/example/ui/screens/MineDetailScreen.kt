package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.MineDetail
import com.example.ui.theme.*
import com.example.viewmodel.MiningUiState
import com.example.viewmodel.MiningViewModel

@Composable
fun MineDetailScreen(
    mine: MineDetail,
    state: MiningUiState,
    viewModel: MiningViewModel,
    modifier: Modifier = Modifier
) {
    val isModoFaena = state.isModoFaena
    val isFollowed = state.followedFaenas.contains(mine.id)
    var selectedDayByTouch by remember { mutableStateOf<String?>(null) }
    var exportStatusText by remember { mutableStateOf("EXPORTAR FICHA (PDF/XLS)") }

    // Intercept hardware/system back button
    BackHandler {
        viewModel.closeMineDetail()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("mine_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Identity Breadcrumb
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = { viewModel.closeMineDetail() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isModoFaena) Color(0xFF222222) else SurfaceContainerHigh)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Directorio",
                    tint = if (isModoFaena) FaenaYellow else OnSurface
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = mine.district,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(text = "/", color = OutlineColor, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = mine.basin,
                        style = MaterialTheme.typography.labelSmall,
                        color = CopperBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(text = "/", color = OutlineColor, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "ID: ${mine.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TelemetryCyan,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Mine Title & Subtitle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLowest
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mine.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isModoFaena) FaenaYellow else OnSurface
                        )
                        Text(
                            text = mine.company,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IndustrialEmerald)
                        )
                        Text(
                            text = "OPERACIÓN ACTIVA · 7X7",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald,
                            fontSize = 9.sp
                        )
                    }
                }

                // Geographical details
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = mine.coords,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${mine.altitudeMsnm} m.s.n.m. (Precordillera de Domeyko)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = TelemetryCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = mine.waterSource,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Action Deck Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleFollowFaena(mine.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("follow_mine_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowed) CopperPrimaryContainer else SurfaceContainerHigh,
                            contentColor = if (isFollowed) Color.White else OnSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFollowed) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isFollowed) Color.White else CopperBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFollowed) "FAENA GUARDADA" else "SEGUIR FAENA",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            exportStatusText = "GENERANDO REPORTE..."
                            exportStatusText = "DESCARGA INICIADA ✓"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("export_mine_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CopperPrimaryContainer,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = exportStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 6-Card Telemetry Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cu 2024
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PROYECCIÓN CU 2024",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = mine.cuProjectionYear,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = mine.cuFulfillmentPercent,
                            style = MaterialTheme.typography.bodySmall,
                            color = IndustrialEmerald,
                            fontSize = 10.sp
                        )
                    }
                }

                // Cash Cost C1
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "CASH COST NETO (C1)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = mine.cashCostC1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CopperBright
                        )
                        Text(
                            text = "▼ -0.06 vs Q3 Cochilco",
                            style = MaterialTheme.typography.bodySmall,
                            color = IndustrialEmerald,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Agua de Mar Directa
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "AGUA DE MAR DIRECTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = mine.seaWaterDirectPercent,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryCyan
                        )
                        Text(
                            text = mine.seaWaterDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Fuerza Laboral
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FUERZA LABORAL EN TURNO",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = mine.workforceShift,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = mine.workforceBreakdown,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accidentabilidad
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "TASA ACCIDENTABILIDAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = mine.accidentRate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald
                        )
                        Text(
                            text = mine.accidentStandard,
                            style = MaterialTheme.typography.bodySmall,
                            color = IndustrialEmerald,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Sismicidad 50km
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "RED SÍSMICA RADIO 50KM",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${mine.seismicCount7d} REG. 7D",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CopperBright
                        )
                        Text(
                            text = "CSN San Pedro / Calama",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Open Pit Satellite Image & Tactical Telemetry Overlay
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceContainerLowest
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CopperBright)
                        )
                        Text(
                            text = "DISTRITO GEORREFERENCIADO: RAJO ESPERANZA & RAJO ENCUENTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "WGS84",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                // Satellite Mine Image with fallback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh)
                ) {
                    // Load remote satellite picture or fallback local asset
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://lh3.googleusercontent.com/aida-public/AB6AXuBwUsrPPTmmiusBepEzEEJUndAXF-pvc-654I_yLEQ0miOsiEXDNfUNXl0zpOsBSnKuE2llNPOIfRKWUMkN5f66jPRX04BjajgaePu9myij21uMQZqcnwAnp9fask5IfHyZoqnMeGBSssIYcRmWGOUD_Y5bxHnLyiS7uo_ldz3tC2JWtyLKTnT8poKerixxq4Ve8XobdRN20FJkutFaIBYCLuyM464-6zNgtYibW8UUOFD48T8gE-kO")
                            .error(R.drawable.mine_open_pit_1790349240600)
                            .placeholder(R.drawable.mine_open_pit_1790349240600)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Vista satelital de Rajo Esperanza y Rajo Encuentro",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Tactical Overlay at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = mine.pitEsperanzaDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface,
                            fontSize = 10.sp
                        )
                        Text(
                            text = mine.pitEncuentroDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TelemetryCyan,
                            fontSize = 10.sp
                        )
                        Text(
                            text = mine.tailingsDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = IndustrialEmerald,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Flowsheet: Cadena de Conminución & Beneficio (5 Stages)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLowest
            ),
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
                    Column {
                        Text(
                            text = "DIAGRAMA DE FLUJO: CONMINUCIÓN & BENEFICIO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Sulfuros (Concentradora) & Óxidos (Planta ESDE)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SCADA SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CopperBright,
                            fontSize = 9.sp
                        )
                    }
                }

                // 5 Stage Cards
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mine.stages.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isModoFaena) Color.Black else SurfaceContainer)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CopperBright.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stage.stepNumber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CopperBright
                                    )
                                }

                                Column {
                                    Text(
                                        text = stage.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${stage.machinery} · ${stage.detail}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = stage.metricHighlight,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialEmerald,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Interactive Daily Throughput Bar Chart
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isModoFaena) Color.Black else SurfaceContainer)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TONELAJE TRATADO (KTON/DÍA) - 7 DÍAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "PROMEDIO: ${mine.averageThroughput}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald,
                            fontSize = 10.sp
                        )
                    }

                    // Bar graph columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        mine.throughputList.forEach { dt ->
                            val heightFraction = (dt.throughputKt / 120.0).toFloat().coerceIn(0.2f, 1f)
                            val isSelected = selectedDayByTouch == dt.day

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDayByTouch = dt.day }
                            ) {
                                if (isSelected || dt.isPeak) {
                                    Text(
                                        text = "${dt.throughputKt}k",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CopperBright,
                                        fontSize = 9.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (dt.isPeak) CopperBright
                                            else if (isSelected) TelemetryCyan
                                            else SurfaceContainerHighest
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dt.day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (dt.isPeak) CopperBright else OnSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hito Expansión: Nueva Centinela (US$ 4.4B Capex)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLowest
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mine.expansionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = mine.expansionDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mine.expansionCapex,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryCyan
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Balance Hidrico
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainer)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(16.dp))
                            Text(text = "88% MAR", style = MaterialTheme.typography.labelSmall, color = TelemetryCyan, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Sin desalar. Cero extracción cuencas desde 2021.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                    }

                    // Energia 100% Renovable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainer)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = IndustrialEmerald, modifier = Modifier.size(16.dp))
                            Text(text = "PPA 100%", style = MaterialTheme.typography.labelSmall, color = IndustrialEmerald, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Solar + Eólica con Engie & Enel.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                    }

                    // Vida Util
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainer)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = CopperBright, modifier = Modifier.size(16.dp))
                            Text(text = "+30 AÑOS", style = MaterialTheme.typography.labelSmall, color = CopperBright, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Extensión LOM al 2055+.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }
        }

        // Estación Meteorológica & Auditoría Transparencia
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLowest
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "AUDITORÍA & TRANSPARENCIA REGULATORIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Sernageomin
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainer)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "SERNAGEOMIN Dirección Zona Norte", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(text = "Inspección integral conforme. Cero observaciones críticas en polvorines.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                        }
                        Text(text = "CONFORME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IndustrialEmerald)
                    }

                    // SMA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainer)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "SUPERINTENDENCIA MEDIO AMBIENTE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(text = "Emisiones MP10 Sierra Gorda dentro de norma primaria.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                        }
                        Text(text = "VIGENTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CopperBright)
                    }
                }
            }
        }

        // Operational Footer Notes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "HASH: 9F8A2D78-CEN", style = MaterialTheme.typography.labelSmall, color = OutlineColor, fontSize = 10.sp)
            Text(text = "LATENCIA SCADA: 420 ms", style = MaterialTheme.typography.labelSmall, color = OutlineColor, fontSize = 10.sp)
            Text(text = "ACTUALIZACIÓN C/15 MIN", style = MaterialTheme.typography.labelSmall, color = IndustrialEmerald, fontSize = 10.sp)
        }
    }
}
