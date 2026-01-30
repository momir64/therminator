package rs.moma.therminator.data.remote

import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.data.models.ResponseStatus
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RestApi(private val client: HttpClient) {
    val baseUrl = "http://192.168.0.100:200"

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
}