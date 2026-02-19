package rs.moma.therminator.data.remote

import io.ktor.client.request.forms.MultiPartFormDataContent
import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.LocationInfo
import rs.moma.therminator.data.models.DisplayInfo
import rs.moma.therminator.data.models.AlarmInfo
import rs.moma.therminator.data.models.FileItem
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.HttpMethod
import io.ktor.http.Headers

class RestApi(private val client: HttpClient) {
    val baseUrl = "https://therminator.moma.rs"

    private suspend inline fun <reified T> requestJson(method: HttpMethod, endpoint: String, body: T): ResponseStatus = try {
        ResponseStatus.from(client.request("$baseUrl$endpoint") {
            this.method = method
            contentType(ContentType.Application.Json)
            setBody(body)
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    private suspend inline fun <reified T> getBody(endpoint: String, default: T): T = try {
        client.get("$baseUrl$endpoint").body()
    } catch (_: Throwable) {
        default
    }

    private suspend fun get(endpoint: String = ""): ResponseStatus = try {
        ResponseStatus.from(client.get("$baseUrl$endpoint").status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    private suspend inline fun <reified T> post(endpoint: String, body: T) = requestJson(HttpMethod.Post, endpoint, body)
    private suspend inline fun <reified T> delete(endpoint: String, body: T) = requestJson(HttpMethod.Delete, endpoint, body)

    suspend fun ping(): ResponseStatus = get()

    suspend fun getCameraSettings(): CameraSettings? = getBody("/settings/camera", null)
    suspend fun updateCameraSettings(cameraSettings: CameraSettings) = post("/settings/camera", cameraSettings)
    suspend fun getFileItems(): List<FileItem> = getBody("/files", emptyList())
    suspend fun createFolder(path: String): ResponseStatus = post("/files/folder", mapOf("path" to path))
    suspend fun deleteFiles(paths: List<String>): ResponseStatus = delete("/files", paths)

    suspend fun uploadTracks(path: String, files: List<Pair<String, ByteArray>>): ResponseStatus = try {
        ResponseStatus.from(client.post("/files/track") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("path", path)
                        files.forEach { (fileName, fileData) ->
                            append("tracks", fileData, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    }
                ))
        }.status)
    } catch (_: Throwable) {
        ResponseStatus.Error
    }

    suspend fun getAlarms(): List<AlarmInfo> = getBody("/alarms", emptyList())
    suspend fun testAlarm(alarm: AlarmInfo): ResponseStatus = post("/alarms/test", alarm)
    suspend fun updateAlarm(alarm: AlarmInfo): ResponseStatus = post("/alarms", alarm)
    suspend fun deleteAlarm(alarm: AlarmInfo): ResponseStatus = delete("/alarms", alarm)

    suspend fun geocodeLocation(address: String): LocationInfo = getBody("/weather/geocode/$address", LocationInfo())
    suspend fun updateLocation(location: LocationInfo): ResponseStatus = post("/weather", location)
    suspend fun getLocation(): LocationInfo = getBody("/weather", LocationInfo())

    suspend fun updateDisplay(display: DisplayInfo): ResponseStatus = post("/display", display)
    suspend fun getDisplay(): DisplayInfo? = getBody("/display", null)
}