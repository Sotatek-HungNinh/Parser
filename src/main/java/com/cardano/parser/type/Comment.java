package com.cardano.parser.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Comment implements Type, Assignment {
    private final String type = "comment";
    private String content;
}
