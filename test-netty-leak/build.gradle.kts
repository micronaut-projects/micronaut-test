import io.micronaut.build.TestFramework

plugins {
    id("io.micronaut.build.internal.micronaut-test-module")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.managed.junit.jupiter.api)
    compileOnly(platform(libs.boms.spock))
    compileOnly(libs.spock.core)
    implementation(mn.netty.common)
    testImplementation(libs.managed.junit.platform.testkit)
    testImplementation(mn.netty.buffer)
    testImplementation(platform(libs.boms.spock))
    testImplementation(libs.spock.core)
    testRuntimeOnly(libs.managed.junit.jupiter.engine)
}

micronautBuild {
    binaryCompatibility {
        enabledAfter("4.10.0")
    }
    testFramework = TestFramework.JUNIT6
}

tasks.withType(Test::class.java) {
    // these are run explicitly by LeakPresenceExtensionTest
    exclude("io/micronaut/test/leak/LeakyTest.class", "io/micronaut/test/leak/LeakySpec.class")
}
