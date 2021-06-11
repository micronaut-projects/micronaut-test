Spock: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-spock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-spock)
JUnit 5: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-junit5/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-junit5)
Kotest: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-kotest/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-kotest)

# Micronaut Test

This project provides testing extension for JUnit 5, Spock and Kotest to make it easier to test Micronaut applications.

For more information see the [Latest](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html) or [Snapshot](https://micronaut-projects.github.io/micronaut-test/snapshot/guide/index.html) Documentation.

Example Spock Test:

```groovy
import io.micronaut.test.annotation.MicronautTest
import spock.lang.*
import jakarta.inject.Inject

@MicronautTest // Declares the test as a micronaut test
class MathServiceSpec extends Specification {

    @Inject
    MathService mathService // Dependency injection is used to supply the system under test

    @Unroll
    void "should compute #num times 4"() { // This is the test case. #num will be replaces by the values defined in the where: block
        when:
        def result = mathService.compute(num)

        then:
        result == expected

        where:
        num | expected
        2   | 8
        3   | 12
    }
}
```

Example JUnit 5 Test:

```java
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.inject.Inject;


@MicronautTest // Declares the test as a micronaut test
class MathServiceTest {

    @Inject
    MathService mathService; // Dependency injection is used to supply the system under test


    @ParameterizedTest
    @CsvSource({"2,8", "3,12"})
    void testComputeNumToSquare(Integer num, Integer square) {
        final Integer result = mathService.compute(num); // Injected bean can be used in test case

        Assertions.assertEquals(
                square,
                result
        );
    }
}

```

## Snapshots and Releases

Snaphots are automatically published to [JFrog OSS](https://oss.jfrog.org/artifactory/oss-snapshot-local/) using [Github Actions](https://github.com/micronaut-projects/micronaut-test/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to JCenter and Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-test/actions).

A release is performed with the following steps:

* [Update the draft release](https://github.com/micronaut-projects/micronaut-test/releases).
* [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-test/actions?query=workflow%3ARelease) to check it passed successfully.
* Celebrate!
