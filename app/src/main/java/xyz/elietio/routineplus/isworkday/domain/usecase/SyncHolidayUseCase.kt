package xyz.elietio.routineplus.isworkday.domain.usecase

import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import javax.inject.Inject

class SyncHolidayUseCase @Inject constructor(
    private val repository: HolidayRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncCurrentAndNextYear()
    }

    suspend fun syncYear(year: Int): Result<Unit> {
        return repository.syncYear(year)
    }
}
