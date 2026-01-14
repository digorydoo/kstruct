import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

// This is necessary to get the correct metadata into the published library.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JVM_17)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java"))
        }
    }
}

dependencies {
    // Versions are maintained in gradle/libs.versions.toml
    implementation(platform(kotlin("bom")))
    testImplementation(kotlin("test"))
}
