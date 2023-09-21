import io.micronaut.testresources.buildtools.KnownModules.R2DBC_MYSQL

plugins {
    id("io.micronaut.library")
    id("io.micronaut.test-resources")
    id("io.micronaut.graalvm") // Required to configure Graal for nativeTest
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

    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnR2dbc.r2dbc.mysql)

    testResourcesService(mnSql.mysql.connector.java)
}

micronaut {
    version.set(libs.versions.micronaut.platform)
    testResources {
        additionalModules.add(R2DBC_MYSQL)
    }
}
