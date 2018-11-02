Spock: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-spock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-spock)
JUnit 5: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-junit5/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.micronaut.test/micronaut-test-junit5)

# Micronaut Test

This project provides testing extension for JUnit 5 and Spock to make it easier to test Micronaut applications.

For more information see the [Documentation](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html).

Example Spock Test:

```groovy
import io.micronaut.test.annotation.MicronautTest
import spock.lang.*
import javax.inject.Inject

@MicronautTest 
class MathServiceSpec extends Specification {

    @Inject
    MathService mathService // 

    @Unroll
    void "should compute #num times 4"() { 
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

import javax.inject.Inject;


@MicronautTest // <1>
class MathServiceTest {

    @Inject
    MathService mathService; // <2>


    @ParameterizedTest
    @CsvSource({"2,8", "3,12"})
    void testComputeNumToSquare(Integer num, Integer square) {
        final Integer result = mathService.compute(num); // <3>

        Assertions.assertEquals(
                square,
                result
        );
    }
}

```
