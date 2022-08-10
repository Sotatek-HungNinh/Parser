package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Range implements Type, PropertyType {
    private final String type = "range";
    private String min;
    private String max;
    private boolean inclusive;
}
