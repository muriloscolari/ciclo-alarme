package me.muriloscolari.ciclo.domain

import java.time.Duration
import java.time.LocalDateTime

/**
 * Configurações de ciclos de sono.
 */
data class SleepSettings(
    val cycleMinutes: Int = 90,
    val fallAsleepMinutes: Int = 15,
    val minCycles: Int = 3,
    val maxCycles: Int = 6,
    val recommendedCycles: Int = 5,
    val maxSleepMinutes: Int = 600 // 10 horas
)

enum class SleepMode {
    NOW,
    BEDTIME,
    WAKE_AT
}

/**
 * Opção individual de horário calculada.
 */
data class SleepOption(
    val cycles: Int,
    val bedTime: LocalDateTime,
    val wakeTime: LocalDateTime,
    val sleepDuration: Duration,
    val isRecommended: Boolean,
    val isAvailable: Boolean = true
)

/**
 * Resultado completo do modo "Acordar às X".
 */
data class WakeAtResult(
    val wakeTime: LocalDateTime,
    val options: List<SleepOption>,
    val hasAnyAvailable: Boolean
)

object SleepCycleCalculator {

    /**
     * Calcula os horários de acordar partindo de uma hora de deitar [bedTime].
     * Utilizado para os modos "Dormir agora" e "Vou dormir às X".
     */
    fun wakeTimesFor(bedTime: LocalDateTime, settings: SleepSettings): List<SleepOption> {
        val validCycles = (settings.minCycles..settings.maxCycles)
            .filter { it * settings.cycleMinutes <= settings.maxSleepMinutes }

        if (validCycles.isEmpty()) return emptyList()

        val targetRecommended = if (settings.recommendedCycles in validCycles) {
            settings.recommendedCycles
        } else {
            validCycles.maxOrNull() ?: settings.recommendedCycles
        }

        return validCycles.map { n ->
            val sleepDuration = Duration.ofMinutes((n * settings.cycleMinutes).toLong())
            val wakeTime = bedTime
                .plusMinutes(settings.fallAsleepMinutes.toLong())
                .plus(sleepDuration)
                .withSecond(0)
                .withNano(0)

            SleepOption(
                cycles = n,
                bedTime = bedTime.withSecond(0).withNano(0),
                wakeTime = wakeTime,
                sleepDuration = sleepDuration,
                isRecommended = (n == targetRecommended),
                isAvailable = true
            )
        }
    }

    /**
     * Calcula os horários para deitar partindo de uma hora de despertar [wakeTime].
     * [now] é o momento atual para aferir se o horário de ir para a cama já passou.
     */
    fun bedTimesFor(
        wakeTime: LocalDateTime,
        now: LocalDateTime,
        settings: SleepSettings
    ): WakeAtResult {
        val standardCycles = (settings.maxCycles downTo settings.minCycles)
            .filter { it * settings.cycleMinutes <= settings.maxSleepMinutes }

        val cleanWakeTime = wakeTime.withSecond(0).withNano(0)
        val cleanNow = now.withSecond(0).withNano(0)

        fun createOption(n: Int, isRec: Boolean): SleepOption {
            val sleepDuration = Duration.ofMinutes((n * settings.cycleMinutes).toLong())
            val totalMinutes = (n * settings.cycleMinutes) + settings.fallAsleepMinutes
            val bedTime = cleanWakeTime.minusMinutes(totalMinutes.toLong())
            val isAvailable = bedTime.isAfter(cleanNow)

            return SleepOption(
                cycles = n,
                bedTime = bedTime,
                wakeTime = cleanWakeTime,
                sleepDuration = sleepDuration,
                isRecommended = isRec,
                isAvailable = isAvailable
            )
        }

        val standardOptions = standardCycles.map { n -> createOption(n, false) }
        val availableStandard = standardOptions.filter { it.isAvailable }

        return if (availableStandard.isNotEmpty()) {
            // Entre as disponíveis, a recomendada é a com maior número de ciclos até recommendedCycles
            val recommendedCandidate = availableStandard
                .filter { it.cycles <= settings.recommendedCycles }
                .maxByOrNull { it.cycles } ?: availableStandard.maxByOrNull { it.cycles }

            val finalOptions = standardOptions.map { opt ->
                opt.copy(isRecommended = (opt.cycles == recommendedCandidate?.cycles))
            }
            WakeAtResult(
                wakeTime = cleanWakeTime,
                options = finalOptions,
                hasAnyAvailable = true
            )
        } else {
            // Nenhuma opção padrão está no futuro. Tenta ciclos menores (2 e 1).
            val fallbackCycles = (2 downTo 1).filter { n ->
                val totalMinutes = (n * settings.cycleMinutes) + settings.fallAsleepMinutes
                val bedTime = cleanWakeTime.minusMinutes(totalMinutes.toLong())
                bedTime.isAfter(cleanNow)
            }

            if (fallbackCycles.isNotEmpty()) {
                val fallbackOptions = fallbackCycles.sorted().map { n -> createOption(n, false) }
                val highestFallback = fallbackOptions.maxByOrNull { it.cycles }
                val finalFallback = fallbackOptions.map { opt ->
                    opt.copy(isRecommended = (opt.cycles == highestFallback?.cycles))
                }
                WakeAtResult(
                    wakeTime = cleanWakeTime,
                    options = finalFallback,
                    hasAnyAvailable = true
                )
            } else {
                // Nem 1 ciclo é viável (já é muito perto da hora de acordar)
                // Retorna as opções padrão marcadas como indisponíveis para fins de visualização informativa
                WakeAtResult(
                    wakeTime = cleanWakeTime,
                    options = standardOptions,
                    hasAnyAvailable = false
                )
            }
        }
    }
}
