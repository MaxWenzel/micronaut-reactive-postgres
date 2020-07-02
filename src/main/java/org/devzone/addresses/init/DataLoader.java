package org.devzone.addresses.init;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.scheduling.annotation.Async;
import org.devzone.addresses.model.Address;
import org.devzone.addresses.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class DataLoader implements ApplicationEventListener<StartupEvent> {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Inject
    private AddressService addressService;

    @Async
    @Override
    public void onApplicationEvent(final StartupEvent event) {
        log.info("Loading data at startup");

        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<InputStream> resourceAsStream = loader.getResourceAsStream("classpath:postalcode_locality_de.csv");

        List<String> addresses = new ArrayList<>();
        if (resourceAsStream.isPresent()) {
            InputStream inputStream = resourceAsStream.get();
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines();
            addresses.addAll(lines.collect(Collectors.toList()));
            insertAddresses(addresses);
        }
    }

    private void insertAddresses(List<String> addresses) {
        List<Address> convertedAddresses = new ArrayList<>();
        for (String plainAddress : addresses) {
            String[] fields = plainAddress.split(",");
            convertedAddresses.add(new Address(fields[2], fields[1], fields[3]));
        }
        if (!convertedAddresses.isEmpty()) {
            addressService.insert(convertedAddresses);
        }
    }
}