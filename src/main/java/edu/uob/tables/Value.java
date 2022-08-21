package edu.uob.tables;

import edu.uob.exceptions.ValueException;

// this class don't check the type of value, just store it.
public record Value(String valueString, Type type) implements Comparable<Value> {
    private final static String REGEX_FLOAT = "^[+-]?[0-9]+[.][0-9]+$";
    private final static String REGEX_INTEGER = "^[+-]?[0-9]+$";
    private final static String REGEX_STRING = "^'.*'$";

    // This method is used to match value from a token generate by Tokenizer. Which means
    // a token surround by \' is a string. Other type should also match their regex.
    public static Type matchValueType(String value) throws ValueException {
        Type type;
        switch (value.toUpperCase()) {
            case "TRUE", "FALSE" -> type = Type.BOOL;
            case "NULL" -> type = Type.NULL;
            default -> {
                if (value.matches(REGEX_STRING)) type = Type.STRING;
                else if (value.matches(REGEX_FLOAT)) type = Type.FLOAT;
                else if (value.matches(REGEX_INTEGER)) type = Type.INT;
                else throw new ValueException.ValueTypeMatchingFailedException(value);
            }
        }
        return type;
    }

    // when value is NULL, compare with INT or FLOAT will cause error.
    // this issue will be handled in Condition class.
    // this compare method is base on the type of referenceValue
    public int compareTo(Value referenceValue) {
        try {
            return switch (referenceValue.type) {
                case INT -> Integer.compare(Integer.parseInt(valueString),
                                            Integer.parseInt(referenceValue.valueString));
                case FLOAT -> Float.compare(Float.parseFloat(valueString),
                                            Float.parseFloat(referenceValue.valueString));
                case STRING -> valueString.compareTo(referenceValue.valueString);
                case BOOL, NULL -> valueString.equalsIgnoreCase(
                        referenceValue.valueString) ? 0 : 1;
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}


