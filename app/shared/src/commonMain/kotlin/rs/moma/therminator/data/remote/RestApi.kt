package rs.moma.therminator.data.remote

import io.ktor.client.request.forms.MultiPartFormDataContent
import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.FileItem
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.Headers

class RestApi(private val client: HttpClient) {
    val baseUrl = "https://therminator.moma.rs"

    suspend fun ping(): ResponseStatus = try {
        ResponseStatus.from(client.get(baseUrl).status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun updateCameraSettings(cameraSettings: CameraSettings): ResponseStatus = try {
        ResponseStatus.from(client.post("$baseUrl/settings/camera") {
            contentType(ContentType.Application.Json)
            setBody(cameraSettings)
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun runTest(): ResponseStatus = try {
        ResponseStatus.from(client.get("$baseUrl/test").status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun getCameraSettings(): CameraSettings = client.get("$baseUrl/settings/camera").body()

    suspend fun getFileItems(): List<FileItem> = client.get("$baseUrl/files").body()

    suspend fun createFolder(path: String): ResponseStatus = try {
        ResponseStatus.from(client.post("$baseUrl/files/folder") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("path" to path))
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun uploadTracks(path: String, files: List<Pair<String, ByteArray>>): ResponseStatus = try {
        ResponseStatus.from(client.post("$baseUrl/files/track") {
            setBody(
                MultiPartFormDataContent(
                formData {
                    append("path", path)
                    files.forEach { (fileName, fileData) ->
                        append("tracks", fileData, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        })
                    }
                }
            ))
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun deleteFiles(paths: List<String>): ResponseStatus = try {
        ResponseStatus.from(client.delete("$baseUrl/files") {
            contentType(ContentType.Application.Json)
            setBody(paths)
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }
}