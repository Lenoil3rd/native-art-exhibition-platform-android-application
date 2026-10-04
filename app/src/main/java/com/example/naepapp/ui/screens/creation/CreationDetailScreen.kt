package com.example.naepapp.ui.screens.creation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.model.UserUi
import com.example.naepapp.ui.model.inquiryMessage
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.components.NaepButtonKind
import kotlinx.coroutines.launch


@Composable
fun CreationDetailScreen(
    creation: CreationUi, user: UserUi?,
    onBack: () -> Unit, onOpenArtisan: (String) -> Unit, onEdit: (String) -> Unit, onSignIn: () -> Unit,
) {
    var liked by remember { mutableStateOf(false) }
    var likes by remember { mutableIntStateOf(creation.likes) }
    var available by remember { mutableStateOf(creation.available) }
    var guestPrompt by remember { mutableStateOf<String?>(null) }     // message shown in the sign-in sheet
    var showInquiry by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isOwn = user?.artisanId == creation.artisanId                  // artisans cannot like/inquire their own pieces
    val likePrompt = stringResource(R.string.guest_prompt_like)
    val inquirePrompt = stringResource(R.string.guest_prompt_inquire)

    Scaffold(topBar = { NaepTopBar(creation.title, onBack) }, snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {
            ImageCarousel(creation.photoCount, Modifier.fillMaxWidth().aspectRatio(1f))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(creation.title, style = MaterialTheme.typography.headlineSmall)
                Row(Modifier.clickable { onOpenArtisan(creation.artisanId) }, verticalAlignment = Alignment.CenterVertically) {
                    AvatarImage(creation.artisanName); Spacer(Modifier.width(8.dp)); Text(creation.artisanName)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriceText(creation.price, creation.currency); AvailabilityBadge(available)
                }
                DetailRow(stringResource(R.string.category), creation.categoryName)
                DetailRow(stringResource(R.string.dimensions), creation.dimensions)
                Text(creation.description)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LikeButton(liked, likes, enabled = !isOwn, onClick = {
                        if (user == null) guestPrompt = likePrompt
                        else { liked = !liked; likes += if (liked) 1 else -1 }          // optimistic toggle
                    })
                    Spacer(Modifier.width(16.dp))
                    Icon(painterResource(R.drawable.ic_visibility), stringResource(R.string.views), Modifier.size(20.dp))
                    Text(" ${creation.views}")
                }
                if (isOwn) {
                    AvailabilityToggle(available, { available = !available })                  // FR-8
                    NaepButton(stringResource(R.string.edit_creation), { onEdit(creation.id) }, Modifier.fillMaxWidth())
                } else {
                    NaepButton(stringResource(R.string.inquire_creation),
                        { if (user == null) guestPrompt = inquirePrompt else showInquiry = true },
                        Modifier.fillMaxWidth(), NaepButtonKind.Confirm)                        // FR-12
                }
            }
        }
        guestPrompt?.let { SignInPromptSheet(it, onSignIn = { guestPrompt = null; onSignIn() }, onDismiss = { guestPrompt = null }) }
        if (showInquiry) ConfirmDialog(
            title = stringResource(R.string.inquire_creation), message = inquiryMessage(creation),        // FR-13 preview
            onConfirm = {
                showInquiry = false
                // TODO: open the WhatsApp chat with this text (wa.me link) here
                scope.launch { snackbar.showSnackbar("WhatsApp would open now with this message") }
            },
            onDismiss = { showInquiry = false })
    }
}

@Composable private fun DetailRow(label: String, value: String) =
    Row { Text("$label: ", fontWeight = FontWeight.SemiBold); Text(value) }