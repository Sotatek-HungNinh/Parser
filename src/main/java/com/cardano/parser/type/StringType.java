package com.cardano.parser.type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StringType implements PropertyType, Type {
    private final String type = "String";
    private String value = "";
}
