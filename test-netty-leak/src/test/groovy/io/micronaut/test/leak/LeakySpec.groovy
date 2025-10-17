package io.micronaut.test.leak

import io.netty.buffer.ByteBufAllocator
import spock.lang.Specification

class LeakySpec extends Specification {
    def test() {
        expect:
        ByteBufAllocator.DEFAULT.buffer() != null
    }
}
