package id.ac.stmi.aplikasiutsgibran.util.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface pemantau status jaringan untuk aplikasi GEARA CRM.
 */
interface ConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Available,
        Unavailable,
        Losing,
        Lost
    }
}
