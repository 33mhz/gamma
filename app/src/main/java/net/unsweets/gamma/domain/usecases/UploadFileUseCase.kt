package net.unsweets.gamma.domain.usecases

import net.unsweets.gamma.domain.entity.FileBody
import net.unsweets.gamma.domain.entity.raw.replacement.PostOEmbed
import net.unsweets.gamma.domain.model.io.UploadFileInputData
import net.unsweets.gamma.domain.model.io.UploadFileOutputData
import net.unsweets.gamma.domain.repository.IPnutRepository
import net.unsweets.gamma.util.ErrorCollections
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class UploadFileUseCase(private val pnutRepository: IPnutRepository) :
    UseCase<UploadFileOutputData, UploadFileInputData>() {
    override fun run(params: UploadFileInputData): UploadFileOutputData {
        val bytes = params.inputStream?.readBytes() ?: throw ErrorCollections.CannotLoadFile
        // When it was shared from another app, cannot get filename correctly in sometimes.
        val file = File(params.uriInfo.uri.path)
        val content = bytes.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val res = pnutRepository.createFile(
            content,
            FileBody(
              net.unsweets.gamma.domain.entity.File.FileKind.IMAGE, // TODO: Fix it
                file.name
            )
        )
        val oEmbedRaw = PostOEmbed(
            PostOEmbed.OEmbedRawValue(
                PostOEmbed.OEmbedRawValue.FileValue(
                    res.data.id,
                    res.data.fileToken ?: ""
                )
            )
        )
        return UploadFileOutputData(oEmbedRaw)

    }
}