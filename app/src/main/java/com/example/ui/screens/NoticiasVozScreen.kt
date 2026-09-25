package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MiningUiState
import com.example.viewmodel.MiningViewModel

@Composable
fun NoticiasVozScreen(
    state: MiningUiState,
    viewModel: MiningViewModel,
    modifier: Modifier = Modifier
) {
    val isModoFaena = state.isModoFaena
    val activeChapter = state.audioChapters.getOrNull(state.activeChapterIndex) ?: state.audioChapters[0]

    // Animated bar wave multiplier
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveFactor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ticker Live Stream Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isModoFaena) Color(0xFF141414) else SurfaceContainerLowest)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.isPlayingAudio) IndustrialEmerald else CopperBright)
                )
                Text(
                    text = "STREAM MATINAL EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CopperBright
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = null,
                    tint = IndustrialEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "RAJO SUR: 4G ESTABLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        // Hero Audio Boletin Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FaenaYellow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AUDIO BOLETÍN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 9.sp
                                )
                            }
                            Text(
                                text = "EDICIÓN 08:00 AM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TelemetryCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Resumen Minero Matinal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Edición 28 Febrero 2025 • Turno A y Relevo",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CopperBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = viewModel.formatSeconds(state.audioTotalSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CopperBright
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Voz IA: Mateo (Natural Chile)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = IndustrialEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Aptitud Cabina 85dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = IndustrialEmerald,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Tactile Audio Player Core (Giant Driving Mode Player)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tactile_audio_player"),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceSlate
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "CONTROLES DE ALTO IMPACTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHighest)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MODO CONDUCCIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurface,
                            fontSize = 9.sp
                        )
                    }
                }

                // Sonic Waveform Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLowest)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val baseHeights = listOf(14, 28, 42, 22, 38, 50, 44, 30, 18, 26, 36, 48, 20, 32, 45, 25, 38, 16, 28, 18)
                            baseHeights.forEachIndexed { idx, bh ->
                                val currentFrac = state.audioCurrentSeconds.toFloat() / state.audioTotalSeconds.coerceAtLeast(1)
                                val barFrac = idx.toFloat() / baseHeights.size
                                val isPlayed = barFrac <= currentFrac

                                val animatedHeight = if (state.isPlayingAudio) {
                                    val offset = if (idx % 2 == 0) waveAnim else (1.3f - waveAnim)
                                    (bh * offset).coerceIn(8f, 54f).dp
                                } else {
                                    bh.dp
                                }

                                val barColor = when {
                                    isPlayed -> CopperBright
                                    state.isPlayingAudio && (idx % 3 == 0) -> TelemetryCyan
                                    else -> SurfaceContainerHighest
                                }

                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(animatedHeight)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        // Slider Scrubber
                        Slider(
                            value = state.audioCurrentSeconds.toFloat(),
                            onValueChange = { viewModel.seekAudioTo(it.toInt()) },
                            valueRange = 0f..state.audioTotalSeconds.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = FaenaYellow,
                                activeTrackColor = CopperBright,
                                inactiveTrackColor = SurfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("audio_scrubber")
                        )

                        // Time Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.formatSeconds(state.audioCurrentSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CopperBright
                            )
                            Text(
                                text = viewModel.formatSeconds(state.audioTotalSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Active Headline Callout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CopperBright.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REPRODUCIENDO AHORA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryCyan,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${activeChapter.timeLabel} • ${activeChapter.title}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1
                        )
                    }
                }

                // Jumbo Transport Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 15s
                    IconButton(
                        onClick = { viewModel.rewind15() },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerHigh)
                            .testTag("rewind_15_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Retroceder 15s",
                                tint = OnSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "-15s",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Master Jumbo Play/Pause (#D97707)
                    IconButton(
                        onClick = { viewModel.toggleAudioPlayback() },
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(CopperPrimaryContainer)
                            .testTag("master_play_button")
                    ) {
                        Icon(
                            imageVector = if (state.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlayingAudio) "Pausar" else "Reproducir",
                            tint = Color.Black,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // Forward 30s
                    IconButton(
                        onClick = { viewModel.forward30() },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerHigh)
                            .testTag("forward_30_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Forward30,
                                contentDescription = "Avanzar 30s",
                                tint = OnSurface,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "+30s",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Speed Pills & Hands-free Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Speed Selector
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(1.0f, 1.25f, 1.5f).forEach { spd ->
                            val isSelected = state.audioSpeed == spd
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) CopperPrimaryContainer else Color.Transparent)
                                    .clickable { viewModel.setAudioSpeed(spd) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${spd}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Hands-Free Toggle
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest)
                            .clickable { viewModel.toggleHandsFree() }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (state.isHandsFree) CopperBright else OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isHandsFree) "MANOS LIBRES" else "MODO MANUAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isHandsFree) CopperBright else OnSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Shift Automation & Offline Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto-play switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmOn,
                                contentDescription = null,
                                tint = FaenaYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Auto-Play en Inicio de Turno",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Sincroniza al encender el móvil a las 07:00 / 19:00",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = state.isShiftAutoPlay,
                        onCheckedChange = { viewModel.toggleShiftAutoPlay() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CopperBright,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = SurfaceContainerHigh
                        )
                    )
                }

                // Offline Pre-download status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLowest)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = IndustrialEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Descarga Local Offline Lista",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "4.8 MB guardados para rajo sin señal",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.refreshOfflineDownload() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerHigh,
                            contentColor = CopperBright
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTUALIZAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Chapters / Titulares del Boletin
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = CopperBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "TITULARES DEL BOLETÍN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CopperBright
                    )
                }
                Text(
                    text = "TOCA PARA SALTAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            state.audioChapters.forEachIndexed { idx, chapter ->
                val isActive = idx == state.activeChapterIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectChapter(idx) }
                        .testTag("chapter_item_$idx"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) (if (isModoFaena) Color(0xFF242424) else SurfaceContainerHigh)
                        else (if (isModoFaena) Color(0xFF161616) else SurfaceContainer)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isActive) CopperBright else SurfaceContainerHighest)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = chapter.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.Black else OnSurface
                                )
                            }

                            Column {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = OnSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chapter.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isActive) CopperBright else OnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isActive) Icons.Default.PlayCircle else Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = if (isActive) CopperBright else OnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Independent Support & Buy a Coffee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainerLow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Divulgación Independiente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CopperBright.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "100% LIBRE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CopperBright,
                                    fontSize = 8.sp
                                )
                            }
                        }
                        Text(
                            text = "ADN Minero se financia sin presiones corporativas, directo para los trabajadores del turno.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { viewModel.openCoffeeDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("coffee_support_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CopperPrimaryContainer,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Invitar un Café al Desarrollador ($1.500 CLP)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
