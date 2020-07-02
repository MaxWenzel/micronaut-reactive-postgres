package org.devzone.addresses.config;

import io.micronaut.context.annotation.*;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.*;

@Factory
class DatabaseConfiguration {

    @Value("${database.url}")
    private String databaseUrl;

    @Bean
    ConnectionPool connectionPool() {
        final ConnectionFactoryOptions baseOptions = ConnectionFactoryOptions.parse(databaseUrl);
        final ConnectionFactoryOptions options = ConnectionFactoryOptions.builder().from(baseOptions).build();
        final ConnectionFactory factory = ConnectionFactories.get(options);
        final ConnectionPoolConfiguration poolOptions = ConnectionPoolConfiguration.builder(factory).build();
        return new ConnectionPool(poolOptions);
    }
}