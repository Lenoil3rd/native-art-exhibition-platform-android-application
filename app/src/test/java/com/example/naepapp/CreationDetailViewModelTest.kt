package com.example.naepapp

import androidx.lifecycle.SavedStateHandle
import com.example.naepapp.core.common.ConnectivityMonitor
import com.example.naepapp.core.common.WhatsAppSharer
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.dao.PendingOperationDao
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.entity.CreationMediaEntity
import com.example.naepapp.core.db.entity.PendingOperationEntity
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.repository.CreationRepository
import com.example.naepapp.data.session.PendingAction
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.data.sync.SyncScheduler
import com.example.naepapp.ui.screens.creation.CreationDetailViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class CreationDetailViewModelTest {

    private class FakeTokenStorage : TokenStorage {
        override suspend fun save(accessToken: String, refreshToken: String) {}
        override suspend fun access(): String? = null
        override suspend fun refresh(): String? = null
        override suspend fun clear() {}
    }

    private class FakeCreationDao : CreationDao {
        override fun observeById(id: String): Flow<CreationEntity?> = flowOf(null)
        override suspend fun getById(id: String): CreationEntity? = null
        override fun observeMediaForCreation(creationId: String): Flow<List<CreationMediaEntity>> = flowOf(emptyList())
        override suspend fun getMediaForCreation(creationId: String): List<CreationMediaEntity> = emptyList()
        override fun observeMyCreations(artisanId: String): Flow<List<CreationEntity>> = flowOf(emptyList())
        override fun getFeed(queryKey: String) = TODO()
        override suspend fun upsert(creation: CreationEntity) {}
        override suspend fun upsertAll(creations: List<CreationEntity>) {}
        override suspend fun upsertMedia(media: List<CreationMediaEntity>) {}
        override suspend fun deleteMediaForCreation(creationId: String) {}
        override suspend fun saveCreationWithMedia(creation: CreationEntity, mediaList: List<CreationMediaEntity>) {}
        override suspend fun deleteById(id: String) {}
        override suspend fun updateLikeStatus(id: String, isLikedByMe: Boolean, likesCount: Int) {}
        override suspend fun updateAvailability(id: String, status: String) {}
        override suspend fun updateIsOwn(artisanId: String) {}
        override suspend fun clearUserPrivateData() {}
        override suspend fun updateSyncState(id: String, syncState: String) {}
        override suspend fun pruneUnreferencedCreations(cutoff: Long) {}
    }

    private class FakePendingOperationDao : PendingOperationDao {
        override suspend fun getOperationsForUser(userId: String) = emptyList<PendingOperationEntity>()
        override fun observeOperationsForUser(userId: String): Flow<List<PendingOperationEntity>> = flowOf(emptyList())
        override suspend fun getOperationById(id: Long) = null
        override suspend fun upsert(operation: PendingOperationEntity) = 1L
        override suspend fun deleteOperation(id: Long) {}
        override suspend fun deleteOperationsForUser(userId: String) {}
        override suspend fun deleteOperationByTypeAndEntity(userId: String, type: String, entityId: String) {}
        override suspend fun updateStatus(id: Long, status: String, attemptCount: Int, lastError: String?, updatedAt: Long) {}
    }

    private class FakeSyncScheduler : SyncScheduler(null, FakeConnectivityMonitor()) {
        override fun triggerSync() {}
    }

    private class FakeApiService : ApiService {
        override suspend fun health(): Response<Unit> = Response.success(Unit)
        override suspend fun register(request: RegisterRequest): Envelope<AuthResult> = TODO()
        override suspend fun login(request: LoginRequest): Envelope<AuthResult> = TODO()
        override suspend fun logout(request: LogoutRequest): Response<Unit> = Response.success(Unit)
        override suspend fun getMe(): Envelope<UserDto> = TODO()
        override suspend fun updateMe(request: UpdateMeRequest): Envelope<UserDto> = TODO()
        override suspend fun updateMePut(request: UpdateMeRequest): Envelope<UserDto> = TODO()
        override suspend fun updateMeUsersPatch(request: UpdateMeRequest): Envelope<UserDto> = TODO()
        override suspend fun updateMeUsersPut(request: UpdateMeRequest): Envelope<UserDto> = TODO()
        override suspend fun postOnboarding(request: OnboardingRequest): Envelope<UserDto> = TODO()
        override suspend fun getMyCreations(page: Int?, pageSize: Int?): Envelope<List<CreationSummaryDto>> = TODO()
        override suspend fun getCategories(): Envelope<List<CategoryDto>> = TODO()
        override suspend fun getCreations(page: Int?, pageSize: Int?, categoryId: Int?, search: String?, sort: String?): Envelope<List<CreationSummaryDto>> = TODO()
        override suspend fun getCreationDetail(id: String): Envelope<CreationDetailDto> {
            return Envelope(
                data = CreationDetailDto(
                    id = id,
                    artisanId = "a1",
                    categoryId = 1,
                    title = "Test Creation",
                    isLikedByMe = false,
                    isOwnCreation = false
                )
            )
        }
        override suspend fun createCreation(request: CreationRequest): Envelope<CreationDetailDto> = TODO()
        override suspend fun updateCreation(id: String, request: CreationRequest): Envelope<CreationDetailDto> = TODO()
        override suspend fun deleteCreation(id: String): Response<Unit> = Response.success(Unit)
        override suspend fun viewCreation(id: String): Response<Unit> = Response.success(Unit)
        override suspend fun likeCreation(id: String): Envelope<LikeResult> = TODO()
        override suspend fun unlikeCreation(id: String): Envelope<LikeResult> = TODO()
        override suspend fun updateAvailability(id: String, request: AvailabilityRequest): Envelope<CreationDetailDto> = TODO()
        override suspend fun sendInquiry(id: String, request: InquiryRequest): Envelope<InquiryDto> = TODO()
        override suspend fun getArtisanProfile(id: String): Envelope<ArtisanReadProfileDto> = TODO()
        override suspend fun getArtisanCreations(id: String): Envelope<List<CreationSummaryDto>> = TODO()
        override suspend fun openGallery(request: OpenGalleryRequest): Envelope<ArtisanReadProfileDto> = TODO()
        override suspend fun updateArtisanProfile(request: ArtisanWriteProfileDto): Envelope<ArtisanReadProfileDto> = TODO()
        override suspend fun getUploadSignature(request: SignatureRequest): Envelope<UploadSignatureDto> = TODO()
    }

    private class FakeConnectivityMonitor : ConnectivityMonitor(null) {
        override val isOnline: StateFlow<Boolean> = MutableStateFlow(true)
    }

    @Test
    fun onLike_guestUser_showsSignInPrompt() = runBlocking {
        val tokenStorage = FakeTokenStorage()
        val sessionManager = SessionManager(tokenStorage)
        sessionManager.setUser(null)

        val savedStateHandle = SavedStateHandle(mapOf("id" to "c1"))
        val creationRepository = CreationRepository(
            context = null,
            apiService = FakeApiService(),
            creationDao = FakeCreationDao(),
            pendingOperationDao = FakePendingOperationDao(),
            sessionManager = sessionManager,
            syncScheduler = FakeSyncScheduler()
        )

        val viewModel = CreationDetailViewModel(
            savedStateHandle = savedStateHandle,
            creationRepository = creationRepository,
            sessionManager = sessionManager,
            whatsappSharer = WhatsAppSharer(null),
            connectivityMonitor = FakeConnectivityMonitor()
        )

        viewModel.onLike()

        assertEquals(PendingAction.Like, viewModel.uiState.value.showSignInPrompt)
        assertEquals(Pair(PendingAction.Like, "c1"), sessionManager.pendingAction.value)
    }
}
