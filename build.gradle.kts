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

// The plugin vanniktech.maven.publish has some advantages over Gradle's own `maven-publish`.
// Publish the artefact like this:
//    $ ./gradlew build && ./gradlew publishToMavenCentral
// You can publish to maven local with:
//    $ ./gradlew build && ./gradlew publishToMavenLocal
// Maven local is in ~/.m2/repository/io/github/digorydoo/kstruct
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
