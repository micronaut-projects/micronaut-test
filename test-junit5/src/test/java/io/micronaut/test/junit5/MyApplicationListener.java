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
package io.micronaut.test.junit5;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

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
