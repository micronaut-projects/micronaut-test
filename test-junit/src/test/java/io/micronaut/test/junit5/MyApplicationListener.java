
package io.micronaut.test.junit5;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MyApplicationListener implements ApplicationEventListener<StartupEvent> {
    private final String description;

    public MyApplicationListener(String description) {
        this.description = description;
    }

    @Inject
    public MyApplicationListener() {
        this.description = "Real bean";
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        // no-op
    }
}
