package com.cardano.parser.utils;

import com.cardano.parser.enumable.TokenType;
import com.cardano.parser.type.StringType;
import com.cardano.parser.type.Token;
import lombok.Getter;
import lombok.Setter;

import static com.cardano.parser.constants.Constants.SPECIAL_CHARACTERS;
import static com.cardano.parser.constants.Constants.WHITESPACE_CHARACTERS;
import static com.cardano.parser.enumable.TokenType.*;
import static com.cardano.parser.utils.Utils.*;

@Getter
@Setter
public class Lexer {

    private String input;
    private int position = 0;
    private int readPosition = 0;
    private int ch = 0;

    public Lexer(String source) {
        this.input = source;

        this.readChar();
    }

    private void readChar() {
        if (this.readPosition >= this.input.length()) {
            this.ch = 0;
        } else {
            this.ch = String.valueOf(this.input.charAt(this.readPosition)).codePointAt(0);
        }
        this.position = this.readPosition;
        this.readPosition++;
    }

    private String readString() {
        int position = this.position;

        this.readChar(); // eat "
        while (this.ch != 0 && !convertCharCodeToString(this.ch).equals(QUOT.getValue())) {
            this.readChar(); // eat any character until "
        }

        return this.input.substring(position + 1, this.position).trim();
    }

    private String readComment() {
        int position = this.position;

        while (this.ch != 0 && !convertCharCodeToString(this.ch).equals("\n")) {
            this.readChar();
        }

        return this.input.substring(position, this.position).trim();
    }

    private String readIdentifier() {
        int position = this.position;

        /**
         * an identifier can contain
         * see https://tools.ietf.org/html/draft-ietf-cbor-cddl-08#section-3.1
         */
        while (isLetter(convertCharCodeToChar(this.ch))
                || isDigit(convertCharCodeToChar(this.ch))
                || SPECIAL_CHARACTERS.contains(this.ch)) {
            this.readChar();
        }
        return this.input.substring(position, this.position);
    }

    private String readNumberOrFloat() {
        int position = this.position;
        boolean foundSpecialCharacter = false;

        /**
         * a number of float can contain
         */
        while (isDigit(convertCharCodeToChar(this.ch))
                // a special character, e.g. ".", "x" and "b"
                || hasSpecialNumberCharacter(this.ch)
        ) {
            /**
             * ensure we respect ranges, e.g. 0..10
             * so break after the second dot and adjust read position
             */
            if (hasSpecialNumberCharacter(this.ch) && foundSpecialCharacter) {
                this.position--;
                this.readPosition--;
                break;
            }

            if (hasSpecialNumberCharacter(this.ch)) {
                foundSpecialCharacter = true;
            }

            this.readChar(); // eat any character until a non digit or a 2nd dot
        }

        return this.input.substring(position, this.position).trim();
    }

    private void skipWhitespace() {
        while (WHITESPACE_CHARACTERS.contains(Character.toString(this.ch))) {
            this.readChar();
        }
    }

    public Token nextToken() {
        Token token;
        this.skipWhitespace();

        String literalVal = convertCharCodeToString(this.ch);
        StringType literal = new StringType();
        literal.setValue(literalVal);

        TokenType tokenType = getTokenFromCode(this.ch);
        switch (tokenType) {
            case ASSIGN:
                token = new Token(ASSIGN, literal);
                break;
            case LPAREN:
                token = new Token(LPAREN, literal);
                break;
            case RPAREN:
                token = new Token(RPAREN, literal);
                break;
            case LBRACE:
                token = new Token(LBRACE, literal);
                break;
            case RBRACE:
                token = new Token(RBRACE, literal);
                break;
            case LBRACK:
                token = new Token(LBRACK, literal);
                break;
            case RBRACK:
                token = new Token(RBRACK, literal);
                break;
            case LT:
                token = new Token(LT, literal);
                break;
            case GT:
                token = new Token(GT, literal);
                break;
            case PLUS:
                token = new Token(PLUS, literal);
                break;
            case COMMA:
                token = new Token(COMMA, literal);
                break;
            case DOT:
                token = new Token(DOT, literal);
                break;
            case COLON:
                token = new Token(COLON, literal);
                break;
            case QUEST:
                token = new Token(QUEST, literal);
                break;
            case SLASH:
                token = new Token(SLASH, literal);
                break;
            case ASTERISK:
                token = new Token(ASTERISK, literal);
                break;
            case CARET:
                token = new Token(CARET, literal);
                break;
            case HASH:
                token = new Token(HASH, literal);
                break;
            case TILDE:
                token = new Token(TILDE, literal);
                break;
            case STRING:
                literal.setValue(this.readString());
                token = new Token(STRING, literal);
                break;
            case COMMENT:
                literal.setValue(this.readComment());
                token = new Token(COMMENT, literal);
                break;
            case EOF:
                token = new Token(EOF, new StringType());
                break;
            default: {
                if (isAlphabeticCharacter(literal.getValue().charAt(0))) {
                    literal.setValue(this.readIdentifier());
                    return new Token(IDENT, literal);
                } else if (isDigit(literal.getValue().charAt(0))) {
                    String numberOrFloat = this.readNumberOrFloat();
                    literal.setValue(numberOrFloat);
                    return new Token(
                            numberOrFloat.contains(DOT.getValue()) ? FLOAT : NUMBER, literal);
                }
                token = new Token(ILLEGAL, new StringType());
                break;
            }
        }

        this.readChar();
        return token;
    }
}
