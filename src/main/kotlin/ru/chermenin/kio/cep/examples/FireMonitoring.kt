package ru.chermenin.kio.cep.examples

import org.apache.beam.sdk.coders.SerializableCoder
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import ru.chermenin.kio.Kio
import ru.chermenin.kio.cep.match
import ru.chermenin.kio.cep.pattern.Pattern
import ru.chermenin.kio.functions.*
import kotlin.random.Random

object FireMonitoring {

    private const val TemperatureThreshold = 50

    @JvmStatic
    fun main(args: Array<String>) {
        val kio = Kio.fromArguments(args)
        val initTimestamp = LocalDateTime.now().minus(Duration.standardDays(1))

        val events = kio.parallelize(
                (0..100).map {
                    0.4 * it + Random.nextDouble()
                }.map {
                    val timestamp = initTimestamp.plus(Duration.standardSeconds((it * 100).toLong()))
                    when (Random.nextInt(100)) {
                        in 0..80 -> TemperatureEvent(timestamp, (it + Random.nextDouble(20.0, 30.0)).toInt())
                        else -> SmokeEvent(timestamp)
                    }
                }.asIterable(),
                name = "Events generator",
                coder = SerializableCoder.of(Event::class.java)
        ).withTimestamps { it.timestamp.toDateTime().toInstant() }

        val smokeEvents = events.filter { it is SmokeEvent }

        val temperatureWarnings = events.match(
                Pattern.startWith<Event>("first") {
                    it is TemperatureEvent && it.value > TemperatureThreshold
                }.thenFollowBy("second") {
                    it is TemperatureEvent && it.value > TemperatureThreshold
                }.within(Duration.standardSeconds(20))
        ).filter {
            (it["second"].first() as TemperatureEvent).value > (it["first"].first() as TemperatureEvent).value
        }.map {
            (it["second"].first() as TemperatureEvent).let { event ->
                TemperatureWarning(event.timestamp, event.value)
            }
        }

        val alerts = smokeEvents
                .union(temperatureWarnings.map { it })
                .withTimestamps { it.timestamp.toDateTime().toInstant() }
                .match(
                        Pattern.startWith<Event>("first") {
                            it is SmokeEvent
                        }.thenFollowBy("second") {
                            it is TemperatureWarning
                        }.within(Duration.standardSeconds(60))
                )
                .map {
                    FireAlert(it["second"].first().timestamp)
                }

        alerts.forEach { println(it) }

        kio.execute().waitUntilDone()
    }
}