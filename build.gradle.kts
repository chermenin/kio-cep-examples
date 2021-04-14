object Versions {
    const val Kio = "0.1.0"
}

plugins {

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.32"

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

apply(plugin="java")

repositories {
    jcenter()
}

dependencies {

    // Kio
    implementation("ru.chermenin.kio:kio-cep:${Versions.Kio}")
}

application {

    // Define the main class for the application.
    mainClass.set("ru.chermenin.kio.cep.examples.FireMonitoring")
}
