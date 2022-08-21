package edu.uob.exceptions;

import java.io.Serial;

public class ValueException extends Exception {
    @Serial
    private static final long serialVersionUID = -8244959735753696331L;

    public ValueException(String message) {
        super(message);
    }

    public static class ValueTypeMatchingFailedException extends ValueException {
        @Serial
        private static final long serialVersionUID = 7321722747629123122L;

        public ValueTypeMatchingFailedException(String value) {
            super("\"" + value + "\" is not a <Value>.");
        }
    }
}
