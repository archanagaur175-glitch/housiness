package com.housiness.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var scaleAnim by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(quantity) {
        scaleAnim = 1.3f
        delay(100)
        scaleAnim = 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = scaleAnim,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stepper_bounce"
    )

    Row(
        modifier = modifier
            .scale(animatedScale)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (quantity > 1) onQuantityChange(quantity - 1)
            },
            enabled = quantity > 1
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease",
                tint = if (quantity > 1) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }

        Text(
            text = "$quantity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        IconButton(
            onClick = { onQuantityChange(quantity + 1) }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
