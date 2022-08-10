package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Property {
    private boolean hasCut;
    private Occurrence occurrence;
    private String name;
    private Object propertyType;  // propertyType[];
    private String comment;
}
