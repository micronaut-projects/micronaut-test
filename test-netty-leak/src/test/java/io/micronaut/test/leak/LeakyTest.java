package io.micronaut.test.leak;

import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JupiterLeakPresenceExtension.class)
public class LeakyTest {
    // this test triggers leak detection.

    @Test
    void test() {
        ByteBufAllocator.DEFAULT.buffer();
    }
}
