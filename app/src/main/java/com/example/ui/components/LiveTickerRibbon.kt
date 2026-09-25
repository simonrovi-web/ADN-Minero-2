package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketQuote
import com.example.ui.theme.*

@Composable
fun LiveTickerRibbon(
    isModoFaena: Boolean,
    onToggleModoFaena: () -> Unit,
    quotes: List<MarketQuote>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticker_ribbon"),
        color = if (isModoFaena) FaenaBg else SurfaceContainerLowest,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Scrollable Ticker Items
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                quotes.forEach { quote ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${quote.name}:".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isModoFaena) Color.LightGray else OnSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${quote.price} ${quote.unit}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (quote.isPositive) IndustrialEmerald else TelemetryCyan,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (quote.isPositive) "▲ ${quote.delta}" else "▼ ${quote.delta}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (quote.isPositive) IndustrialEmerald else TickerDown,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "·",
                        color = BorderIndustrial,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Sismos Ticker Node
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SISMOS NORTE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "M3.8 San Pedro (118km)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CopperBright,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "·",
                    color = BorderIndustrial,
                    style = MaterialTheme.typography.labelSmall
                )

                // Fuentes Node
                Text(
                    text = "FUENTES: COCHILCO · SERNAGEOMIN · DIPRES",
                    style = MaterialTheme.typography.labelSmall,
                    color = OutlineColor,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Modo Faena Toggle Button
            Button(
                onClick = onToggleModoFaena,
                modifier = Modifier
                    .height(28.dp)
                    .testTag("modo_faena_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isModoFaena) FaenaYellow else SurfaceContainerHigh,
                    contentColor = if (isModoFaena) Color.Black else FaenaYellow
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isModoFaena) Color.Black else FaenaYellow.copy(alpha = alphaAnim))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "MODO FAENA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.05.sp
                )
            }
        }
    }
}
