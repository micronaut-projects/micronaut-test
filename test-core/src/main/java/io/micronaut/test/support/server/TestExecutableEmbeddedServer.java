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
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.AbstractMicronautExtension;

import jakarta.inject.Singleton;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.micronaut.core.io.socket.SocketUtils.findAvailableTcpPort;
import io.micronaut.core.order.Ordered;

/**
 * An {@link EmbeddedServer} implementation that runs an external executable JAR or native.
 *
 * @author graemerocher
 * @since 2.2.1
 */
@Primary
@Order(-100)
@Requires(property = TestExecutableEmbeddedServer.PROPERTY)
@Requires(beans = HttpServerConfiguration.class)
@Singleton
public class TestExecutableEmbeddedServer implements EmbeddedServer {
    public static final String PROPERTY = "micronaut.test.server.executable";

    private final String executable;
    private final ApplicationContext applicationContext;
    private final HttpServerConfiguration httpServerConfiguration;
    private final Environment environment;

    private int port;
    private Process process;

    /**
     * Default constructor.
     * @param executable The executable to run
     * @param applicationContext The context
     * @param httpServerConfiguration The server configuration
     */
    @Internal
    protected TestExecutableEmbeddedServer(
            @Property(name = PROPERTY)
            String executable,
            ApplicationContext applicationContext,
            HttpServerConfiguration httpServerConfiguration) {
        this.executable = executable;
        if (!new File(executable).exists()) {
            throw new ConfigurationException("No executable server exists at path: " + executable);
        }
        this.environment = applicationContext.getEnvironment();
        this.applicationContext = applicationContext;
        this.httpServerConfiguration = httpServerConfiguration;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public URL getURL() {
        try {
            return getURI().toURL();
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Invalid Configured Server URL: " + e.getMessage(), e);
        }
    }

    @Override
    public URI getURI() {
        return URI.create(getScheme() + "://" + getHost() + ":" + getPort());
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return httpServerConfiguration.getApplicationConfiguration();
    }

    @Override
    public boolean isRunning() {
        return process != null;
    }

    @Override
    public EmbeddedServer start() {
        if (process == null) {
            PropertySource testPropertySource = environment.getPropertySources().stream()
                    .filter(ps -> ps.getName().equals(AbstractMicronautExtension.TEST_PROPERTY_SOURCE))
                    .findFirst().orElse(null);

            CompletableFuture<Process> processFuture = new CompletableFuture<>();
            Integer p = httpServerConfiguration.getPort().orElse(null);
            int port;
            if (p == null) {
                if (environment.getActiveNames().contains(Environment.TEST)) {
                    port = findAvailableTcpPort();
                } else {
                    port = 8080;
                }
            } else {
                if (p == -1) {
                    port = findAvailableTcpPort();
                } else {
                    port = p;
                }
            }
            new Thread(() -> {
                ProcessBuilder processBuilder = new ProcessBuilder();
                List<String> commandArgs = new ArrayList<>(Arrays.asList(
                        "-Dmicronaut.environments=test",
                        "-Dmicronaut.server.host=localhost",
                        "-Dmicronaut.server.port=" + port
                ));

                if (executable.endsWith(".jar")) {
                    commandArgs.addAll(0, Arrays.asList(
                            "java",
                            "-jar",
                            executable
                    ));
                } else {
                    commandArgs.add(0, executable);
                }

                if (testPropertySource != null) {
                    for (String prop : testPropertySource) {
                        commandArgs.add("-D" + prop + "=" + testPropertySource.get(prop));
                    }
                }
                processBuilder.command(commandArgs);
                processBuilder.inheritIO();
                try {
                    Process start = processBuilder.start();
                    processFuture.complete(start);
                } catch (IOException e) {
                    processFuture.completeExceptionally(new RuntimeException("Error starting native image server: " + e.getMessage(), e));
                }
            }).start();

            try {
                this.process = processFuture.get();
                int max = 10000;
                int timeout = 0;
                while (timeout < max) {
                    try {
                        URLConnection urlConnection = new URL("http://localhost:" + port).openConnection();
                        urlConnection.setConnectTimeout(max);
                        urlConnection.setReadTimeout(max);
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));
                        in.readLine();
                        in.close();
                    } catch (IOException e) {
                        if (!(e instanceof FileNotFoundException)) {
                            timeout += 100;
                            if (timeout < max) {
                                Thread.sleep(100);
                            } else {
                                throw new ServerStartupException("Timeout occurred starting Micronaut process server");
                            }
                        } else {
                            // response from server
                            break;
                        }
                    }
                }

                this.port = port;
            } catch (InterruptedException | ExecutionException e) {
                throw new ServerStartupException(e.getMessage(), e);
            }
        }
        return this;
    }

    @Override
    public EmbeddedServer stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
        return this;
    }
}
