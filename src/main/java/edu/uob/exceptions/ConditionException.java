package edu.uob.exceptions;

import java.io.Serial;

public class ConditionException extends Exception {
    @Serial
    private static final long serialVersionUID = -4740585436577285803L;

    public ConditionException(String message) {
        super("Failed to process conditions. " + message);
    }

    public static class InvalidOperatorException extends ConditionException {
        @Serial
        private static final long serialVersionUID = 2642306917913747983L;

        public InvalidOperatorException(String operator) {
            super("Invalid comparing operator \"" + operator + "\".");
        }
    }

    public static class OperatorValueTypeNotMatchException extends ConditionException {
        @Serial
        private static final long serialVersionUID = 5274757453168795156L;

        public OperatorValueTypeNotMatchException(String value) {
            super("Operator for value " + value + " is not valid.");
        }
    }
}
