package com.example.naepapp.ui.navigation

import kotlinx.serialization.Serializable

// ui/navigation/Routes.kt  — NAVIGATION: every destination as a type-safe class
@Serializable object MarketplaceRoute
@Serializable data class CreationDetailRoute(val id: String)
@Serializable data class ArtisanProfileRoute(val id: String)
@Serializable data class GalleryRoute(val id: String)
@Serializable data class LoginRoute(val returnToPrevious: Boolean = false)
@Serializable data class RegisterRoute(val returnToPrevious: Boolean = false)
@Serializable data class RoleSelectionRoute(val returnToPrevious: Boolean = false)
@Serializable object ProfileRoute
@Serializable object OpenGalleryRoute
@Serializable object MyGalleryRoute
@Serializable data class CreationEditorRoute(val id: String? = null)
@Serializable object EditProfileRoute
