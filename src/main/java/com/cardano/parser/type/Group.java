package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Group implements Type, Assignment {
    private final String type = "group";
    private String name;
    private boolean isChoiceAddition;
    private List<List<Property>> properties;
}
