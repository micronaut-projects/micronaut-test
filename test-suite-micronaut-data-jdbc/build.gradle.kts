plugins {
    `java-library`
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnData.micronaut.data.processor)

    implementation(mnData.micronaut.data.jdbc)
    implementation(mnSql.micronaut.jdbc.hikari)

    runtimeOnly(mnSql.h2)
    runtimeOnly(mnLogging.logback.classic)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testAnnotationProcessor(mnData.micronaut.data.processor)

    testCompileOnly(mn.micronaut.inject.groovy)
    testCompileOnly(mnData.micronaut.data.processor)

    testImplementation(libs.managed.junit.jupiter.api)
    testImplementation(libs.managed.junit.jupiter.params)
    testImplementation(libs.managed.junit.platform.launcher)
    testImplementation(projects.micronautTestJunit5)
    testImplementation(projects.micronautTestSpock)

    testRuntimeOnly(libs.managed.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
