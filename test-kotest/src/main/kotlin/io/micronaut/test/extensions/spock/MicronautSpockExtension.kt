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
package io.micronaut.test.extensions.spock

/**
 * This class is necessary because `test-core` declares it as part of the class annotation, at 
 * `io.micronaut.test.annotation.MicronautTest`. It's removed from the classpath from commit
 * 0cf54bfbca0aa0f9ec39ed4cea31375ba7e1e441 on.
 *
 * When Kotest tries to initialize the spec, `io.micronaut.test.annotation::findRepeatableAnnotations` would throw an
 * exception because `MicronautSpockExtension` won't be on the classpath.
 * 
 * This small hack adds it to the classpath and hides it via the `internal`. It's not an issue as users from Kotest
 * won't use MicronautSpockExtension anyway.
 */
internal class MicronautSpockExtension 
