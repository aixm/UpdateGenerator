plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "com.solitec.aixm"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
    implementation("com.github.ajalt:clikt:2.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

application {
    mainClass.set("com.solitec.aixm.updgen.MainKt")
}