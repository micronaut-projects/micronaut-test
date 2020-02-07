package io.micronaut.test.junit5;

import static io.micronaut.http.HttpStatus.MOVED_PERMANENTLY;
import static io.micronaut.http.HttpStatus.OK;
import static org.hamcrest.core.Is.is;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.test.annotation.MicronautTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

@MicronautTest
public class TestClientConfigurationInterferenceInjection {

    @Inject
    @Client(value = "/", configuration = TestNoFollowRedirectClientConfig.class)
    RxHttpClient clientNoFollow;

    /**
     * Should use {@link DefaultHttpClientConfiguration}.
     */
    @Inject
    @Client(value = "/")
    RxHttpClient clientFollow;

    @Test
    void redirect_followClient() {
        // Given
        String uri = "/test/redirect";

        // When
        HttpResponse<String> result = clientFollow.toBlocking().exchange(uri, String.class);

        // Then
        MatcherAssert.assertThat(result.status(), is(OK));
        MatcherAssert.assertThat(result.body(), StringContains.containsString("It works!"));
    }

    @Test
    void redirect_noFollowClient() {
        // Given
        String uri = "/test/redirect";

        // When
        HttpResponse<String> response = clientNoFollow.toBlocking().exchange(uri, String.class);

        // Then
        MatcherAssert.assertThat(response.status(), is(MOVED_PERMANENTLY));
    }

    @Singleton
    static class TestNoFollowRedirectClientConfig extends HttpClientConfiguration {

        private final ConnectionPoolConfiguration connectionPoolConfiguration;

        @Inject
        public TestNoFollowRedirectClientConfig(ApplicationConfiguration applicationConfiguration,
                ConnectionPoolConfiguration connectionPoolConfiguration) {
            super(applicationConfiguration);
            this.connectionPoolConfiguration = connectionPoolConfiguration;
        }

        @Override
        public ConnectionPoolConfiguration getConnectionPoolConfiguration() {
            return connectionPoolConfiguration;
        }

        @Override
        public boolean isFollowRedirects() {
            return false;
        }
    }


    @Controller("/test")
    static class TestController {

        @Get("redirect")
        public HttpResponse<?> redirect() {
            return HttpResponse.redirect(URI.create("/test/direct"));
        }

        @Get("direct")
        public HttpResponse<?> direct() {
            return HttpResponse.ok("It works!");
        }
    }
}
