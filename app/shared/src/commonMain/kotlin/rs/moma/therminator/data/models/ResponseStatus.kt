package rs.moma.therminator.data.models

import io.ktor.http.HttpStatusCode

enum class ResponseStatus {
    Unauthorized,
    Successful,
    Error;

    companion object {
        fun from(status: HttpStatusCode): ResponseStatus = when (status.value) {
            in 200..299, 101 -> Successful
            401 -> Unauthorized
            else -> Error
        }
    }
}
