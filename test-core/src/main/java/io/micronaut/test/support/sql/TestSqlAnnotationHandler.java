/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.test.support.sql;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.support.sql.processor.SqlScriptProcessor;
import io.micronaut.test.support.sql.resolver.DataSourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Static helper class to handle {@link Sql} annotations.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Experimental
public final class TestSqlAnnotationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestSqlAnnotationHandler.class);

    private TestSqlAnnotationHandler() {
    }

    /**
     * Given a spec definition and application context, find and process all {@link Sql} annotations.
     *
     * @param specDefinition The test class
     * @param applicationContext The application context
     * @throws SQLException If an error occurs executing the SQL
     * @throws IOException If an error occurs reading the SQL
     */
    public static void handle(BeanDefinition<?> specDefinition, ApplicationContext applicationContext) throws SQLException, IOException {
        ResourceLoader resourceLoader = applicationContext.getBean(ResourceLoader.class);

        var compositeDataSourceHandler = applicationContext.getBean(DataSourceResolver.class);

        Optional<List<AnnotationValue<Sql>>> sqlAnnotations = specDefinition
            .findAnnotation(Sql.Sqls.class)
            .map(s -> s.getAnnotations("value", Sql.class));

        if (sqlAnnotations.isPresent()) {
            for (var sql : sqlAnnotations.get()) {
                String datasourceName = sql.getRequiredValue("datasourceName", String.class);

                Class<?> dataSourceType = sql.get("resourceType", Class.class).filter(c -> c != Object.class).orElse(DataSource.class);

                Object bean = applicationContext.getBean(dataSourceType, Qualifiers.byName(datasourceName));

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Looking for resource of type {} with name {}", dataSourceType, datasourceName);
                }

                Optional<? extends SqlScriptProcessor> resolve = compositeDataSourceHandler.resolve(bean);

                if (resolve.isPresent()) {
                    handleScript(
                        resourceLoader,
                        Arrays.asList(sql.stringValues("value")),
                        resolve.get()
                    );
                } else {
                    LOG.warn("Could not resolve data source: {}", datasourceName);
                }
            }
        }
    }

    private static void handleScript(
        ResourceLoader loader,
        List<String> scripts,
        SqlScriptProcessor processor
    ) throws IOException, SQLException {
        for (String script : scripts) {
            Optional<URL> resource = loader.getResource(script);
            if (resource.isPresent()) {
                try (InputStream in = resource.get().openStream()) {
                    String scriptBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    processor.process(scriptBody);
                }
            } else {
                LOG.warn("Could not find SQL script: {}", script);
            }
        }
    }
}
