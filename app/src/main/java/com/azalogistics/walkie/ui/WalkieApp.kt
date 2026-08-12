package com.azalogistics.walkie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azalogistics.walkie.domain.model.ConnectionState
import com.azalogistics.walkie.domain.model.TxState
import com.azalogistics.walkie.ptt.PttKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkieApp(
    vm: WalkieViewModel,
    accessibilityEnabled: Boolean = true,
    batteryOptimized: Boolean = true,
    onOpenAccessibility: () -> Unit = {},
    onOpenBatterySettings: () -> Unit = {}
) {
    MaterialTheme {
        val state by vm.state.collectAsStateWithLifecycle()
        val channels by vm.channels.collectAsStateWithLifecycle()
        val listenChannelIds by vm.listenChannelIds.collectAsStateWithLifecycle()
        val txChannelId by vm.txChannelId.collectAsStateWithLifecycle()
        val pttKey by vm.pttKeyCode.collectAsStateWithLifecycle()

        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!accessibilityEnabled) {
                    WarningBanner(
                        text = "PTT fuera de la app desactivado",
                        buttonText = "Activar",
                        onClick = onOpenAccessibility
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (!batteryOptimized) {
                    WarningBanner(
                        text = "La batería puede matar la app. Activa 'Sin restricciones' para que arranque con el sistema.",
                        buttonText = "Configurar",
                        onClick = onOpenBatterySettings
                    )
                    Spacer(Modifier.height(8.dp))
                }
                StatusBar(state.connection, state.tx)
                Spacer(Modifier.height(12.dp))

                PttButton(
                    enabled = txChannelId != null,
                    transmitting = state.tx == TxState.TRANSMITTING,
                    onDown = vm::pttDown,
                    onUp = vm::pttUp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    txChannelId?.let { "TX channel: $it" } ?: "Select a TX channel below",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))
                Text("Channels", fontWeight = FontWeight.Bold)
                channels.forEach { ch ->
                    ChannelRow(
                        name = ch.name,
                        id = ch.id,
                        listening = ch.id in listenChannelIds,
                        isTx = ch.id == txChannelId,
                        receiving = ch.id in state.receivingChannelIds,
                        onListen = { vm.toggleListen(ch.id, it) },
                        onSelectTx = { vm.selectTx(if (txChannelId == ch.id) null else ch.id) },
                        onRemove = { vm.removeChannel(ch.id) }
                    )
                    HorizontalDivider()
                }

                Spacer(Modifier.height(8.dp))
                AddChannel(onAdd = vm::addChannel)
                Spacer(Modifier.height(8.dp))
                val detecting by vm.detectingPttKey.collectAsStateWithLifecycle()
                PttKeyPicker(
                    current = pttKey,
                    detecting = detecting,
                    onPick = vm::setPttKey,
                    onStartDetect = vm::startPttKeyDetection,
                    onCancelDetect = vm::cancelPttKeyDetection
                )
                Spacer(Modifier.height(8.dp))
                PeerCount(state.peers.size)
            }
        }
    }
}

@Composable
private fun StatusBar(conn: ConnectionState, tx: TxState) {
    val (label, color) = when (conn) {
        ConnectionState.CONNECTED -> "Connected" to Color(0xFF2E7D32)
        ConnectionState.CONNECTING -> "Connecting…" to Color(0xFFF9A825)
        ConnectionState.RECONNECTING -> "Reconnecting…" to Color(0xFFF9A825)
        ConnectionState.NO_NETWORK -> "No network" to Color(0xFFC62828)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        if (tx == TxState.TRANSMITTING) {
            AssistChip(onClick = {}, label = { Text("TRANSMITTING") })
        }
    }
}

@Composable
private fun PttButton(enabled: Boolean, transmitting: Boolean, onDown: () -> Unit, onUp: () -> Unit) {
    val bg = when {
        !enabled -> Color(0xFFBDBDBD)
        transmitting -> Color(0xFFC62828)
        else -> Color(0xFF1565C0)
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = {
                    onDown()
                    tryAwaitRelease()
                    onUp()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (transmitting) "TALKING" else "HOLD TO TALK",
            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChannelRow(
    name: String, id: String, listening: Boolean, isTx: Boolean, receiving: Boolean,
    onListen: (Boolean) -> Unit, onSelectTx: () -> Unit, onRemove: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(
                when { receiving -> "● receiving"; listening -> "listening"; else -> "" },
                fontSize = 12.sp,
                color = if (receiving) Color(0xFF2E7D32) else Color.Gray
            )
        }
        Text("Listen", fontSize = 12.sp)
        Switch(checked = listening, onCheckedChange = onListen)
        Spacer(Modifier.width(8.dp))
        FilterChip(selected = isTx, onClick = onSelectTx, label = { Text("TX") })
        IconButton(onClick = onRemove) { Text("✕") }
    }
}

@Composable
private fun AddChannel(onAdd: (String, String) -> Unit) {
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(id, { id = it }, Modifier.weight(1f), label = { Text("id") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(name, { name = it }, Modifier.weight(1f), label = { Text("name") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        Button(onClick = { onAdd(id, name); id = ""; name = "" }) { Text("Add") }
    }
}

@Composable
private fun PttKeyPicker(
    current: Int,
    detecting: Boolean,
    onPick: (Int) -> Unit,
    onStartDetect: () -> Unit,
    onCancelDetect: () -> Unit
) {
    Column {
        Text("PTT key: ${PttKey.label(current)}", fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PttKey.candidates.forEach { (code, label) ->
                FilterChip(
                    selected = code == current,
                    onClick = { onPick(code) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        if (detecting) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pulsa la tecla PTT…", Modifier.weight(1f), fontSize = 13.sp, color = Color(0xFF0D47A1))
                TextButton(onClick = onCancelDetect) { Text("Cancelar") }
            }
        } else {
            OutlinedButton(onClick = onStartDetect) {
                Text("Detectar tecla automáticamente", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PeerCount(n: Int) {
    Text("Peers on LAN: $n", fontSize = 12.sp, color = Color.Gray)
}

@Composable
private fun WarningBanner(text: String, buttonText: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, Modifier.weight(1f), fontSize = 13.sp, color = Color(0xFF7B3F00))
        Spacer(Modifier.width(8.dp))
        Button(onClick = onClick) { Text(buttonText, fontSize = 12.sp) }
    }
}
