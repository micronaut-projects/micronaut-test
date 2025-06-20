import io.micronaut.testresources.buildtools.KnownModules.R2DBC_MYSQL

plugins {
    id("io.micronaut.minimal.library")
    id("io.micronaut.test-resources")
}

repositories {
    mavenCentral()
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}

dependencies {
    testAnnotationProcessor(mnData.micronaut.data.processor)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)

    testImplementation(projects.micronautTestJunit5)
    testImplementation(mnData.micronaut.data.r2dbc)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(platform(mnTestResources.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)

    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnR2dbc.r2dbc.mysql)
    testRuntimeOnly(libs.managed.junit.platform.launcher)

    testResourcesService(mnSql.mysql.connector.java)
}

micronaut {
    testRuntime("junit5")
    importMicronautPlatform.set(false)
    testResources {
        version.set(libs.versions.micronaut.test.resources)
        additionalModules.add(R2DBC_MYSQL)
    }
}
