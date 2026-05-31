package app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.theme.appRed
import app.theme.colorHomeDarkGreen
import app.theme.colorHomeLightGreen
import feature.common.events.DialogEvent

@Composable
fun DialogWrapper(
    dialogEvent: DialogEvent,
    onClosed: () -> Unit,
) {
    EventDialog(
        event = dialogEvent,
        onDismiss = {
            onClosed.invoke()
        },
        onCancelable = {
            if (dialogEvent.isCancelable) {
                onClosed.invoke()
            }
        }
    )
}

@Composable
private fun EventDialog(
    event: DialogEvent,
    onDismiss: () -> Unit,
    onCancelable: () -> Unit
) {
    Dialog(onDismissRequest = onCancelable) {
        Box(
            Modifier
        ) {
            Column(
                Modifier
            ) {
                Box(
                    Modifier
                        .background(
                            color = colorHomeDarkGreen,
                            shape = ShapeDefaults.Large
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            event.title?.let {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = appRed,
                                    modifier = Modifier
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Color Selection
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column {
                                    event.body?.let {
                                        Text(
                                            text = it,
                                            color = colorHomeLightGreen,
                                        )
                                    }
                                }
                            }
                            // Buttons
                            if (event.positiveButton != null || event.negativeButton != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    event.negativeButton?.let {
                                        val fillWeight = event.positiveButton != null
                                        TextButton(
                                            modifier = Modifier.weight(1f, fillWeight),
                                            onClick = {
                                                it.action?.invoke()
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.textButtonColors(

                                            )
                                        ) {
                                            Text(
                                                text = it.ctaLabel ?: "Cancel",
                                                color = colorHomeLightGreen,
                                            )
                                        }
                                    }
                                    event.positiveButton?.let {
                                        val fillWeight = event.negativeButton != null
                                        TextButton(
                                            modifier = Modifier.weight(1f, fillWeight),
                                            onClick = {
                                                it.action?.invoke()
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.textButtonColors(

                                            )
                                        ) {
                                            Text(
                                                text = it.ctaLabel ?: "Ok",
                                                color = appRed,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
