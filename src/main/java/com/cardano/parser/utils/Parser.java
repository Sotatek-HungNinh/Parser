package com.cardano.parser.utils;

import com.cardano.parser.enumable.PropertyReferenceType;
import com.cardano.parser.enumable.TokenType;
import com.cardano.parser.type.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cardano.parser.constants.Constants.BOOLEAN_LITERALS;
import static com.cardano.parser.constants.Constants.PREDEFINED_IDENTIFIER;
import static com.cardano.parser.enumable.CddlDataTypes.*;
import static com.cardano.parser.enumable.PropertyReferenceType.GROUP;
import static com.cardano.parser.enumable.PropertyReferenceType.LITERAL;


public class Parser {
    private Lexer lexer;
    private final Token NIL_TOKEN = new Token(TokenType.ILLEGAL, new StringType());
    private final Occurrence DEFAULT_OCCURRENCE = new Occurrence(1, 1); // exactly one time
    private Token currentToken = NIL_TOKEN;
    private Token peekToken = NIL_TOKEN;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.nextToken();
        this.nextToken();
    }

    private boolean nextToken() {
        this.currentToken = this.peekToken;
        this.peekToken = this.lexer.nextToken();
        return true;
    }

    private Assignment parseAssignments() throws Exception {
        /**
         * expect group identifier, e.g.
         * groupName =
         * groupName /=
         * groupName //=
         */
        if (this.currentToken.getType() != TokenType.IDENT
                || !(this.peekToken.getType() == TokenType.ASSIGN || this.peekToken.getType() == TokenType.SLASH)) {
            throw new Exception("group identifier expected, received " + this.currentToken.toString());
        }

        boolean isChoiceAddition = false;
        String groupName = this.currentToken.getLiteral().getValue();
        this.nextToken(); // eat group identifier

        if (this.currentToken.getType() == TokenType.SLASH) {
            isChoiceAddition = true;
            this.nextToken(); // eat `/`
        }

        if (this.currentToken.getType() == TokenType.SLASH) {
            this.nextToken(); // eat `/`
        }

        this.nextToken(); // eat `=`
        return (Assignment) this.parseAssignmentValue(groupName, isChoiceAddition);
    }

    /**
     * @param groupName        nullable String
     * @param isChoiceAddition default false
     * @return Assignment | List<PropertyType> as Object
     */
    private Object parseAssignmentValue(String groupName, boolean isChoiceAddition) throws Exception {
        boolean isChoice = false;
        List<List<Property>> valuesOrProperties = new ArrayList<>();
        List<String> closingTokens = this.openSegment();

        /**
         * if no group segment was opened we have a variable assignment
         * and can return immediatelly
         */
        if (closingTokens.size() == 0) {
            if (!Utils.isEmpty(groupName)) {
                return new Variable(groupName, isChoiceAddition, this.parsePropertyTypes());
            }

            return this.parsePropertyTypes();
        }

        while (!closingTokens.contains(this.currentToken.getType().getValue())) {
            List<PropertyType> propertyType = new ArrayList<>();
            boolean isUnwrapped = false;
            boolean hasCut = false;
            String propertyName = "";
            String comment = "";

            Occurrence occurrence = this.parseOccurrences();

            /**
             * check if variable name is unwrapped
             */
            if (this.currentToken.getLiteral().getValue().equals(TokenType.TILDE.getValue())) {
                isUnwrapped = true;
                this.nextToken(); // eat ~
            }

            propertyName = this.parsePropertyName();

            /**
             * if `,` is found we have a group reference and jump to the next line
             */
            if (this.currentToken.getType() == TokenType.COMMA || closingTokens.contains(this.currentToken.getType().getValue())) {
                TokenType tokenType = this.currentToken.getType();
                boolean parsedComments = false;

                /**
                 * check if line has a comment
                 */
                if (this.currentToken.getType() == TokenType.COMMA && this.peekToken.getType() == TokenType.COMMENT) {
                    this.nextToken();
                    comment = this.parseComment();
                    parsedComments = true;
                }

                valuesOrProperties.add(List.of(Property.builder()
                        .hasCut(hasCut)
                        .occurrence(occurrence)
                        .name("")
                        .comment(comment)
                        .propertyType(PREDEFINED_IDENTIFIER.contains(propertyName) ? propertyName
                                : List.of(PropertyReference.builder().type(PropertyReferenceType.GROUP)
                                .value(propertyName).unwrapped(isUnwrapped).build()))
                        .build()));

                if (!parsedComments) {
                    this.nextToken();
                }

                /**
                 * only continue if next token contains a comma
                 */
                if (tokenType == TokenType.COMMA) {
                    continue;
                }

                /**
                 * otherwise break
                 */
                break;
            }

            /**
             * check if property has cut, which happens if a property is described as
             * - `? "optional-key" ^ => int,`
             * - `? optional-key: int,` - since the colon shortcut includes cuts
             */
            if (this.currentToken.getType() == TokenType.CARET || this.currentToken.getType() == TokenType.COLON) {
                hasCut = true;

                if (this.currentToken.getType() == TokenType.CARET) {
                    this.nextToken(); // eat ^
                }
            }

            /**
             * check if we have a choice instead of an assignment
             */
            if (this.currentToken.getType() == TokenType.SLASH && this.peekToken.getType() == TokenType.SLASH) {
                Property prop = Property.builder()
                        .hasCut(hasCut)
                        .occurrence(occurrence)
                        .name("")
                        .comment(comment)
                        .propertyType(PropertyReference.builder().type(PropertyReferenceType.GROUP).value(propertyName).unwrapped(isUnwrapped).build())
                        .build();

                if (isChoice) {
                    /**
                     * if we already in a choice just push into it
                     */
                    valuesOrProperties.get(valuesOrProperties.size() - 1).add(prop);
                } else {
                    /**
                     * otherwise create a new one
                     */
                    isChoice = true;
                    valuesOrProperties.add(List.of(prop));
                }

                this.nextToken(); // eat /
                this.nextToken(); // eat /
                continue;
            }

            /**
             * else if no colon was found, throw
             */
            if (!this.isPropertyValueSeparator()) {
                throw new Exception("Expected \":\" or =>. Actual: " + this.currentToken);
            }

            this.nextToken(); // eat :

            /**
             * parse property value
             */
            Object props = this.parseAssignmentValue(null, false);
            if (props instanceof List) {
                /**
                 * property has multiple types (e.g. `float / tstr / int`)
                 */
                propertyType.addAll((Collection<? extends PropertyType>) props);
            } else {
                propertyType.add((PropertyType) props);
            }

            /**
             * advance comma
             */
            boolean flipIsChoice = false;
            if (this.currentToken.getType() == TokenType.COMMA) {
                /**
                 * if we are in a choice, we leave it here
                 */
                flipIsChoice = true;

                this.nextToken(); // eat ,
            }

            comment = this.parseComment();

            Property prop = Property.builder()
                    .hasCut(hasCut)
                    .occurrence(occurrence)
                    .name(propertyName)
                    .propertyType(propertyType)
                    .comment(comment)
                    .build();

            if (isChoice) {
                (valuesOrProperties.get(valuesOrProperties.size() - 1)).add(prop);
            } else {
                valuesOrProperties.add(List.of(prop));
            }

            if (flipIsChoice) {
                isChoice = false;
            }

            /**
             * if `}` is found we are at the end of the group
             */
            if (closingTokens.contains(this.currentToken.getType().getValue())) {
                break;
            }

            /**
             * eat // if we are in a choice
             */
            if (isChoice) {
                this.nextToken(); // eat /
                this.nextToken(); // eat /
            }
        }

        /**
         * close segment
         */
        String firstClosingToken = closingTokens.get(0);
        if (this.currentToken.getType().getValue().equals(firstClosingToken)) {
            this.nextToken();
        }

        /**
         * if last closing token is "]" we have an array
         */
        if (closingTokens.get(closingTokens.size() - 1).equals(TokenType.RBRACK.getValue())) {
            return Array.builder()
                    .name(Utils.isEmpty(groupName) ? "" : groupName)
                    .values(valuesOrProperties)
                    .build();
        }

        /**
         * otherwise a group
         */
        return Group.builder()
                .name(Utils.isEmpty(groupName) ? "" : groupName)
                .properties(valuesOrProperties)
                .isChoiceAddition(isChoiceAddition)
                .build();
    }

    private PropertyType parsePropertyType() throws Exception {
        PropertyType type;
        boolean isUnwrapped = false;

        /**
         * check if variable name is unwrapped
         */
        if (this.currentToken.getLiteral().getValue().equals(TokenType.TILDE.getValue())) {
            isUnwrapped = true;
            this.nextToken(); // eat ~
        }

        if (BOOL.value.equals(this.currentToken.getLiteral().getValue()) || INT.value.equals(this.currentToken.getLiteral().getValue())
                || UINT.value.equals(this.currentToken.getLiteral().getValue()) || NINT.value.equals(this.currentToken.getLiteral().getValue())
                || FLOAT.value.equals(this.currentToken.getLiteral().getValue()) || FLOAT16.value.equals(this.currentToken.getLiteral().getValue())
                || FLOAT32.value.equals(this.currentToken.getLiteral().getValue()) || FLOAT64.value.equals(this.currentToken.getLiteral().getValue())
                || BSTR.value.equals(this.currentToken.getLiteral().getValue()) || BYTES.value.equals(this.currentToken.getLiteral().getValue())
                || TSTR.value.equals(this.currentToken.getLiteral().getValue()) || TEXT.value.equals(this.currentToken.getLiteral().getValue())) {
            type = this.currentToken.getLiteral();
        } else {
            if (BOOLEAN_LITERALS.contains(this.currentToken.getLiteral().getValue())) {
                type = PropertyReference.builder()
                        .type(LITERAL)
                        .value(this.currentToken.getLiteral().getValue().equals("true"))
                        .unwrapped(isUnwrapped)
                        .build();
            } else if (this.currentToken.getType() == TokenType.IDENT) {
                type = PropertyReference.builder()
                        .type(GROUP)
                        .value(this.currentToken.getLiteral())
                        .unwrapped(isUnwrapped)
                        .build();
            } else if (this.currentToken.getType() == TokenType.STRING) {
                type = PropertyReference.builder()
                        .type(LITERAL)
                        .value(this.currentToken.getLiteral())
                        .unwrapped(isUnwrapped)
                        .build();
            } else if (this.currentToken.getType() == TokenType.NUMBER) {
                if (this.currentToken.getLiteral().getValue().contains("x") || this.currentToken.getLiteral().getValue().contains("b")) {
                    type = PropertyReference.builder()
                            .type(LITERAL)
                            .value(this.currentToken.getLiteral())
                            .unwrapped(isUnwrapped)
                            .build();
                } else {
                    type = PropertyReference.builder()
                            .type(LITERAL)
                            .value(Utils.parseIntValue(this.currentToken))
                            .unwrapped(isUnwrapped)
                            .build();
                }
            } else if (this.currentToken.getType() == TokenType.FLOAT) {
                type = PropertyReference.builder()
                        .type(LITERAL)
                        .value(Utils.parseFloatValue(this.currentToken))
                        .unwrapped(isUnwrapped)
                        .build();
            } else if (this.currentToken.getType() == TokenType.HASH) {
                this.nextToken();
                int n = Utils.parseIntValue(this.currentToken);
                this.nextToken(); // eat numeric value
                this.nextToken(); // eat (
                PropertyType t = this.parsePropertyType();
                this.nextToken();// eat )
                type = Tag.builder()
                        .typePart(String.valueOf(t))
                        .numericPart(n)
                        .build();

            } else {
                throw new Exception("Invalid property type " + this.currentToken.getLiteral());
            }
        }

        /**
         * check if type continue as a range
         */
        if (this.peekToken.getType() == TokenType.DOT &&
                this.nextToken() &&
                this.peekToken.getType() == TokenType.DOT) {
            this.nextToken();
            boolean inclusive = true;

            /**
             * check if range excludes upper bound
             */
            if (this.peekToken.getType() == TokenType.DOT) {
                inclusive = false;
                this.nextToken();
            }
            this.nextToken();

            String min = "";
            if (type instanceof StringType) {
                min = ((StringType) type).getValue();
            } else if (type instanceof PropertyReference) {
                min = ((PropertyReference) type).getValue().toString();
            }

            String max = ((PropertyReference)this.parsePropertyType()).getValue().toString();
            type = Range.builder()
                    .min(min)
                    .max(max)
                    .inclusive(inclusive)
                    .build();
        }

        return type;
    }

    /**
     * checks if group segment is opened and forwards to beginning of
     * first property declaration
     *
     * @returns List String  closing tokens for group (either `}`, `)` or both)
     */
    private List<String> openSegment() {
        if (this.currentToken.getType() == TokenType.LBRACE) {
            this.nextToken();

            if (this.peekToken.getType() == TokenType.LPAREN) {
                this.nextToken();
                return List.of(TokenType.RPAREN.getValue(), TokenType.RBRACE.getValue());
            }
            return List.of(TokenType.RBRACE.getValue());
        } else if (this.currentToken.getType() == TokenType.LPAREN) {
            this.nextToken();
            return List.of(TokenType.RPAREN.getValue());
        } else if (this.currentToken.getType() == TokenType.LBRACK) {
            this.nextToken();
            return List.of(TokenType.RBRACK.getValue());
        }

        return List.of();
    }

    private List<PropertyType> parsePropertyTypes() throws Exception {
        List<PropertyType> propertyTypes = new ArrayList<>();

        propertyTypes.add(this.parsePropertyType());
        this.nextToken(); // eat `/`

        /**
         * ensure we don't go into the next choice, e.g.:
         * ```
         * delivery = (
         *   city // lala: tstr / bool // per-pickup: true,
         * )
         */
        if (this.currentToken.getType() == TokenType.SLASH && this.peekToken.getType() == TokenType.SLASH) {
            return propertyTypes;
        }

        /**
         * capture more if available (e.g. `tstr / float / boolean`)
         */
        while (this.currentToken.getType() == TokenType.SLASH) {
            this.nextToken(); // eat `/`
            propertyTypes.add(this.parsePropertyType());
            this.nextToken();

            /**
             * ensure we don't go into the next choice, e.g.:
             * ```
             * delivery = (
             *   city // lala: tstr / bool // per-pickup: true,
             * )
             */
            if (this.currentToken.getType() == TokenType.SLASH && this.peekToken.getType() == TokenType.SLASH) {
                break;
            }
        }

        return propertyTypes;
    }

    private Occurrence parseOccurrences() {
        Occurrence occurrence = DEFAULT_OCCURRENCE;

        /**
         * check for non-numbered occurrence indicator, e.g.
         * ```
         *  * bedroom: size,
         * ```
         * which is the same as:
         * ```
         *  ? bedroom: size,
         * ```
         * or have miniumum of 1 occurrence
         * ```
         *  + bedroom: size,
         * ```
         */
        if (this.currentToken.getType() == TokenType.QUEST || this.currentToken.getType() == TokenType.ASTERISK || this.currentToken.getType() == TokenType.PLUS) {
            int n = this.currentToken.getType() == TokenType.PLUS ? 1 : 0;
            int m = Integer.MAX_VALUE;

            /**
             * check if there is a max definition
             */
            if (this.peekToken.getType() == TokenType.NUMBER) {
                m = Integer.parseInt(this.peekToken.getLiteral().getValue(), 10);
                this.nextToken();
            }

            occurrence = new Occurrence(n, m);
            this.nextToken();
            /**
             * numbered occurrence indicator, e.g.
             * ```
             *  1*10 bedroom: size,
             * ```
             */
        } else if (
                this.currentToken.getType() == TokenType.NUMBER &&
                        this.peekToken.getType() == TokenType.ASTERISK
        ) {
            int n = Integer.parseInt(this.currentToken.getLiteral().getValue(), 10);
            int m = Integer.MAX_VALUE;
            this.nextToken(); // eat "n"
            this.nextToken();// eat "*"

            /**
             * check if there is a max definition
             */
            if (this.currentToken.getType() == TokenType.NUMBER) {
                m = Integer.parseInt(this.currentToken.getLiteral().getValue(), 10);
                this.nextToken();
            }

            occurrence = new Occurrence(n, m);
        }

        return occurrence;
    }

    /**
     * check if line has a comment
     */
    private String parseComment() {
        String comment = "";
        if (this.currentToken.getType() == TokenType.COMMENT) {
            comment = this.currentToken.getLiteral().getValue().substring(1);
            this.nextToken();
        }
        return comment;
    }

    private String parsePropertyName() throws Exception {
        if (this.currentToken.getType() == TokenType.COMMA) {
            this.nextToken();
        }
        /**
         * property name without quotes
         */
        if (this.currentToken.getType() == TokenType.IDENT || this.currentToken.getType() == TokenType.STRING) {
            String name = this.currentToken.getLiteral().getValue();
            this.nextToken();
            return name;
        }

        throw new Exception("Expected property name, received " + this.currentToken.toString());
    }

    private boolean isPropertyValueSeparator() {
        if (this.currentToken.getType() == TokenType.COLON) {
            return true;
        }

        if (this.currentToken.getType() == TokenType.ASSIGN && this.peekToken.getType() == TokenType.GT) {
            this.nextToken(); // eat <
            return true;
        }

        return false;
    }

    public List<Assignment> parse() throws Exception {
        List<Assignment> definitions = new ArrayList<>();

        while (this.currentToken.getType() != TokenType.EOF) {
            if (this.currentToken.getType() == TokenType.COMMENT) {
                Comment comment = Comment.builder()
                        .content(this.currentToken.getLiteral().getValue().substring(1).trim())
                        .build();

                definitions.add(comment);
                this.nextToken();
                continue;
            }
            Assignment group = this.parseAssignments();
            if (group != null) {
                definitions.add(group);
            }
        }
        return definitions;
    }

}
