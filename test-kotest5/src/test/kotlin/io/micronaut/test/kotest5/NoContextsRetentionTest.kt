package io.micronaut.test.kotest5

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

class NoContextsRetentionTest : FunSpec({
    test("extension should not retain references to contexts after tests are finished") {
        MicronautKotest5Extension.contexts[NoContextsRetentionTests::class.java.name].shouldBeNull()
        val a = MicronautKotest5Extension.instantiate(NoContextsRetentionTests::class).shouldNotBeNull()
        val b = MicronautKotest5Extension.instantiate(NoContextsRetentionTests::class).shouldNotBeNull()
        MicronautKotest5Extension.contexts[NoContextsRetentionTests::class.java.name].shouldNotBeNull().shouldHaveSize(2)
        MicronautKotest5Extension.afterSpec(a)
        MicronautKotest5Extension.contexts[NoContextsRetentionTests::class.java.name].shouldNotBeNull().shouldHaveSize(1)
        MicronautKotest5Extension.afterSpec(b)
        MicronautKotest5Extension.contexts[NoContextsRetentionTests::class.java.name].shouldBeNull()
    }
})

@MicronautTest
// need a parameter here so the extension instantiates the spec
private class NoContextsRetentionTests(private val mathService: MathService) : FunSpec() {
    init {
        test("test1") {}
    }
}
