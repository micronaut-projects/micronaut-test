import io.micronaut.build.TestFramework

plugins {
    id("io.micronaut.build.internal.micronaut-test-module")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.managed.bytebuddy)

    testImplementation(libs.managed.bytebuddy.agent)
    testImplementation(libs.managed.junit.jupiter.api)
    testRuntimeOnly(libs.managed.junit.jupiter.engine)
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

afterEvaluate {
    tasks.withType<Test> {
        extensions.findByType(JacocoTaskExtension::class.java)?.isEnabled = false
    }
}

micronautBuild {
    binaryCompatibility {
        enabled.set(true)
    }
    testFramework = TestFramework.JUNIT6
}
