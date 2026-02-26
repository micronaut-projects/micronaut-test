
package io.micronaut.test.kotest5

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension

@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(MicronautKotest5Extension)
}
