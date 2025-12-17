package io.micronaut.test.r2dbc;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class MySQL {
    private static final String IMAGE_NAME = "mysql:lts";
    private static MySQLContainer<?> mysql;

    public static Map<String, String> getProperties() {
        if (mysql == null) {
            mysql = new MySQLContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withConfigurationOverride(null); // for Native tests
            mysql.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!mysql.isRunning());
            return getProperties(mysql);
        } else {
            return getProperties(mysql);
        }
    }

    private static Map<String, String> getProperties(MySQLContainer mysql) {
        return Map.of(
            "r2dbc.datasources.default.url", mysql.getJdbcUrl().replace("jdbc", "r2dbc"),
            "r2dbc.datasources.default.username", mysql.getUsername(),
            "r2dbc.datasources.default.password", mysql.getPassword()
        );
    }
}
