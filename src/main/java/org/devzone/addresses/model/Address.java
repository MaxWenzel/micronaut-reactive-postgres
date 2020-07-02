package org.devzone.addresses.model;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Introspected
public class Address {

    private String postalCode;
    private String locality;
    private String state;

}
