package org.devzone.addresses.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.devzone.addresses.model.Address;
import org.devzone.addresses.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

@Controller("/postalcodes")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Inject
    private AddressService addressService;

    @Get("/{postalCode}/{locality}")
    public Mono<Address> findByPostalCodeAndLocality(String postalCode, String locality) {
        return  addressService.findByPostalCodeAndLocality(postalCode, locality);
    }

    @Get("/{postalCode}")
    // call it with curl not postman
    public Flux<Address> findByPostalCode(String postalCode) {
        return addressService.findByPostalCode(postalCode);
    }
}