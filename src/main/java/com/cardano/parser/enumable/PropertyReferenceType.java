package com.cardano.parser.enumable;

public enum PropertyReferenceType {
    LITERAL("literal"),
    GROUP("group"),
    GROUP_ARRAY("group_array"),
    RANGE("range"),
    TAG("tag");

    private String value;

    PropertyReferenceType(String value) {
        this.value = value;
    }

    public PropertyReferenceType getType(String value) {
        PropertyReferenceType result = null;
        for (PropertyReferenceType type : PropertyReferenceType.values()) {
            if (type.value.equals(value)) {
                result = type;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("No matching constant for [" + value + "]");
        }
        return result;
    }
}
