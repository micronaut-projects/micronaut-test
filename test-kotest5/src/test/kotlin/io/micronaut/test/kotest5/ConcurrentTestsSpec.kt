package io.micronaut.test.kotest5

import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.concurrency.TestExecutionMode
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.coroutines.delay

// tag::concurrent[]
@MicronautTest
class ConcurrentTestsSpec : FreeSpec({

    // Configure this spec to run tests concurrently
    testExecutionMode = TestExecutionMode.Concurrent

    "test 1" {
        // This test will run concurrently with other tests
        1 + 2 shouldBe 3
        delay(1000)
    }

    "test 2" {
        // This test will run concurrently with other tests
        1 + 2 shouldBe 3
        delay(500)
    }

    "test 3" {
        // This test will run concurrently with other tests
        1 + 2 shouldBe 3
        delay(200)
    }
})
// end::concurrent[]
