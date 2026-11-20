package com.example.naepapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.core.db.entity.PendingOperationEntity
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.Onyx
import com.example.naepapp.ui.theme.OxBlood
import com.example.naepapp.ui.theme.BrightSnow

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Pending(val count: Int) : SyncStatus()
    data class Failed(val count: Int) : SyncStatus()
}

@Composable
fun SyncStatusChip(
    status: SyncStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (text, textColor) = when (status) {
        is SyncStatus.Idle -> stringResource(R.string.sync_status_all_saved) to Onyx
        is SyncStatus.Syncing -> stringResource(R.string.sync_status_syncing) to Onyx
        is SyncStatus.Pending -> stringResource(R.string.sync_status_pending, status.count) to Onyx
        is SyncStatus.Failed -> stringResource(R.string.sync_status_failed, status.count) to OxBlood
    }

    Box(
        modifier = modifier
            .background(DustGrey, RoundedCornerShape(16.dp))
            .clickable(enabled = status is SyncStatus.Failed) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedOperationsSheet(
    failedOps: List<PendingOperationEntity>,
    onRetry: (PendingOperationEntity) -> Unit,
    onDiscard: (PendingOperationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var opToDiscard by remember { mutableStateOf<PendingOperationEntity?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Failed Changes", style = MaterialTheme.typography.titleLarge)
            if (failedOps.isEmpty()) {
                Text("No failed changes.")
            } else {
                failedOps.forEach { op ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrightSnow)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Type: ${op.type}", style = MaterialTheme.typography.titleSmall)
                            Text("Error: ${op.lastError ?: "Unknown error"}", color = OxBlood, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NaepButton(stringResource(R.string.retry), { onRetry(op) }, modifier = Modifier.weight(1f))
                                NaepButton(stringResource(R.string.discard_changes), { opToDiscard = op }, modifier = Modifier.weight(1f), kind = NaepButtonKind.Destructive)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    opToDiscard?.let { op ->
        ConfirmDialog(
            title = stringResource(R.string.discard_changes),
            message = "Discard this change? This cannot be undone.",
            onConfirm = {
                onDiscard(op)
                opToDiscard = null
            },
            onDismiss = { opToDiscard = null }
        )
    }
}
