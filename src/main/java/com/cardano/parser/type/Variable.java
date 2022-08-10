package com.cardano.parser.type;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Variable implements Type, Assignment {
    private final String type = "variable";
    private String name;
    private boolean isChoiceAddition;
    private Object propertyTypes;  // PropertyType or PropertyType[]
}
