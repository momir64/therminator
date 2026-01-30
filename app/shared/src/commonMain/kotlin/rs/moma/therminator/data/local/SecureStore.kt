package rs.moma.therminator.data.local

interface SecureStore {
    suspend fun save(password: String)
    suspend fun load(): String?
    suspend fun clear()
}
