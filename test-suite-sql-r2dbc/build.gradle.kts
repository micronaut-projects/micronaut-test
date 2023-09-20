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
    testAnnotationProcessor("io.micronaut.data:micronaut-data-processor")
    testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    testImplementation(projects.micronautTestJunit5)
    testImplementation("io.micronaut.data:micronaut-data-r2dbc")
    testImplementation("io.micronaut.serde:micronaut-serde-jackson")

    testRuntimeOnly("ch.qos.logback:logback-classic")
    testRuntimeOnly("dev.miku:r2dbc-mysql")

    testResourcesService("mysql:mysql-connector-java")
}

micronaut {
    testResources {
        additionalModules.add(R2DBC_MYSQL)
    }
}
