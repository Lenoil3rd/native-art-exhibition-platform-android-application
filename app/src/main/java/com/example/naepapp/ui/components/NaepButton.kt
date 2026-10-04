package com.example.naepapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.example.naepapp.ui.theme.*


// COMPONENT: NaepButton — the only button used in the app
enum class NaepButtonKind { Neutral, Confirm, Destructive }

@Composable
fun NaepButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    kind: NaepButtonKind = NaepButtonKind.Neutral,
    loading: Boolean = false, enabled: Boolean = true, icon: Painter? = null,
    tintIcon: Boolean = true,          // false only for full-color logos such as WhatsApp
) {
    val container = when (kind) {
        NaepButtonKind.Confirm -> BlackForest        // yes / confirm
        NaepButtonKind.Destructive -> OxBlood        // cancel / terminate / delete
        NaepButtonKind.Neutral -> Onyx
    }
    val content = if (kind == NaepButtonKind.Neutral) BrightSnow else BrightSnow
    Button(
        onClick = onClick, enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
        border = if (kind == NaepButtonKind.Neutral) BorderStroke(1.dp, if (loading || !enabled) DustGrey else Onyx) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = container, contentColor = content,
            disabledContainerColor = when {
                kind == NaepButtonKind.Neutral -> DustGrey     // dustGrey = loading/disabled for neutral buttons only
                loading -> container
                else -> container.copy(alpha = 0.4f)
            },
            disabledContentColor = content.copy(alpha = 0.7f),
        ),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = content)
        else {
            if (icon != null) { Icon(icon, null, Modifier.size(20.dp), tint = if (tintIcon) content else Color.Unspecified); Spacer(Modifier.width(8.dp)) }
            Text(text)
        }
    }
}