package me.muriloscolari.ciclo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SleepCycleCalculatorTest {

    private val defaultSettings = SleepSettings(
        cycleMinutes = 90,
        fallAsleepMinutes = 15,
        minCycles = 3,
        maxCycles = 6,
        recommendedCycles = 5,
        maxSleepMinutes = 600
    )

    @Test
    fun wakeTimesFor_standardInputs_calculatesCorrectTimes() {
        // Deitar às 23:00 de hoje
        val bedTime = LocalDateTime.of(2026, 10, 23, 23, 0)
        val options = SleepCycleCalculator.wakeTimesFor(bedTime, defaultSettings)

        assertEquals(4, options.size) // 3, 4, 5, 6 ciclos

        // 3 ciclos: 3 * 90m = 270m + 15m = 285m (4h45) -> 03:45 do dia seguinte
        val opt3 = options.first { it.cycles == 3 }
        assertEquals(LocalDateTime.of(2026, 10, 24, 3, 45), opt3.wakeTime)
        assertFalse(opt3.isRecommended)

        // 4 ciclos: 4 * 90m = 360m + 15m = 375m (6h15) -> 05:15 do dia seguinte
        val opt4 = options.first { it.cycles == 4 }
        assertEquals(LocalDateTime.of(2026, 10, 24, 5, 15), opt4.wakeTime)
        assertFalse(opt4.isRecommended)

        // 5 ciclos: 5 * 90m = 450m + 15m = 465m (7h45) -> 06:45 do dia seguinte
        val opt5 = options.first { it.cycles == 5 }
        assertEquals(LocalDateTime.of(2026, 10, 24, 6, 45), opt5.wakeTime)
        assertTrue("5 ciclos deve ser recomendado por padrão", opt5.isRecommended)

        // 6 ciclos: 6 * 90m = 540m + 15m = 555m (9h15) -> 08:15 do dia seguinte
        val opt6 = options.first { it.cycles == 6 }
        assertEquals(LocalDateTime.of(2026, 10, 24, 8, 15), opt6.wakeTime)
        assertFalse(opt6.isRecommended)
    }

    @Test
    fun wakeTimesFor_with120MinuteCycles_respectsMaxSleepCap() {
        val settings120 = defaultSettings.copy(cycleMinutes = 120, maxSleepMinutes = 600)
        val bedTime = LocalDateTime.of(2026, 10, 23, 22, 0)
        val options = SleepCycleCalculator.wakeTimesFor(bedTime, settings120)

        // Ciclos avaliados:
        // 3 ciclos = 360m (6h) <= 600m OK
        // 4 ciclos = 480m (8h) <= 600m OK
        // 5 ciclos = 600m (10h) <= 600m OK
        // 6 ciclos = 720m (12h) > 600m -> FILTRADO!
        assertEquals(3, options.size)
        assertTrue(options.none { it.cycles == 6 })
        assertTrue(options.any { it.cycles == 5 && it.isRecommended })
    }

    @Test
    fun bedTimesFor_earlyEvening_recommends5Cycles() {
        // Deseja acordar amanhã às 07:00. Agora são 20:00.
        val now = LocalDateTime.of(2026, 10, 23, 20, 0)
        val wakeTime = LocalDateTime.of(2026, 10, 24, 7, 0)

        val result = SleepCycleCalculator.bedTimesFor(wakeTime, now, defaultSettings)

        assertTrue(result.hasAnyAvailable)
        assertEquals(4, result.options.size)

        // 5 ciclos: 07:00 - 7h45 = 23:15
        val opt5 = result.options.first { it.cycles == 5 }
        assertEquals(LocalDateTime.of(2026, 10, 23, 23, 15), opt5.bedTime)
        assertTrue(opt5.isAvailable)
        assertTrue(opt5.isRecommended)
    }

    @Test
    fun bedTimesFor_lateNight_recommendsHighestViableCycle() {
        // Deseja acordar às 07:00. Agora já são 01:00 da madrugada.
        // Horários calculados para deitar:
        // 6 ciclos: 21:45 (já passou -> indisponível)
        // 5 ciclos: 23:15 (já passou -> indisponível)
        // 4 ciclos: 00:45 (já passou -> indisponível)
        // 3 ciclos: 02:15 (no futuro -> disponível!)
        val now = LocalDateTime.of(2026, 10, 24, 1, 0)
        val wakeTime = LocalDateTime.of(2026, 10, 24, 7, 0)

        val result = SleepCycleCalculator.bedTimesFor(wakeTime, now, defaultSettings)

        assertTrue(result.hasAnyAvailable)
        val opt6 = result.options.first { it.cycles == 6 }
        assertFalse(opt6.isAvailable)

        val opt5 = result.options.first { it.cycles == 5 }
        assertFalse(opt5.isAvailable)

        val opt4 = result.options.first { it.cycles == 4 }
        assertFalse(opt4.isAvailable)

        val opt3 = result.options.first { it.cycles == 3 }
        assertTrue(opt3.isAvailable)
        assertTrue("Como é o único viável, 3 ciclos deve ser recomendado", opt3.isRecommended)
    }

    @Test
    fun bedTimesFor_veryLateNight_fallsBackTo1Or2Cycles() {
        // Deseja acordar às 07:00. Agora são 04:30 da madrugada.
        // 3 ciclos (02:15) já passou.
        // 2 ciclos: 07:00 - (180+15)m = 03:45 (já passou).
        // 1 ciclo: 07:00 - (90+15)m = 05:15 (viável!).
        val now = LocalDateTime.of(2026, 10, 24, 4, 30)
        val wakeTime = LocalDateTime.of(2026, 10, 24, 7, 0)

        val result = SleepCycleCalculator.bedTimesFor(wakeTime, now, defaultSettings)

        assertTrue(result.hasAnyAvailable)
        assertEquals(1, result.options.size)
        val opt1 = result.options.first()
        assertEquals(1, opt1.cycles)
        assertEquals(LocalDateTime.of(2026, 10, 24, 5, 15), opt1.bedTime)
        assertTrue(opt1.isAvailable)
        assertTrue(opt1.isRecommended)
    }

    @Test
    fun bedTimesFor_tooCloseToWakeTime_returnsNoAvailableOptions() {
        // Deseja acordar às 07:00. Agora são 06:15 da manhã.
        // Nem 1 ciclo de 90m + 15m é viável.
        val now = LocalDateTime.of(2026, 10, 24, 6, 15)
        val wakeTime = LocalDateTime.of(2026, 10, 24, 7, 0)

        val result = SleepCycleCalculator.bedTimesFor(wakeTime, now, defaultSettings)

        assertFalse("Não deve haver opções disponíveis", result.hasAnyAvailable)
        assertTrue(result.options.all { !it.isAvailable })
    }
}
