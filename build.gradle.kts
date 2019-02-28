import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    application
}

group = "org.hildan.hashcode"
version = "0.3.0"
description = "Utilities for programs solving Google HashCode problems"

application {
    mainClassName = "MainKt"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("org.hildan.hashcode:hashcode-utils-kt:0.3.0")
    implementation("org.slf4j:slf4j-api:1.7.24")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

apply(from = "hashcode.gradle")
