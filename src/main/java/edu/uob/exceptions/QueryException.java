package edu.uob.exceptions;

import java.io.Serial;

public class QueryException extends Exception {
    @Serial
    private static final long serialVersionUID = 1967342121903155755L;

    public QueryException(String message) {
        super(message);
    }

    public static class NoSpecificDatabaseException extends QueryException {

        @Serial
        private static final long serialVersionUID = 5943126923707632175L;

        public NoSpecificDatabaseException() {
            super("Database not specified.");
        }
    }
}
