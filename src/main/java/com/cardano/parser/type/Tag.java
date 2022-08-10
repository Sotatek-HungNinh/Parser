package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Tag implements Type, PropertyType {
    private final String type = "tag";
    private int numericPart;
    private String typePart;
}
