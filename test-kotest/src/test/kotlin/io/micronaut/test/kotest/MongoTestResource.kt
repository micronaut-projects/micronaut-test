package io.micronaut.test.kotest

import io.micronaut.test.support.resource.TestResourceManager
import io.micronaut.test.support.resource.TestRun
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoResource : TestResourceManager {
    private var mongoDBContainer: MongoDBContainer? = null

    @Throws(Exception::class)
    override fun close() {
        mongoDBContainer?.stop()
    }

    override fun start(testRun: TestRun) {
        mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
        mongoDBContainer!!.start()
        testRun.addProperty("mongodb.uri", mongoDBContainer!!.replicaSetUrl)
    }
}
