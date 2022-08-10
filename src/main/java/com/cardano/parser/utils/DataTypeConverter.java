package com.cardano.parser.utils;

import com.cardano.parser.enumable.CddlDataTypes;

import java.util.HashMap;
import java.util.Map;

import static com.cardano.parser.enumable.CddlDataTypes.*;


public class DataTypeConverter {

    static Map<CddlDataTypes, String> mapNullableDataType = new HashMap<>() {{
        put(TSTR, "String");
        put(TEXT, "String");
        put(BOOL, "Boolean");
        put(INT, "Integer");
        put(UINT, "Integer");
        put(NINT, "Integer");
        put(FLOAT, "Float");
        put(FLOAT16, "Float");
        put(FLOAT32, "Double");
        put(FLOAT64, "Double");
        put(BSTR, "Byte");
        put(BYTES, "Byte");
        put(ANY, "Object");
    }};

    static Map<CddlDataTypes, String> mapNotNullableDataType = new HashMap<>() {{
        put(TSTR, "String");
        put(TEXT, "String");
        put(BOOL, "boolean");
        put(INT, "int");
        put(UINT, "int");
        put(NINT, "int");
        put(FLOAT, "float");
        put(FLOAT16, "float");
        put(FLOAT32, "double");
        put(FLOAT64, "double");
        put(BSTR, "byte");
        put(BYTES, "byte");
        put(ANY, "Object");
    }};


    public static String getJavaDataType(CddlDataTypes cddlDataType, boolean nullable) {
        if (nullable) {
            return mapNullableDataType.get(cddlDataType);
        }
        return mapNotNullableDataType.get(cddlDataType);
    }

    public static boolean isJavaDataType(String input) {
        return mapNullableDataType.containsValue(input) || mapNotNullableDataType.containsValue(input);
    }
}
