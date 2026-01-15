plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.maven.publish)
    `java-library`
    signing
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Versions are maintained in gradle/libs.versions.toml
    // Do NOT include platform(kotlin("bom")) here, or sonatype will reject the artefact!
    testImplementation(kotlin("test"))
}

// We use the plugin vanniktech.maven.publish, which has some advantages over Gradle's own `maven-publish`.
// POM metadata are in gradle.properties.
// Publish locally if desired (e.g. for verifying target JDK):
//    $ ./gradlew build && ./gradlew publishToMavenLocal
// Maven local is in ~/.m2/repository/io/github/digorydoo/kstruct
// You can check if the jar contains the expected classes after publishing to maven local like this:
//    $ jar tf ~/.m2/repository/io/github/digorydoo/kstruct/version/kstruct-version.jar
// Upload to sonatype:
//    $ ./gradlew build && ./gradlew publishToMavenCentral
// Uploads will appear in: https://central.sonatype.com/publishing/deployments
// Uploads must be published manually (see mavenCentralAutomaticPublishing in gradle.properties).
// Published version is here: https://central.sonatype.com/artifact/io.github.digorydoo/kstruct/overview
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

// If signing fails with the error:
//    gpg: signing failed: Inappropriate ioctl for device
// then add this to your .bashrc:
//    export GPG_TTY=$(tty)
signing {
    useGpgCmd()
    sign(publishing.publications)
}
