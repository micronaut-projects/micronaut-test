
package io.micronaut.test.spock.base

class InjectSharedSpec2 extends AbstractSharedSpec {

    def setupSpec() {
        assert mathService != null
    }

    void "test use shared 1"() {
        expect:
        mathService.compute(10) == 40
    }

    void "test use shared 2"() {
        expect:
        mathService.compute(10) == 40
    }
}
