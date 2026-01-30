package rs.moma.therminator.data.remote

import rs.moma.therminator.data.models.ResponseStatus.*
import rs.moma.therminator.data.models.ResponseStatus
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.DefaultRequest
import org.koin.core.component.KoinComponent
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import io.ktor.client.request.header
import kotlinx.coroutines.launch
import io.ktor.client.*

class HttpClientManager : KoinComponent {
    private var scope: CoroutineScope? = null

    fun setScope(scope: CoroutineScope) {
        this.scope = scope
    }

    fun getHttpClient(): HttpClient = HttpClient {
        install(WebSockets)

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(DefaultRequest) {
            AuthProvider.getHeaderValue()?.let { header("Auth", it) }
        }

        HttpResponseValidator {
            validateResponse { response ->
                scope?.launch {
                    NetworkResponseService.emit(ResponseStatus.from(response.status))
                }
            }

            handleResponseException {
                scope?.launch {
                    NetworkResponseService.emit(Error)
                }
            }
        }
    }
}