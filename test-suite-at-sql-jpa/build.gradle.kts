plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnData.micronaut.data.processor)

    implementation(mnData.micronaut.data.hibernate.jpa)
    implementation(mnSql.micronaut.jdbc.hikari)

    runtimeOnly(mnSql.postgresql)
    runtimeOnly(mnLogging.logback.classic)

    testAnnotationProcessor(mn.micronaut.inject.java)

    testImplementation(libs.managed.junit.jupiter.api)
    testImplementation(projects.micronautTestJunit5)
    testImplementation(mnTestResources.testcontainers.postgres)
    testImplementation(platform(mnTestResources.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.managed.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
