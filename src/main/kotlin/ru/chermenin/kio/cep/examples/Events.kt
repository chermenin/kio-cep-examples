package ru.chermenin.kio.cep.examples

import java.io.Serializable
import org.joda.time.LocalDateTime

open class Event(open val timestamp: LocalDateTime) : Serializable

data class SmokeEvent(override val timestamp: LocalDateTime) : Event(timestamp)

data class TemperatureEvent(override val timestamp: LocalDateTime, val value: Int) : Event(timestamp)

data class TemperatureWarning(override val timestamp: LocalDateTime, val value: Int) : Event(timestamp)

data class FireAlert(override val timestamp: LocalDateTime) : Event(timestamp)
