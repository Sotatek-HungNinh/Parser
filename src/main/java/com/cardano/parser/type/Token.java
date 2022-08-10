package com.cardano.parser.type;

import com.cardano.parser.enumable.TokenType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Token implements Type {
    private TokenType type;
    private StringType literal;
}
