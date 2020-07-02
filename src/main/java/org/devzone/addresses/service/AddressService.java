package org.devzone.addresses.service;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.annotation.Controller;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.*;
import lombok.RequiredArgsConstructor;
import org.devzone.addresses.init.DataLoader;
import org.devzone.addresses.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Primary
@Controller
@RequiredArgsConstructor
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    private final ConnectionPool connectionPool;

    public Mono<Address> findByPostalCodeAndLocality(String postalCode, String locality) {
        return connectionPool.create().flatMap(connection -> {
            final Statement statement = connection.createStatement("SELECT * FROM postalcode WHERE postalcode = $1 AND locality = $2");
            statement.bind(0, postalCode);
            statement.bind(1, locality);
            return Mono.from(statement.execute())
                    .map(result -> result.map(this::convertToAddress))
                    .flatMap(Mono::from)
                    .timeout(Duration.ofSeconds(1))
                    .doOnTerminate(() -> Mono.from(connection.close()).subscribe());
        });
    }

    public Flux<Address> findByPostalCode(String postalCode) {
        return connectionPool.create().flatMapMany(connection -> {
            final Statement statement = connection.createStatement("SELECT * FROM postalcode WHERE postalcode = $1");
            statement.bind(0, postalCode);
            return Mono.from(statement.execute())
                    .map(result -> result.map(this::convertToAddress))
                    .flatMapMany(Flux::from)
                    .timeout(Duration.ofSeconds(3))
                    .doOnTerminate(() -> Mono.from(connection.close()).subscribe());
        });
    }

    public Mono<Void> insert(Address address) {
        return connectionPool.create().flatMap(connection -> {
            final Statement statement = connection.createStatement("INSERT INTO postalcode (postalcode, locality, state) VALUES ($1, $2, $3)");
            statement.bind(0, address.getPostalCode());
            statement.bind(1, address.getLocality());
            statement.bind(2, address.getState());
            return Mono.from(statement.execute())
                    .flatMap(this::checkOneRowUpdated)
                    .timeout(Duration.ofSeconds(1))
                    .doOnTerminate(() -> Mono.from(connection.close()).subscribe());
        });
    }

    private Mono<Void> checkOneRowUpdated(Result result) {
        return Mono.from(result.getRowsUpdated())
                .flatMap(rows -> rows != 1 ? Mono.error(new RuntimeException("Address not found")) : Mono.empty());
    }

    public void insert(List<Address> addresses) {
        logger.info("Try to insert {} addresses", addresses.size());
        Flux.from(connectionPool.create())
                .flatMap(con -> {
                            Batch batch = con.createBatch()
                                    .add("delete from postalcode");

                            for (Address address : addresses) {
                                batch.add(String.format("INSERT INTO postalcode (postalcode, locality, state) VALUES ('%s', '%s', '%s')", address.getPostalCode(), address.getLocality(), address.getState()));
                            }

                            return Flux.from(
                                    batch
                                            .execute())
                                            .doFinally((st) -> con.close());
                        }
                )
                .log()
                .blockLast(Duration.ofSeconds(5));
    }

    private Address convertToAddress(Row row, RowMetadata rm) {
        final String postalCode = (String) row.get("postalcode");
        final String locality = (String) row.get("locality");
        final String state = (String) row.get("state");
        logger.info("Found address for locality {}", locality);
        return new Address(postalCode, locality, state);
    }

}
