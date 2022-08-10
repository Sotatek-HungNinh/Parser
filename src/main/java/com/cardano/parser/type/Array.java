package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Array implements Type, Assignment, PropertyType {
    private final String type = "array";
    private String name;
    private List<List<Property>> values;  // List<Property>
}
