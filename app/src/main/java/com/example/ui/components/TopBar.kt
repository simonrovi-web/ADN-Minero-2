package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun TopBar(
    isModoFaena: Boolean,
    onSosClick: () -> Unit,
    onCoffeeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_bar"),
        color = if (isModoFaena) FaenaBg else SurfaceContainerLow,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo_1790349223300),
                    contentDescription = "ADN Minero Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ADN MINERO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isModoFaena) FaenaYellow else OnSurface,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isModoFaena) FaenaYellow else SurfaceContainerHigh)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "112 PANELES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isModoFaena) Color.Black else CopperBright,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "Divulgación Minera Independiente",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isModoFaena) Color.White.copy(alpha = 0.8f) else OnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Actions: SOS Emergency Button + Coffee
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // SOS Button
                Button(
                    onClick = onSosClick,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("sos_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRedContainer,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergencia SOS Faena",
                        modifier = Modifier.size(16.dp),
                        tint = AlertRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                // Coffee Support Pill
                IconButton(
                    onClick = onCoffeeClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isModoFaena) Color(0xFF222222) else SurfaceContainerHigh)
                        .testTag("coffee_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = "Apoyar ADN Minero",
                        tint = CopperBright,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Operator Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CopperPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil Operador",
                        tint = OnCopperPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
