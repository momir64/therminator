package rs.moma.therminator.data.remote

import rs.moma.therminator.data.models.ResponseStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object NetworkResponseService {
    private val _response = MutableSharedFlow<ResponseStatus>()
    val response: SharedFlow<ResponseStatus> = _response

    suspend fun emit(responseStatus: ResponseStatus) {
        _response.emit(responseStatus)
    }
}