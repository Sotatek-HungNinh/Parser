package com.cardano.parser.enumable;

public enum TokenType {

    ILLEGAL("ILLEGAL", -1),

    EOF("EOF", 0),

    NL("\n", 10),

    SPACE(" ", 32),

    UNDERSCORE("_", 95),

    DOLLAR("$", 36),

    ATSIGN("@", 64),

    CARET("^", 94),

    HASH("#", 35),

    TILDE("~", 126),

    // Identifiers + literals),
    IDENT("IDENT", 73),

    INT("INT", 73),

    COMMENT("COMMENT", 59),

    STRING("STRING", 83),

    NUMBER("NUMBER", 78),

    FLOAT("FLOAT", 70),

    // Operators),
    ASSIGN("=", 61),
    //
    PLUS("+", 43),

    MINUS("-", 45),

    SLASH("/", 47),

    QUEST("?", 63),

    ASTERISK("*", 42),

    // Delimiters),
    COMMA(",", 44),

    DOT(".", 46),

    COLON(":", 58),

    SEMICOLON(";", 59),

    LPAREN("(", 40),

    RPAREN(")",41),

    LBRACE("{", 123),

    RBRACE("}",125),

    LBRACK("[",91),

    RBRACK("]", 93),

    LT("<", 60),

    GT(">",62),

    QUOT("\"",34);

    private String value;
    private int code;

    TokenType(String value, int code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return this.value;
    }

    public int getCode() {
        return this.code;
    }

    public TokenType getTokenFromValue(String value) {
        TokenType result = null;
        for (TokenType tokens : TokenType.values()) {
            if (tokens.value.equals(value)) {
                result = tokens;
                break;
            }
        }
        return result;
    }

    public static TokenType getTokenFromCode(int code) {
        TokenType result = null;
        for (TokenType token : TokenType.values()) {
            if (token.code == code) {
                result = token;
                break;
            }
        }
        if (result == null){
            result = TokenType.ILLEGAL;
        }
        return result;
    }

}
