package com.cardano.parser.constants;

import com.cardano.parser.enumable.TokenType;

import java.util.List;

public class Constants {

    public static List<Integer> SPECIAL_CHARACTERS = List.of(
            TokenType.MINUS.getCode(),
            TokenType.UNDERSCORE.getCode(),
            TokenType.ATSIGN.getCode(),
            TokenType.DOT.getCode(),
            TokenType.DOLLAR.getCode());
    public static List<String> WHITESPACE_CHARACTERS = List.of(" ", "\t", "\n", "\r");
    public static List<String> BOOLEAN_LITERALS = List.of("true", "false");
    public static List<String> PREDEFINED_IDENTIFIER = List.of(
            "any", "uint", "nint", "int", "bstr", "bytes", "tstr", "text",
            "tdate", "time", "number", "biguint", "bignint", "bigint",
            "integer", "unsigned", "decfrac", "bigfloat", "eb64url",
            "eb64legacy", "eb16", "encoded-cbor", "uri", "b64url",
            "b64legacy", "regexp", "mime-message", "cbor-any", "float16",
            "float32", "float64", "float16-32", "float32-64", "float",
            "false", "true", "bool", "nil", "null", "undefined");
}
