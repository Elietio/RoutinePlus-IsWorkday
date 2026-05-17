package xyz.elietio.routineplus.isworkday.domain.usecase

import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import xyz.elietio.routineplus.isworkday.domain.model.DayType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class CheckDayTypeUseCase @Inject constructor(
    private val repository: HolidayRepository
) {
    private val chinaZone: ZoneId = ZoneId.of("Asia/Shanghai")

    suspend operator fun invoke(targetOffset: Int): DayType {
        return checkDate(LocalDate.now(chinaZone).plusDays(targetOffset.toLong()))
    }

    suspend fun checkDate(date: LocalDate): DayType {
        val record = repository.getDayByDate(date.toString())
        return when {
            record != null && record.isOffDay -> DayType.OFFDAY
            record != null && !record.isOffDay -> DayType.WORKDAY
            date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> DayType.OFFDAY
            else -> DayType.WORKDAY
        }
    }
}
