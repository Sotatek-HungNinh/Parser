package com.cardano.parser.type;

import com.cardano.parser.enumable.PropertyReferenceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PropertyReference implements PropertyType {
    private PropertyReferenceType type;

    /**
     * Type: string | number | boolean | Group | Array | Range | Tag;
     * */
    private Object value;
    private boolean unwrapped;
}
