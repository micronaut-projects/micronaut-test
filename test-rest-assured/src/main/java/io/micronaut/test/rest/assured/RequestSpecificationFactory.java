/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.rest.assured;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.server.EmbeddedServer;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

/**
 * Helper class making it easier to create request specifications.
 *
 * @author gkrocher
 * @since 3.4.0 
 */
@Factory
@Requires(beans = EmbeddedServer.class) 
public class RequestSpecificationFactory {

    /**
     * @param embeddedServer The embedded server
     * @return A request specification for the current server.
     */
    @Prototype
    @Requires(beans = EmbeddedServer.class) 
    RequestSpecification requestSpecification(EmbeddedServer embeddedServer) {
        return RestAssured.given()
            .port(embeddedServer.getPort());
    }

}
