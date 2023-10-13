plugins {
    id("io.micronaut.library")
    id("io.micronaut.test-resources")
}

repositories {
    mavenCentral()
}
micronaut {
    importMicronautPlatform.set(false)
}
