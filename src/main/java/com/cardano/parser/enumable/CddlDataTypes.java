package com.cardano.parser.enumable;

import com.cardano.parser.utils.Utils;

public enum CddlDataTypes {
    BOOL("bool"),
    INT("int"),
    UINT("uint"),
    NINT("nint"),
    FLOAT("float"),
    FLOAT16("float16"),
    FLOAT32("float32"),
    FLOAT64("float64"),
    BSTR("bstr"),
    BYTES("bytes"),
    TSTR("tstr"),
    TEXT("text"),
    ANY("any");

    public String value;

    CddlDataTypes(String value) {
        this.value = value;
    }

    public static CddlDataTypes getType(String value) {
        CddlDataTypes result = null;
        for (CddlDataTypes type : CddlDataTypes.values()) {
            if (type.value.equals(value)) {
                result = type;
                break;
            }
        }
        if (Utils.startWithSpecialCharacter(value)) {
            result = ANY;
        }
        return result;
    }
}
