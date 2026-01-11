plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    jvmToolchain(libs.versions.targetJDK.get().toInt())
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
