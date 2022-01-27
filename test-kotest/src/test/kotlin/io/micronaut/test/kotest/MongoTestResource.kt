package io.micronaut.test.kotest

import io.micronaut.core.value.PropertyResolver
import io.micronaut.test.support.resource.TestResource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoResource : TestResource {
    private var mongoDBContainer: MongoDBContainer? = null

    @Throws(Exception::class)
    override fun start(environment: PropertyResolver): Map<String, String?> {
        mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
        mongoDBContainer!!.start()
        return mapOf(
            "mongodb.uri" to mongoDBContainer?.getReplicaSetUrl()
        )
    }

    @Throws(Exception::class)
    override fun close() {
        mongoDBContainer?.stop()
    }
}
