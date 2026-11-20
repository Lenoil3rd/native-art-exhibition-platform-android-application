package com.example.naepapp

import com.example.naepapp.core.db.dao.ArtisanDao
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.dao.PendingOperationDao
import com.example.naepapp.core.db.dao.UserDao
import com.example.naepapp.core.db.entity.ArtisanEntity
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.entity.CreationMediaEntity
import com.example.naepapp.core.db.entity.CurrentUserEntity
import com.example.naepapp.core.db.entity.PendingOperationEntity
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.repository.AuthRepository
import com.example.naepapp.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    private class FakeTokenStorage : TokenStorage {
        var accessTok: String? = null
        var refreshTok: String? = null

        override suspend fun save(accessToken: String, refreshToken: String) {
            accessTok = accessToken
            refreshTok = refreshToken
        }

        override suspend fun access(): String? = accessTok
        override suspend fun refresh(): String? = refreshTok

        override suspend fun clear() {
            accessTok = null
            refreshTok = null
        }
    }

    private class FakeUserDao : UserDao {
        private var user: CurrentUserEntity? = null
        private val _flow = MutableStateFlow<CurrentUserEntity?>(null)

        override fun observeCurrentUser(): Flow<CurrentUserEntity?> = _flow

        override suspend fun getCurrentUser(): CurrentUserEntity? = user

        override suspend fun upsert(user: CurrentUserEntity) {
            this.user = user
            _flow.value = user
        }

        override suspend fun clear() {
            this.user = null
            _flow.value = null
        }
    }

    private class FakeCreationDao : CreationDao {
        override fun observeById(id: String) = TODO()
        override suspend fun getById(id: String) = null
        override fun observeMediaForCreation(creationId: String) = TODO()
        override suspend fun getMediaForCreation(creationId: String) = emptyList<CreationMediaEntity>()
        override fun observeMyCreations(artisanId: String) = TODO()
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

    private class FakeArtisanDao : ArtisanDao {
        override fun observeById(userId: String) = TODO()
        override suspend fun getById(userId: String) = null
        override suspend fun upsert(artisan: ArtisanEntity) {}
        override suspend fun clearAllWhatsappNumbers() {}
    }

    private class FakeApiService(private val userDto: UserDto) : ApiService {
        override suspend fun health(): Response<Unit> = Response.success(Unit)

        override suspend fun login(request: LoginRequest): Envelope<AuthResult> {
            return Envelope(
                data = AuthResult(
                    user = userDto,
                    accessToken = "fake_access",
                    refreshToken = "fake_refresh",
                    tokenType = "Bearer",
                    expiresIn = 3600
                )
            )
        }
        override suspend fun register(request: RegisterRequest) = TODO()
        override suspend fun logout(request: LogoutRequest): Response<Unit> = Response.success(Unit)
        override suspend fun getMe(): Envelope<UserDto> = Envelope(data = userDto)
        override suspend fun updateMe(request: UpdateMeRequest) = TODO()
        override suspend fun postOnboarding(request: OnboardingRequest) = TODO()
        override suspend fun getMyCreations(page: Int?, pageSize: Int?): Envelope<List<CreationSummaryDto>> = TODO()
        override suspend fun getCategories() = TODO()
        override suspend fun getCreations(page: Int?, pageSize: Int?, categoryId: Int?, search: String?, sort: String?): Envelope<List<CreationSummaryDto>> = TODO()
        override suspend fun getCreationDetail(id: String) = TODO()
        override suspend fun createCreation(request: CreationRequest) = TODO()
        override suspend fun updateCreation(id: String, request: CreationRequest) = TODO()
        override suspend fun deleteCreation(id: String): Response<Unit> = Response.success(Unit)
        override suspend fun viewCreation(id: String): Response<Unit> = Response.success(Unit)
        override suspend fun likeCreation(id: String) = TODO()
        override suspend fun unlikeCreation(id: String) = TODO()
        override suspend fun updateAvailability(id: String, request: AvailabilityRequest) = TODO()
        override suspend fun sendInquiry(id: String, request: InquiryRequest) = TODO()
        override suspend fun getArtisanProfile(id: String) = TODO()
        override suspend fun getArtisanCreations(id: String) = TODO()
        override suspend fun openGallery(request: OpenGalleryRequest) = TODO()
        override suspend fun updateArtisanProfile(request: ArtisanWriteProfileDto) = TODO()
        override suspend fun getUploadSignature(request: SignatureRequest) = TODO()
    }

    @Test
    fun login_savesTokensAndUpdatesSession() = runBlocking {
        val testUser = UserDto(
            id = "u1",
            fullName = "Test User",
            email = "test@example.com",
            phoneNumber = "+265990000000"
        )
        val fakeTokenStorage = FakeTokenStorage()
        val sessionManager = SessionManager(fakeTokenStorage)
        val fakeUserDao = FakeUserDao()
        val fakeCreationDao = FakeCreationDao()
        val fakeArtisanDao = FakeArtisanDao()
        val fakePendingDao = FakePendingOperationDao()
        val fakeApi = FakeApiService(testUser)

        val repository = AuthRepository(
            context = null,
            apiService = fakeApi,
            tokenStorage = fakeTokenStorage,
            sessionManager = sessionManager,
            userDao = fakeUserDao,
            creationDao = fakeCreationDao,
            artisanDao = fakeArtisanDao,
            pendingOperationDao = fakePendingDao
        )

        val result = repository.login("test@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
        assertEquals("fake_access", fakeTokenStorage.access())
        assertEquals("fake_refresh", fakeTokenStorage.refresh())
        assertEquals(testUser, sessionManager.user.value)
        assertEquals("Test User", fakeUserDao.getCurrentUser()?.fullName)
    }
}
