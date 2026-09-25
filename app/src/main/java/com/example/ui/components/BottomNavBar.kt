package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavDestination
import com.example.ui.theme.*

@Composable
fun BottomNavBar(
    currentDestination: NavDestination,
    onDestinationSelect: (NavDestination) -> Unit,
    isModoFaena: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        color = if (isModoFaena) FaenaBg else SurfaceContainerLowest,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = destination == currentDestination

                val iconVector = when (destination) {
                    NavDestination.INICIO -> Icons.Default.Engineering
                    NavDestination.VOZ -> Icons.Default.GraphicEq
                    NavDestination.ALERTAS -> Icons.Default.Warning
                    NavDestination.PANELES -> Icons.Default.Analytics
                }

                val activeColor = if (isModoFaena) FaenaYellow else CopperBright
                val inactiveColor = if (isModoFaena) Color.Gray else OnSurfaceVariant

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDestinationSelect(destination) }
                        .padding(vertical = 6.dp)
                        .testTag("nav_item_${destination.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = destination.label,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) activeColor else inactiveColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) activeColor else inactiveColor,
                        fontSize = 11.sp,
                        letterSpacing = 0.05.sp
                    )
                }
            }
        }
    }
}
