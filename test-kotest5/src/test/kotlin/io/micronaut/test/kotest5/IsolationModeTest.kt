package io.micronaut.test.kotest5

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

class IsolationModeTest : FunSpec({

    test("extension should support multiple instances per spec to handle isolation modes") {
        val a = MicronautKotest5Extension.instantiate(IsolatedTests::class).shouldNotBeNull()
        val b = MicronautKotest5Extension.instantiate(IsolatedTests::class).shouldNotBeNull()
        a.hashCode() shouldNotBe b.hashCode()
        val contexts = MicronautKotest5Extension.contexts[IsolatedTests::class.java.name].shouldNotBeNull().toList()
        contexts.shouldHaveSize(2)
        contexts.forAll { it.isApplicationContextOpen() }
        MicronautKotest5Extension.afterSpec(a)
        MicronautKotest5Extension.afterSpec(b)
        contexts.forAll { !it.isApplicationContextOpen() }
    }
})

@MicronautTest
// need a parameter here so the extension instantiates the spec
private class IsolatedTests(private val mathService: MathService) : FunSpec() {
    init {
        test("test1") {}
        test("test2") {}
    }
}
