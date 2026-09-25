package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import com.example.model.NavDestination
import com.example.ui.theme.*
import com.example.viewmodel.MiningUiState
import com.example.viewmodel.MiningViewModel

@Composable
fun InicioFaenaScreen(
    state: MiningUiState,
    viewModel: MiningViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shift = state.shiftInfo
    val isModoFaena = state.isModoFaena

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Telemetría en Vivo & Sat-Sync Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isModoFaena) Color(0xFF141414) else SurfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        .background(if (isModoFaena) FaenaYellow else IndustrialEmerald)
                )
                Text(
                    text = "TELEMETRÍA EN VIVO · MODO FAENA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isModoFaena) FaenaYellow else CopperBright,
                    letterSpacing = 0.05.sp
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isModoFaena) Color.Black else SurfaceContainerLowest)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    tint = if (isModoFaena) FaenaYellow else IndustrialEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "SAT-SYNC OK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isModoFaena) FaenaYellow else IndustrialEmerald,
                    fontSize = 10.sp
                )
            }
        }

        // Shift Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shift_hero_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
            ),
            shape = RoundedCornerShape(12.dp)
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
                        Text(
                            text = shift.faenaName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.05.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shift.shiftName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isModoFaena) FaenaYellow else OnSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CopperPrimaryContainer.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = shift.groupTag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CopperBright
                        )
                    }
                }

                // Grid stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cambio Guardia
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainerLow)
                            .padding(10.dp)
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
                                text = "Cambio Guardia",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shift.changeHour,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }

                    // Red Caminos
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModoFaena) Color.Black else SurfaceContainerLow)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Red Caminos",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shift.roadsStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald,
                            maxLines = 1
                        )
                    }
                }

                // Polvo en suspensión note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isModoFaena) Color.Black else SurfaceContainerLowest.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CopperBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = shift.dustStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Quick Audio Briefing Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setDestination(NavDestination.VOZ) }
                .testTag("quick_audio_briefing_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isModoFaena) Color(0xFF1E1E1E) else SurfaceContainerHigh
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleAudioPlayback() },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CopperPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = if (state.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Reproducir Noticias",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TelemetryCyan)
                            )
                            Text(
                                text = "RESUMEN CON VOZ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TelemetryCyan,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "Noticias del Turno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "2 min · Mercado, Clima y Seguridad",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "128 kbps",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CopperBright
                    )
                    Text(
                        text = "OFFLINE READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Prevención y Seguridad HSE Section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PREVENCIÓN Y SEGURIDAD HSE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.05.sp
                    )
                }
                Text(
                    text = "ALTO IMPACTO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CopperBright
                )
            }

            // Alert 1: UV Radiation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AlertRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALERTA UV EXTREMO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AlertRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = shift.uvLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                            }
                        }
                        Text(
                            text = "Uso obligatorio de legionario/cubre-nuca y lentes oscuros con filtro UV400.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isModoFaena) Color.Black else SurfaceContainerLowest)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = CopperBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Próxima reaplicación bloqueador: ${shift.nextSunscreenHour}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Alert 2: Wind
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = CopperBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VIENTO RAJO ABIERTO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CopperBright
                            )
                            Text(
                                text = shift.windRajoStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialEmerald
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${shift.windRajoSpeed}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "KM/H RÁFAGAS",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }

                        // Progress bar towards limit (45 km/h)
                        LinearProgressIndicator(
                            progress = { shift.windRajoSpeed.toFloat() / shift.windRajoAlertLimit },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndustrialEmerald,
                            trackColor = SurfaceContainerLowest
                        )

                        Text(
                            text = "Alerta preventiva se activa a 45 km/h. Izaje y grúas operando normalmente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Alert 3: Seismic
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = TelemetryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SISMO DISTRITAL REGISTRADO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TelemetryCyan
                            )
                            Text(
                                text = shift.seismicTimeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TelemetryCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MAGNITUD ${shift.seismicDistritalMag} ML",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TelemetryCyan
                                )
                            }
                            Text(
                                text = shift.seismicDistritalDist,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Inspección completada: Sin deformaciones en taludes ni tranques",
                                style = MaterialTheme.typography.bodySmall,
                                color = IndustrialEmerald,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Spot Market Indicators
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "INDICADORES DE MERCADO · SPOT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.05.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CU LME
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CU LME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CopperBright
                            )
                            Text(
                                text = "▲ +1.42%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialEmerald
                            )
                        }
                        Text(
                            text = "US$ 4.38",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "por libra",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // USD / CLP
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "USD / CLP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TelemetryCyan
                            )
                            Text(
                                text = "▼ -0.35%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                        Text(
                            text = "$942.50",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "peso chileno",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Comunicaciones Internas Faena
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
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "COMUNICACIONES INTERNAS FAENA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.05.sp
                    )
                }
                Text(
                    text = "ANEXO SAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            // Policlínico
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shift.clinicaAnnex}"))
                        context.startActivity(dialIntent)
                    }
                    .testTag("contact_policlinico"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(IndustrialEmeraldContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = IndustrialEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Policlínico Mina",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Paramédico disponible · Anexo ${shift.clinicaAnnex}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "LLAMAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialEmerald
                        )
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = IndustrialEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // CCO Central
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shift.ccoAnnex}"))
                        context.startActivity(dialIntent)
                    }
                    .testTag("contact_cco"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModoFaena) Color(0xFF161616) else SurfaceContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TelemetryCyanContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = TelemetryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Centro Control Operacional (CCO)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Despacho Mina & Tráfico · Anexo ${shift.ccoAnnex}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CANAL 4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryCyan
                        )
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = TelemetryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Brigada Rescate Minero (SOS Directo)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSosDialog() }
                    .testTag("contact_brigada_rescate"),
                colors = CardDefaults.cardColors(
                    containerColor = AlertRedContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF690005)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Brigada Rescate Minero",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Urgencias vitales e incidentes mayores",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertRed
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF690005))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SOS DIRECTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // District Cloud ID & Slogan
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "DISTRICT CLOUD ID: SG-CENT-2025 • V12.4 ROBUSTA",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = "\"La seguridad no se negocia, la vida primero.\"",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = CopperBright,
                fontSize = 11.sp
            )
        }
    }
}
