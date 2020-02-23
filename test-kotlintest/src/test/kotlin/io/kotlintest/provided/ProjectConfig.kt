package io.kotlintest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotlintest.MicronautKotlinTestExtension

object ProjectConfig : AbstractProjectConfig() {
    override fun listeners() = listOf(MicronautKotlinTestExtension)
    override fun extensions() = listOf(MicronautKotlinTestExtension)
}
