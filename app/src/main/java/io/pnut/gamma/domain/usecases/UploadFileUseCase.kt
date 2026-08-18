package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.FileBody
import io.pnut.gamma.domain.entity.raw.replacement.PostOEmbed
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.domain.model.io.UploadFileOutputData
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.util.ErrorCollections
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class UploadFileUseCase(private val pnutRepository: IPnutRepository) :
    UseCase<UploadFileOutputData, UploadFileInputData>() {
    override suspend fun run(params: UploadFileInputData): UploadFileOutputData {
        val bytes = params.inputStream?.readBytes() ?: throw ErrorCollections.CannotLoadFile()
        val fileName = params.fileName ?: File(params.uriInfo.uri.path ?: "upload.jpg").name
        val content = bytes.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val res = pnutRepository.createFile(
            content,
            FileBody(
                io.pnut.gamma.domain.entity.File.FileKind.IMAGE, // TODO: Fix it
                fileName
            )
        )
        val oEmbedRaw = PostOEmbed(
            PostOEmbed.FileValue(
                res.data.id,
                res.data.fileToken ?: ""
            )
        )
        return UploadFileOutputData(oEmbedRaw)

    }
}