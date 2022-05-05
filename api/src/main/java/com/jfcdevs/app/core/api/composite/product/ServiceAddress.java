package com.jfcdevs.app.core.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ServiceAddress {
    private final String cmp;
    private final String pro;
    private final String rev;
    private final String rec;
}
