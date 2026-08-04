package io.pnut.gamma.domain.usecases

import android.net.Uri
import io.pnut.gamma.domain.entity.File
import io.pnut.gamma.domain.entity.FileBody
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Clients
import io.pnut.gamma.sample.Users
import io.pnut.gamma.util.Constants
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.util.*
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
class UploadFileUseCaseTest {
  private val dummyUri = Uri.parse("dummyPath")

  @Test
  fun upload() {
    val file = File(
      createdAt = Date(),
      id = "1",
      isComplete = true,
      isPublic = true,
      kind = File.FileKind.OTHER,
      name = "testFile.txt",
      sha256 = "sha256",
      size = 1,
      source = Clients.testClient,
      type = Constants.GAMMA,
      user = Users.me,
      fileToken = "fileToken"
    )
    val useCase = UploadFileUseCase(object : PnutRepositoryMock() {
      override fun createFile(content: RequestBody, fileBody: FileBody): PnutResponse<File> {
        val buffer = Buffer()
        content.writeTo(buffer)
        // TODO: Fix it
//        assertThat(fileBody.kind).isEqualTo(File.FileKind.OTHER)
        assertThat(fileBody.name).isEqualTo("dummyPath")
        assertThat(buffer.readUtf8()).isEqualTo("test utf8 data")
        return PnutResponse(PnutResponse.Meta(200), file)
      }
    })
    val inputStream = ByteArrayInputStream("test utf8 data".toByteArray())
    val res = useCase.run(UploadFileInputData(UriInfo(dummyUri), inputStream))
    assertThat(res.postOEmbedRaw.replacementFileValue.fileId).isEqualTo(file.id)
    assertThat(res.postOEmbedRaw.replacementFileValue.fileToken).isEqualTo(file.fileToken)
  }
}
