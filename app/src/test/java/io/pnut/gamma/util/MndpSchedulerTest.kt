package io.pnut.gamma.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

class MndpSchedulerTest {

    @Test
    fun calculateNextOccurrence_whenMondayBefore815PM_returnsSameDayAt815PM() {
        val now = ZonedDateTime.of(2026, 3, 30, 12, 0, 0, 0, MndpScheduler.CENTRAL_ZONE)
        assertThat(now.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)

        val next = MndpScheduler.calculateNextOccurrence(now)

        assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(next.toLocalDate()).isEqualTo(now.toLocalDate())
        assertThat(next.toLocalTime()).isEqualTo(LocalTime.of(20, 15))
    }

    @Test
    fun calculateNextOccurrence_whenMondayAt815PMExact_returnsNextWeekMondayAt815PM() {
        val now = ZonedDateTime.of(2026, 3, 30, 20, 15, 0, 0, MndpScheduler.CENTRAL_ZONE)
        assertThat(now.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)

        val next = MndpScheduler.calculateNextOccurrence(now)

        assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(next.toLocalDate()).isEqualTo(now.toLocalDate().plusDays(7))
        assertThat(next.toLocalTime()).isEqualTo(LocalTime.of(20, 15))
    }

    @Test
    fun calculateNextOccurrence_whenMondayAfter815PM_returnsNextWeekMondayAt815PM() {
        val now = ZonedDateTime.of(2026, 3, 30, 21, 0, 0, 0, MndpScheduler.CENTRAL_ZONE)
        assertThat(now.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)

        val next = MndpScheduler.calculateNextOccurrence(now)

        assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(next.toLocalDate()).isEqualTo(now.toLocalDate().plusDays(7))
        assertThat(next.toLocalTime()).isEqualTo(LocalTime.of(20, 15))
    }

    @Test
    fun calculateNextOccurrence_whenTuesday_returnsComingMondayAt815PM() {
        val now = ZonedDateTime.of(2026, 3, 31, 10, 0, 0, 0, MndpScheduler.CENTRAL_ZONE)
        assertThat(now.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)

        val next = MndpScheduler.calculateNextOccurrence(now)

        assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(next.toLocalDate()).isEqualTo(now.toLocalDate().plusDays(6))
        assertThat(next.toLocalTime()).isEqualTo(LocalTime.of(20, 15))
    }

    @Test
    fun calculateNextOccurrence_whenSunday_returnsTomorrowMondayAt815PM() {
        val now = ZonedDateTime.of(2026, 4, 5, 23, 0, 0, 0, MndpScheduler.CENTRAL_ZONE)
        assertThat(now.dayOfWeek).isEqualTo(DayOfWeek.SUNDAY)

        val next = MndpScheduler.calculateNextOccurrence(now)

        assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(next.toLocalDate()).isEqualTo(now.toLocalDate().plusDays(1))
        assertThat(next.toLocalTime()).isEqualTo(LocalTime.of(20, 15))
    }
}
