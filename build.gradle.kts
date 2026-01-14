import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(libs.plugins.vanniktech.maven.publish)
    `java-library` // necessary so that com.vanniktech.maven.publish sees the artefact to be published
    signing
}

buildscript {
    repositories {
        mavenCentral()
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

java {
    toolchain {
        // This is necessary even though the subproject uses jvmToolchain, otherwise the library will target the wrong
        // JDK. Make sure the two are always in sync!
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

subprojects {
    // The following modifies the configuration of the "test" task in all subprojects
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        testLogging {
            events("skipped", "failed") // emit details for skipped and failed tests only, not "passed"
            showStandardStreams = true // otherwise we won't see anything in console
            exceptionFormat = TestExceptionFormat.FULL

            addTestListener(object: TestListener {
                override fun beforeTest(testDescriptor: TestDescriptor) = Unit
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit
                override fun beforeSuite(suite: TestDescriptor) = Unit

                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if (suite.parent == null) {
                        println(
                            "${suite.name} ${result.resultType}: " +
                                "${result.successfulTestCount} passed, " +
                                "${result.failedTestCount} failed, " +
                                "${result.skippedTestCount} skipped"
                        )
                    }
                }
            })
        }
    }
}

// We use the plugin vanniktech.maven.publish, which has some advantages over Gradle's own `maven-publish`.
// POM metadata are in gradle.properties.
// Publish locally if desired (e.g. for verifying target JDK):
//    $ ./gradlew build && ./gradlew publishToMavenLocal
// Maven local is in ~/.m2/repository/io/github/digorydoo/kstruct
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
