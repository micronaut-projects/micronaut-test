/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.test.support.server;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;

import jakarta.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * An {@link EmbeddedServer} implementation that can be enabled by setting {@code micronaut.test.server.url} to run tests against and existing running server.
 *
 * @author graemerocher
 * @since 2.2.0
 */
@Singleton
@Primary
@Requires(property = TestEmbeddedServer.PROPERTY)
@Requires(missingProperty = TestExecutableEmbeddedServer.PROPERTY)
public class TestEmbeddedServer implements EmbeddedServer {
    public static final String PROPERTY = "micronaut.test.server.url";
    private final ApplicationContext applicationContext;
    private final URL url;
    private final ApplicationConfiguration applicationConfiguration;

    /**
     * Default constructor.
     * @param url The server URL
     * @param applicationConfiguration The application configuration
     * @param applicationContext The application context
     */
    @Internal
    protected TestEmbeddedServer(
            @Property(name = PROPERTY) URL url,
            ApplicationConfiguration applicationConfiguration,
            ApplicationContext applicationContext) {
        this.url = url;
        this.applicationConfiguration = applicationConfiguration;
        this.applicationContext = applicationContext;
    }

    @Override
    public int getPort() {
        return getURL().getPort();
    }

    @Override
    public String getHost() {
        return getURL().getHost();
    }

    @Override
    public String getScheme() {
        return getURL().getProtocol();
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public URI getURI() {
        try {
            return getURL().toURI();
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Invalid Server URL: " + e.getMessage(), e);
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    @Override
    public boolean isRunning() {
        return !SocketUtils.isTcpPortAvailable(getPort()) && applicationContext.isRunning();
    }

    @Override
    public boolean isServer() {
        return true;
    }
}
