package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.OpenInNew
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

@Composable
fun SosEmergencyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "EMERGENCIA SOS FAENA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "¿Deseas enlazar llamada inmediata con la Brigada de Rescate Minero o Central de Emergencias?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnAlertRedContainer
                )

                // Rescate Minero
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1407"))
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Llamar Brigada Rescate (1407)", fontWeight = FontWeight.Bold)
                }

                // Policlínico Mina
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:3302"))
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Llamar Policlínico Mina (3302)", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = Color.White)
            }
        },
        containerColor = AlertRedContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("sos_emergency_dialog")
    )
}

@Composable
fun CoffeeSupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = CopperBright,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "MANTÉN ADN MINERO VIVO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ADN Minero es un proyecto de divulgación 100% independiente, gratuito y sin muros de pago para trabajadores, ingenieros y estudiantes de Chile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "No recibimos subsidios gubernamentales ni financiamiento corporativo. Tu aporte financia servidores en la nube y conectores con Cochilco, Sernageomin y CSN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://link.mercadopago.cl/adnminero"))
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CopperBright,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Aportar $2.000 CLP (MercadoPago)", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CERRAR", color = OnSurface)
            }
        },
        containerColor = SurfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("coffee_support_dialog")
    )
}
