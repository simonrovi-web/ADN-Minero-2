package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MiningPanel
import com.example.model.NavDestination
import com.example.ui.theme.*
import com.example.viewmodel.MiningUiState
import com.example.viewmodel.MiningViewModel

@Composable
fun PanelesScreen(
    state: MiningUiState,
    viewModel: MiningViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isModoFaena = state.isModoFaena

    // Filter panels based on search query and category
    val filteredPanels = remember(state.panels, state.searchQuery, state.selectedCategory, state.favoritePanels) {
        state.panels.filter { panel ->
            val matchesQuery = state.searchQuery.isEmpty() ||
                    panel.title.contains(state.searchQuery, ignoreCase = true) ||
                    panel.description.contains(state.searchQuery, ignoreCase = true) ||
                    panel.subTags.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = when (state.selectedCategory) {
                "all" -> true
                "live" -> panel.isLive
                "favorites" -> state.favoritePanels.contains(panel.id)
                else -> panel.category == state.selectedCategory
            }

            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Operational Breadcrumb & Live System Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isModoFaena) Color(0xFF141414) else SurfaceContainerLowest)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TERMINAL NACIONAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = CopperBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(text = "/", color = OutlineColor, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "PAN-CL-112",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface,
                    fontSize = 10.sp
                )
                Text(text = "/", color = OutlineColor, style = MaterialTheme.typography.labelSmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(IndustrialEmerald))
                    Text(
                        text = "EN LÍNEA",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndustrialEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "HACE 42S · CSN/COCHILCO SYNC",
                style = MaterialTheme.typography.labelSmall,
                color = TelemetryCyan,
                fontSize = 9.sp
            )
        }

        // Hero Header & Description
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CopperDeep)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "DIVULGACIÓN MINERA INDEPENDIENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
                Text(
                    text = "● ACCESO LIBRE Y PÚBLICO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TelemetryCyan,
                    fontSize = 9.sp
                )
            }

            Text(
                text = "PANELES MINEROS DE CHILE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isModoFaena) FaenaYellow else OnSurface,
                letterSpacing = (-0.02).sp
            )

            Text(
                text = "Tableros de inteligencia en tiempo real, datos oficiales y divulgación de la industria que mueve al país. Conectados directamente con Cochilco, Sernageomin, Dipres y Banco Central.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )

            // Quick Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setDestination(NavDestination.VOZ) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = TelemetryCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Headset, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Noticias con Voz", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        viewModel.setCategory(if (state.selectedCategory == "favorites") "all" else "favorites")
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedCategory == "favorites") CopperPrimaryContainer else SurfaceContainerHigh,
                        contentColor = if (state.selectedCategory == "favorites") Color.White else CopperBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Favoritos (${state.favoritePanels.size})", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.toggleModoFaena() },
                    modifier = Modifier.height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isModoFaena) IndustrialEmerald else FaenaYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "FAENA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }

        // 4 High-Density Operational KPIs
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KPI 1: Cobre Spot LME
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "COBRE SPOT LME", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp)
                            Text(text = "COCHILCO", style = MaterialTheme.typography.labelSmall, color = IndustrialEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "US$ 4.38 / lb", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(text = "▲ +1.42% intradía", style = MaterialTheme.typography.bodySmall, color = IndustrialEmerald, fontSize = 10.sp)
                    }
                }

                // KPI 2: Aporte Fiscal
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "APORTE FISCAL", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp)
                            Text(text = "DIPRES", style = MaterialTheme.typography.labelSmall, color = CopperBright, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "US$ 12.458M", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CopperBright)
                        Text(text = "Tributación + Codelco", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KPI 3: Impacto PIB
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "IMPACTO EN PIB", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp)
                            Text(text = "BCCH", style = MaterialTheme.typography.labelSmall, color = TelemetryCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "10 – 15%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TelemetryCyan)
                        Text(text = "54% Exportaciones", style = MaterialTheme.typography.bodySmall, color = IndustrialEmerald, fontSize = 10.sp)
                    }
                }

                // KPI 4: Catálogo Técnico
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "CATÁLOGO TÉCNICO", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp)
                            Text(text = "AUDITADO", style = MaterialTheme.typography.labelSmall, color = IndustrialEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "112 Paneles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(text = "100% Públicos y Libres", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }
        }

        // Search Input Box
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().testTag("panel_search_input"),
            placeholder = {
                Text(
                    text = "Buscar entre 112 paneles (ej: Chuqui, Royalty, Litio)...",
                    style = MaterialTheme.typography.bodySmall,
                    color = OutlineColor
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = CopperBright)
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar", tint = OnSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = CopperBright,
                unfocusedBorderColor = BorderIndustrial,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            )
        )

        // Filter Category Tabs (Horizontal Scroll)
        val categories = listOf(
            "all" to "Todos (112)",
            "live" to "En Vivo (14)",
            "markets" to "Mercados & Cobre (18)",
            "operations" to "Faenas & Operación (32)",
            "lithium" to "Litio & Minerales (16)",
            "employment" to "Empleo & Salarios (12)",
            "environment" to "Medio Ambiente (20)"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSelected = state.selectedCategory == catKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(catKey) },
                    label = {
                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CopperPrimaryContainer,
                        selectedLabelColor = Color.White,
                        containerColor = if (isModoFaena) Color.Black else SurfaceContainer,
                        labelColor = OnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Live Telemetry Operational Preview Widgets (Seismic + Weather Summary)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CopperBright))
                    Text(
                        text = "TELEMETRÍA OPERATIVA EN TIEMPO REAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Text(
                    text = "CSN / OPEN-METEO",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            // Seismic mini widget
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "RED SÍSMICA FAENAS CSN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CopperBright)
                        Text(text = "NORMAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IndustrialEmerald)
                    }
                    state.seismicRecords.take(3).forEach { sismo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainer)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CopperDeep.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "${sismo.magnitude}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CopperBright)
                                }
                                Column {
                                    Text(text = sismo.location, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OnSurface)
                                    Text(text = "${sismo.district} · Prof ${sismo.depthKm} km", style = MaterialTheme.typography.bodySmall, color = OutlineColor, fontSize = 10.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = sismo.timeAgo, style = MaterialTheme.typography.labelSmall, color = IndustrialEmerald, fontWeight = FontWeight.Bold)
                                Text(text = sismo.status, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bento Grid: Paneles de Mayor Consulta Técnica
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIRECTORIO CENTRAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CopperBright
                    )
                    Text(
                        text = "Paneles de Mayor Consulta Técnica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Text(
                    text = "${filteredPanels.size} paneles",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            filteredPanels.forEach { panel ->
                val isFav = state.favoritePanels.contains(panel.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (panel.id == "p-3") {
                                viewModel.openMineDetail()
                            } else {
                                viewModel.toggleFavoritePanel(panel.id)
                            }
                        }
                        .testTag("panel_card_${panel.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (panel.isLive) IndustrialEmeraldContainer.copy(alpha = 0.3f) else SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = panel.sourceBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (panel.isLive) IndustrialEmerald else CopperBright,
                                    fontSize = 9.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.toggleFavoritePanel(panel.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (isFav) CopperBright else OnSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Abrir panel",
                                    tint = CopperBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = panel.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )

                        Text(
                            text = panel.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        // If Panel 2 (Precios y Mercados), draw sparkline curve
                        if (panel.id == "p-2") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerLowest)
                                    .padding(4.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = Path().apply {
                                        moveTo(0f, size.height * 0.85f)
                                        lineTo(size.width * 0.15f, size.height * 0.7f)
                                        lineTo(size.width * 0.3f, size.height * 0.8f)
                                        lineTo(size.width * 0.45f, size.height * 0.5f)
                                        lineTo(size.width * 0.6f, size.height * 0.6f)
                                        lineTo(size.width * 0.75f, size.height * 0.3f)
                                        lineTo(size.width * 0.9f, size.height * 0.35f)
                                        lineTo(size.width, size.height * 0.1f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = IndustrialEmerald,
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }
                        }

                        // Bottom Metrics Tray
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerLowest)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            panel.metrics.forEach { metric ->
                                Column {
                                    Text(
                                        text = metric.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = metric.value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (metric.isPositive == true) IndustrialEmerald else OnSurface
                                    )
                                }
                            }
                        }

                        // Progress Bar if applicable
                        panel.progressPercent?.let { frac ->
                            LinearProgressIndicator(
                                progress = { frac },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(panel.progressColorHex),
                                trackColor = SurfaceContainerLowest
                            )
                        }

                        if (panel.subTags.isNotEmpty()) {
                            Text(
                                text = panel.subTags,
                                style = MaterialTheme.typography.labelSmall,
                                color = OutlineColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Support Independent Mining Outreach
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CopperDeep.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Coffee, contentDescription = null, tint = CopperBright, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "¿TE SIRVE ADN MINERO? MANTÉN LA RED VIVA",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "ADN Minero es un proyecto 100% independiente, gratuito y sin muros de pago para trabajadores de faena y estudiantes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { viewModel.openCoffeeDialog() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CopperBright,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Invítame un Café vía MercadoPago ($2.000 CLP)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Verified Official Data Sources Protocol
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = TelemetryCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "FUENTES OFICIALES Y PROTOCOLO DE DATOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
                Text(
                    text = "Los datos desplegados provienen exclusivamente de registros públicos auditados de la República de Chile y consorcios científicos globales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    fontSize = 11.sp
                )

                // Grid of Sources
                val sources = listOf(
                    "Cochilco" to "https://www.cochilco.cl",
                    "Sernageomin" to "https://www.sernageomin.cl",
                    "Min. Minería" to "https://www.minmineria.gob.cl",
                    "Dipres Chile" to "https://www.dipres.gob.cl",
                    "Subdere" to "https://www.subdere.gov.cl",
                    "USGS & CSN" to "https://earthquake.usgs.gov"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sources.take(3).forEach { (sName, sUrl) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerLowest)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sUrl))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 10.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sources.takeLast(3).forEach { (sName, sUrl) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerLowest)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sUrl))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 10.sp)
                        }
                    }
                }

                // Disclaimer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerLowest)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CopperBright, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Descargo Operacional: ADN Minero es una herramienta referencial de divulgación tecnológica. No sustituye las alertas formales de emergencia del Centro de Control de Operaciones (CCO).",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
