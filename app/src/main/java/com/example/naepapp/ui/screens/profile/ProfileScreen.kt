package com.example.naepapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naepapp.R
import com.example.naepapp.ui.model.UserUi
import com.example.naepapp.ui.components.AvatarImage
import com.example.naepapp.ui.components.ConfirmDialog
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.theme.BlackForest

@Composable
fun ProfileScreen(
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onOpenGallery: () -> Unit,
    onMyGallery: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    ProfileContent(
        user = user,
        onSignIn = onSignIn,
        onRegister = onRegister,
        onOpenGallery = onOpenGallery,
        onMyGallery = onMyGallery,
        onEditProfile = onEditProfile,
        onSignOut = { viewModel.signOut(onDone = onSignOut) }
    )
}

@Composable
fun ProfileContent(
    user: UserUi?,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onOpenGallery: () -> Unit,
    onMyGallery: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    var confirmSignOut by remember { mutableStateOf(false) }
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.profile)) }) { pad ->
        Column(Modifier.padding(pad).padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (user == null) {                                           // GUEST
                Text(stringResource(R.string.role_guest), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.guest_banner))
                NaepButton(stringResource(R.string.sign_in), onSignIn, Modifier.fillMaxWidth(), NaepButtonKind.Confirm)
                NaepButton(stringResource(R.string.register), onRegister, Modifier.fillMaxWidth())
            } else {
                AvatarImage(user.name, size = 96.dp)
                Text(user.name, style = MaterialTheme.typography.headlineSmall)
                Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(if (user.isArtisan) R.string.role_artisan else R.string.role_enthusiast), color = BlackForest,
                    modifier = Modifier.background(BlackForest.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(10.dp, 4.dp))
                if (user.isArtisan) {
                    NaepButton(stringResource(R.string.my_gallery), onMyGallery, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    NaepButton("Edit Profile", onEditProfile, Modifier.fillMaxWidth())
                }
                else NaepButton(stringResource(R.string.open_a_gallery), onOpenGallery, Modifier.fillMaxWidth(), NaepButtonKind.Confirm)
                Spacer(Modifier.height(4.dp))
                NaepButton(stringResource(R.string.sign_out), { confirmSignOut = true }, Modifier.fillMaxWidth(),
                    NaepButtonKind.Destructive, icon = painterResource(R.drawable.ic_logout))
            }
        }
        if (confirmSignOut) ConfirmDialog(stringResource(R.string.sign_out), "Do you want to sign out?",
            onConfirm = { confirmSignOut = false; onSignOut() }, onDismiss = { confirmSignOut = false })
    }
}
