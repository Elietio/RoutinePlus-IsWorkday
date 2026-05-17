package xyz.elietio.routineplus.isworkday.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import xyz.elietio.routineplus.isworkday.data.remote.dto.HolidayYearResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun fetchHolidayData(baseUrl: String, year: Int): HolidayYearResponse {
        return httpClient.get("${baseUrl.trimEnd('/')}/$year.json").body()
    }
}
