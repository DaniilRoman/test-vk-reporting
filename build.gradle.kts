import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    application
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.example.testvkreporting.TestVkReportingApplicationKt")
}

dependencies {
    implementation("com.vk.api:sdk:1.0.14")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.5.0")

    // exposed
    implementation("org.jetbrains.exposed", "exposed-core", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.38.1")

    implementation("io.github.microutils", "kotlin-logging-jvm", "2.0.11")
    implementation("ch.qos.logback", "logback-classic", "1.2.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
