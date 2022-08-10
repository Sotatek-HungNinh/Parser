package com.cardano.parser.utils;


import com.cardano.parser.enumable.TokenType;
import com.cardano.parser.type.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isLetter(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    public static boolean isAlphabeticCharacter(char ch) {
        return isLetter(ch)
                || TokenType.ATSIGN.getValue().equals(String.valueOf(ch))
                || TokenType.UNDERSCORE.getValue().equals(String.valueOf(ch))
                || TokenType.DOLLAR.getValue().equals(String.valueOf(ch));
    }

    public static boolean hasSpecialNumberCharacter(int ch) {
        return ch == TokenType.DOT.getValue().codePointAt(0)
                || ch == "x".codePointAt(0)
                || ch == "b".codePointAt(0);

    }

    public static boolean hasSpecialCharacter(String input) {
        Pattern p = Pattern.compile("[^A-Za-z0-9 ]");
        Matcher m = p.matcher(input);
        return m.find();
    }

    public static boolean startWithSpecialCharacter(String input) {
        String firstChar = input.substring(0, 1);
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(firstChar);
        return m.find();
    }

    public static boolean isDigit(char ch) {
        return Character.isDigit(ch)
                && !TokenType.NL.getValue().equals(String.valueOf(ch))
                && !TokenType.SPACE.getValue().equals(String.valueOf(ch));
    }

    public static float parseFloatValue(Token token) {
        return Float.parseFloat(token.getLiteral().getValue());
    }

    public static String getStringValue(Token token) {
        if (token.getLiteral().getValue().contains("x") || token.getLiteral().getValue().contains("b")) {
            return token.getLiteral().getValue();
        }
        return null;
    }

    public static int parseIntValue(Token token) {
        return Integer.parseInt(token.getLiteral().getValue(), 10);
    }

    public static char convertCharCodeToChar(int code) {
        return Character.toString(code).charAt(0);
    }

    public static String convertCharCodeToString(int code) {
        return Character.toString(code);
    }

    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

}
