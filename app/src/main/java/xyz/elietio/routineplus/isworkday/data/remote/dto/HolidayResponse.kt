package xyz.elietio.routineplus.isworkday.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HolidayYearResponse(
    val year: Int,
    val papers: List<String>,
    val days: List<HolidayDayDto>
)

@Serializable
data class HolidayDayDto(
    val name: String,
    val date: String,
    val isOffDay: Boolean
)
