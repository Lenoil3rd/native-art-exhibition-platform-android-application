package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.AvatarImage
import com.example.naepapp.ui.components.CreationCard
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.components.SectionHeader
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.model.ArtisanUi
import com.example.naepapp.ui.model.CreationUi
import kotlinx.coroutines.launch

@Composable
fun ArtisanProfileScreen(
    artisan: ArtisanUi, creations: List<CreationUi>, signedIn: Boolean,
    onBack: () -> Unit, onOpenCreation: (String) -> Unit, onOpenGallery: (String) -> Unit, onSignIn: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.artisan_profile), onBack) }, snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        LazyVerticalGrid(
            GridCells.Adaptive(160.dp), Modifier.padding(pad), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarImage(artisan.name, size = 96.dp)
                    Text(artisan.name, style = MaterialTheme.typography.headlineSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_location_on), null, Modifier.size(18.dp)); Text(artisan.location)
                    }
                    Text(artisan.categoryName, color = BlackForest,
                        modifier = Modifier.background(BlackForest.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(8.dp, 4.dp))
                    Text(artisan.bio)
                    if (signedIn) NaepButton(stringResource(R.string.contact_on_whatsapp),
                        { scope.launch { snackbar.showSnackbar("WhatsApp would open: ${artisan.whatsapp}") } },   // TODO real wa.me link
                        Modifier.fillMaxWidth(),)
                    else NaepButton(stringResource(R.string.sign_in_to_contact), onSignIn, Modifier.fillMaxWidth())   // Guests: no number shown
                    SectionHeader(stringResource(R.string.recent_creations), Modifier.fillMaxWidth())
                }
            }
            items(creations, key = { it.id }) { c -> CreationCard(c, { onOpenCreation(c.id) }) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                NaepButton(stringResource(R.string.view_full_gallery), { onOpenGallery(artisan.id) }, Modifier.fillMaxWidth())
            }
        }
    }
}