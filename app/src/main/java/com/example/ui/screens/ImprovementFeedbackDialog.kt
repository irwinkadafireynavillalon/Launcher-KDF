package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ImprovementFeedbackDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSendFeedback: (String) -> Unit
) {
    var feedback by remember { mutableStateOf("") }
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sugerir Mejora", color = Color.White) },
        text = {
            Column {
                Text("Describe qué mejora te gustaría ver en Hyper Lux:", color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MetalSecondary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSendFeedback(feedback)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary)
            ) {
                Text("Enviar", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        },
        containerColor = Color.White,
        modifier = Modifier.border(1.dp, MetalSecondary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    )
}
