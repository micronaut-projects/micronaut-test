plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    testAnnotationProcessor(mn.micronaut.inject.java)

    annotationProcessor(mnData.micronaut.data.processor)
    implementation(mnData.micronaut.data.hibernate.jpa)
    implementation(mnSql.micronaut.jdbc.hikari)
    runtimeOnly(mnSql.postgresql)

    runtimeOnly(mnLogging.logback.classic)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(projects.micronautTestJunit5)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
