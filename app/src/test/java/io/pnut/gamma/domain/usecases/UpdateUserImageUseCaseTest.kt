package io.pnut.gamma.domain.usecases

import android.net.Uri
import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.io.UpdateUserImageInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.Response
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Users
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
class UpdateUserImageUseCaseTest {
    private val me = Users.me
    private val dummyUri = Uri.parse("dummy")

    @Test
    fun succeedUpdateAvatar() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun updateAvatar(uri: Uri): PnutResponse<User> {
                val user =
                    me.copy(content = me.content.copy(avatarImage = me.content.avatarImage.copy(url = "updated")))
                return Response.success(user)
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        val res = runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    dummyUri,
                    UpdateUserImageInputData.Type.Avatar
                )
            )
        }
        assertThat(res.res.data.content.avatarImage.url).isEqualTo("updated")
    }

    @Test(expected = TestException::class)
    fun failToUpdateAvatar() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun updateAvatar(uri: Uri): PnutResponse<User> {
                throw TestException()
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    dummyUri,
                    UpdateUserImageInputData.Type.Avatar
                )
            )
        }
    }

    @Test
    fun succeedUpdateCover() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun updateCover(uri: Uri): PnutResponse<User> {
                val user =
                    me.copy(content = me.content.copy(coverImage = me.content.coverImage.copy(url = "updated")))
                return Response.success(user)
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        val res = runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    dummyUri,
                    UpdateUserImageInputData.Type.Cover
                )
            )
        }
        assertThat(res.res.data.content.coverImage.url).isEqualTo("updated")
    }

    @Test(expected = TestException::class)
    fun failToUpdateCover() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun updateCover(uri: Uri): PnutResponse<User> {
                throw TestException()
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    dummyUri,
                    UpdateUserImageInputData.Type.Cover
                )
            )
        }
    }

    @Test
    fun succeedToDeleteAvatar() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun deleteAvatar(): PnutResponse<User> {
                val user =
                    me.copy(
                        content = me.content.copy(
                            avatarImage = me.content.avatarImage.copy(
                                url = "deleted",
                                isDefault = true
                            )
                        )
                    )
                return Response.success(user)
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        val res = runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    null,
                    UpdateUserImageInputData.Type.Avatar
                )
            )
        }
        assertThat(res.res.data.content.avatarImage.isDefault).isTrue()
    }

    @Test
    fun succeedToDeleteCover() {
        val pnutRepository = object : PnutRepositoryMock() {
            override suspend fun deleteCover(): PnutResponse<User> {
                val user =
                    me.copy(
                        content = me.content.copy(
                            coverImage = me.content.coverImage.copy(
                                url = "deleted",
                                isDefault = true
                            )
                        )
                    )
                return Response.success(user)
            }
        }
        val updateUserImageUseCase = UpdateUserImageUseCase(pnutRepository)
        val res = runBlocking {
            updateUserImageUseCase.run(
                UpdateUserImageInputData(
                    null,
                    UpdateUserImageInputData.Type.Cover
                )
            )
        }
        assertThat(res.res.data.content.coverImage.isDefault).isTrue()
    }

}