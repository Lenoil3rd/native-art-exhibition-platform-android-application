package com.example.naepapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.navigation.compose.*
import com.example.naepapp.R
import com.example.naepapp.ui.screens.SplashScreen
import com.example.naepapp.ui.screens.auth.LoginScreen
import com.example.naepapp.ui.screens.auth.RegisterScreen
import com.example.naepapp.ui.screens.auth.RoleSelectionScreen
import com.example.naepapp.ui.screens.marketplace.MarketplaceScreen
import com.example.naepapp.ui.screens.creation.CreationDetailScreen
import com.example.naepapp.ui.screens.mygallery.MyGalleryScreen
import com.example.naepapp.ui.screens.mygallery.CreationEditorScreen
import com.example.naepapp.ui.screens.artisan.GalleryScreen
import com.example.naepapp.ui.screens.artisan.ArtisanProfileScreen
import com.example.naepapp.ui.screens.artisan.OpenGalleryScreen
import com.example.naepapp.ui.screens.profile.ProfileScreen
import com.example.naepapp.ui.screens.profile.EditProfileScreen
import com.example.naepapp.ui.theme.*
import kotlinx.coroutines.launch

// ui/navigation/NaepNavHost.kt  — NAVIGATION: the app shell. Starts on the Splash screen.
@Composable
fun NaepNavHost(mainViewModel: MainViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val user by mainViewModel.user.collectAsStateWithLifecycle()
    val backStack by nav.currentBackStackEntryAsState()
    val dest = backStack?.destination
    // The bottom bar only shows on the three top-level pages
    val showBar = dest?.hasRoute<MarketplaceRoute>() == true || dest?.hasRoute<MyGalleryRoute>() == true || dest?.hasRoute<ProfileRoute>() == true
    val isArtisanUser = user?.isArtisan == true

    Column(Modifier.fillMaxSize().background(BrightSnow)) {
        // consumeWindowInsets avoids double bottom padding when the bar is visible (it handles the nav-bar inset itself)
        Box(Modifier.weight(1f).then(if (showBar) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier)) {
            NavHost(nav, startDestination = SplashRoute) {
                composable<SplashRoute> {
                    SplashScreen(
                        onTimeout = {
                            scope.launch {
                                val hasSession = mainViewModel.checkSession()
                                if (hasSession) {
                                    nav.navigate(MarketplaceRoute) {
                                        popUpTo<SplashRoute> { inclusive = true }
                                    }
                                } else {
                                    nav.navigate(LoginRoute()) {
                                        popUpTo<SplashRoute> { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }

                composable<MarketplaceRoute> {
                    MarketplaceScreen(onOpenCreation = { nav.navigate(CreationDetailRoute(it)) })
                }
                composable<CreationDetailRoute>(
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "http://127.0.0.1:3000/creations/{id}" },
                        navDeepLink { uriPattern = "http://localhost:3000/creations/{id}" }
                    )
                ) {
                    CreationDetailScreen(
                        onBack = { nav.popBackStack() },
                        onOpenArtisan = { nav.navigate(ArtisanProfileRoute(it)) },
                        onEdit = { nav.navigate(CreationEditorRoute(it)) },
                        onSignIn = { nav.navigate(LoginRoute(returnToPrevious = true)) })
                }
                composable<ArtisanProfileRoute> {
                    ArtisanProfileScreen(
                        onBack = { nav.popBackStack() },
                        onOpenCreation = { nav.navigate(CreationDetailRoute(it)) },
                        onOpenGallery = { nav.navigate(GalleryRoute(it)) },
                        onSignIn = { nav.navigate(LoginRoute(returnToPrevious = true)) })
                }
                composable<GalleryRoute> {
                    GalleryScreen(
                        onBack = { nav.popBackStack() },
                        onOpenCreation = { nav.navigate(CreationDetailRoute(it)) })
                }
                composable<LoginRoute> { entry ->
                    val back = entry.toRoute<LoginRoute>().returnToPrevious
                    LoginScreen(
                        onSignIn = { _ ->
                            if (back) {
                                nav.popBackStack()
                            } else {
                                nav.navigate(MarketplaceRoute) { popUpTo(0) }
                            }
                        },
                        onRegister = { nav.navigate(RegisterRoute(back)) },
                        onGuest = { nav.navigate(MarketplaceRoute) { popUpTo(0) } })
                }
                composable<RegisterRoute> { entry ->
                    val back = entry.toRoute<RegisterRoute>().returnToPrevious
                    RegisterScreen(
                        onRegistered = {
                            nav.navigate(RoleSelectionRoute(back)) { popUpTo<RegisterRoute> { inclusive = true }; popUpTo<LoginRoute> { inclusive = true } }
                        },
                        onBack = { nav.popBackStack() })
                }
                composable<RoleSelectionRoute> { entry ->
                    val back = entry.toRoute<RoleSelectionRoute>().returnToPrevious
                    RoleSelectionScreen(onDone = { becameArtisan ->
                        when {
                            becameArtisan -> nav.navigate(OpenGalleryRoute) { popUpTo<RoleSelectionRoute> { inclusive = true } }
                            back -> nav.popBackStack()
                            else -> nav.navigate(MarketplaceRoute) { popUpTo(0) }
                        }
                    })
                }
                composable<ProfileRoute> {
                    ProfileScreen(
                        onSignIn = { nav.navigate(LoginRoute()) }, onRegister = { nav.navigate(RegisterRoute()) },
                        onOpenGallery = { nav.navigate(OpenGalleryRoute) }, onMyGallery = { nav.navigate(MyGalleryRoute) },
                        onEditProfile = { nav.navigate(EditProfileRoute) },
                        onSignOut = { mainViewModel.logout(); nav.navigate(MarketplaceRoute) { popUpTo(0) } })
                }
                composable<EditProfileRoute> {
                    EditProfileScreen(onBack = { nav.popBackStack() })
                }
                composable<OpenGalleryRoute> {
                    OpenGalleryScreen(
                        onBack = { nav.popBackStack() },
                        onDone = {
                            nav.navigate(MyGalleryRoute) { popUpTo<OpenGalleryRoute> { inclusive = true } }
                        })
                }
                composable<MyGalleryRoute> {
                    MyGalleryScreen(
                        onAdd = { nav.navigate(CreationEditorRoute()) }, onEdit = { nav.navigate(CreationEditorRoute(it)) },
                        onOpenCreation = { nav.navigate(CreationDetailRoute(it)) })
                }
                composable<CreationEditorRoute> {
                    CreationEditorScreen(
                        onBack = { nav.popBackStack() })
                }
            }
        }
        if (showBar) NaepBottomBar(nav, dest, isArtisan = isArtisanUser)
    }
}

// NAVIGATION: bottom bar — Marketplace, My Gallery (only once a gallery is opened), Profile
@Composable
private fun NaepBottomBar(nav: NavHostController, dest: NavDestination?, isArtisan: Boolean) {
    NavigationBar(containerColor = BrightSnow) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = BrightSnow, selectedTextColor = BlackForest,
            indicatorColor = Onyx, unselectedIconColor = Onyx, unselectedTextColor = Onyx)
        NavigationBarItem(dest?.hasRoute<MarketplaceRoute>() == true,
            { nav.navigate(MarketplaceRoute) { launchSingleTop = true; popUpTo<MarketplaceRoute>() } },
            { Icon(painterResource(R.drawable.ic_storefront), null) }, label = { Text(stringResource(R.string.marketplace)) }, colors = itemColors)
        if (isArtisan) NavigationBarItem(dest?.hasRoute<MyGalleryRoute>() == true,
            { nav.navigate(MyGalleryRoute) { launchSingleTop = true } },
            { Icon(painterResource(R.drawable.ic_palette), null) }, label = { Text(stringResource(R.string.my_gallery)) }, colors = itemColors)
        NavigationBarItem(dest?.hasRoute<ProfileRoute>() == true,
            { nav.navigate(ProfileRoute) { launchSingleTop = true } },
            { Icon(painterResource(R.drawable.ic_person), null) }, label = { Text(stringResource(R.string.profile)) }, colors = itemColors)
    }
}
