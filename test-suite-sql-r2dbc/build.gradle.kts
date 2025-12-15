import io.micronaut.testresources.buildtools.KnownModules.R2DBC_MYSQL

plugins {
    id("io.micronaut.minimal.library")
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

    testImplementation(projects.micronautTestJunit)
    testImplementation(mnData.micronaut.data.r2dbc)
    testImplementation(mnSerde.micronaut.serde.jackson)

    testImplementation(platform(libs.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.r2dbc)
    testRuntimeOnly(mnSql.mysql.connector.j)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnR2dbc.r2dbc.mysql)
    testRuntimeOnly(libs.managed.junit.platform.launcher)
    testRuntimeOnly(mn.micronaut.http.server.netty)
}

micronaut {
    testRuntime("junit")
    importMicronautPlatform.set(false)
}
