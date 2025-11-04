//import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotest)
    //alias(libs.plugins.kotlin.qa)
}

group = "it.unibo.jakta"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest)

    //TODO remove this at some point
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

//tasks.withType<Detekt>().configureEach {
//    enabled = false
//}
