package io.micronaut.test.leak;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JupiterLeakPresenceExtension.class)
public class LeakyNestedTest {

    @Test
    void outer() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.release();
    }

    @Nested
    class Clean {
        @Test
        void releasesItsBuffer() {
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            buffer.release();
        }
    }

    @Nested
    class Leaky {
        // the leak is reported when the enclosing class finishes
        @Test
        void leaksABuffer() {
            ByteBufAllocator.DEFAULT.buffer();
        }
    }
}
