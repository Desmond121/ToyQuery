package edu.uob.conditions;

import edu.uob.exceptions.ConditionException;
import edu.uob.exceptions.ValueException;
import edu.uob.tables.Type;
import edu.uob.tables.Value;

import java.util.function.BiPredicate;

public class Condition {
    private final String attribute;
    private final Operator operator;
    private final Value referenceValue;

    public Condition(String attribute, String operator, String referenceValue) throws ConditionException, ValueException {
        this.attribute = attribute;
        this.operator = matchOperator(operator);
        Type type = Value.matchValueType(referenceValue);
        if (type == Type.STRING) // remove quote
            referenceValue = referenceValue.substring(1, referenceValue.length() - 1);
        this.referenceValue = new Value(referenceValue, type);
    }

    private static boolean isEqual(Value value, Value referenceValue) {
        return value.compareTo(referenceValue) == 0;
    }

    private static boolean isNotEqual(Value value, Value referenceValue) {
        return value.compareTo(referenceValue) != 0;
    }

    private static boolean isLike(Value value, Value referenceValue) {
        return value.valueString().contains(referenceValue.valueString());
    }

    private static boolean isGreater(Value value, Value referenceValue) {
        if (isValueNull(value)) return false;
        return value.compareTo(referenceValue) > 0;
    }

    private static boolean isLess(Value value, Value referenceValue) {
        if (isValueNull(value)) return false;
        return value.compareTo(referenceValue) < 0;
    }

    private static boolean isGreaterOrEqual(Value value, Value referenceValue) {
        if (isValueNull(value)) return false;
        return value.compareTo(referenceValue) >= 0;
    }

    private static boolean isLessOrEqual(Value value, Value referenceValue) {
        if (isValueNull(value)) return false;
        return value.compareTo(referenceValue) <= 0;
    }

    //     In Table class, the value from table will be directly set in type of referenceValue
    // without any checking. In Value class, the compareTo method set result of Null and
    // Integer/Float to -1. In this case, once value is Null, the result of == or != will
    // be correct but result of other operator will not.
    //     So all these methods will check whether value is null first by using this method.
    private static boolean isValueNull(Value value) {
        return value.valueString().equalsIgnoreCase("null");
    }

    private static Operator matchOperator(String operatorString) throws ConditionException {
        return switch (operatorString.toUpperCase()) {
            case "==" -> Operator.EQUAL;
            case "<=" -> Operator.LESS_OR_EQUAL;
            case ">=" -> Operator.GREATER_OR_EQUAL;
            case "!=" -> Operator.NOT_EQUAL;
            case "<" -> Operator.LESS;
            case ">" -> Operator.GREATER;
            case "LIKE" -> Operator.LIKE;
            default -> throw new ConditionException.InvalidOperatorException(
                    operatorString);
        };
    }

    // Check operator validation by reference value (the value given by user).
    private void checkOperatorValid() throws ConditionException {
        Type type = referenceValue.type();
        boolean isMatch = switch (operator) {
            case EQUAL, NOT_EQUAL -> true; // valid for all type
            case GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL -> type == Type.INT ||
                                                                   type == Type.FLOAT;
            case LIKE -> type == Type.STRING;
        };
        if (!isMatch) throw new ConditionException.OperatorValueTypeNotMatchException(
                referenceValue.valueString());
    }

    public String getAttribute() {
        return attribute;
    }

    public Value getReferenceValue() {
        return referenceValue;
    }

    public BiPredicate<Value, Value> getOperatorMethod() throws ConditionException {
        checkOperatorValid();
        return switch (operator) {
            case EQUAL -> Condition::isEqual;
            case LESS_OR_EQUAL -> Condition::isLessOrEqual;
            case GREATER_OR_EQUAL -> Condition::isGreaterOrEqual;
            case NOT_EQUAL -> Condition::isNotEqual;
            case LESS -> Condition::isLess;
            case GREATER -> Condition::isGreater;
            case LIKE -> Condition::isLike;
        };
    }

    private enum Operator {
        EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, LIKE
    }
}
