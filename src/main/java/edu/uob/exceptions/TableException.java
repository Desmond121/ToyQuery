package edu.uob.exceptions;

import java.io.Serial;

public class TableException extends Exception {
    @Serial
    private static final long serialVersionUID = 4501537919083303324L;

    public TableException(String message) {
        super(message);
    }

    public static class AttributeDuplicatedException extends TableException {
        @Serial
        private static final long serialVersionUID = 6427736112954741094L;

        public AttributeDuplicatedException(String name) {
            super("Attribute \"" + name + "\" is duplicated.");
        }
    }

    public static class AttributeMissingException extends TableException {
        @Serial
        private static final long serialVersionUID = -3282026402562315196L;

        public AttributeMissingException(String name) {
            super("Attribute \"" + name + "\" is missing.");
        }
    }

    public static class InvalidTableOperationException extends TableException {
        @Serial
        private static final long serialVersionUID = 3270131117430748558L;

        public InvalidTableOperationException(String message) {
            super("Invalid operation: " + message);
        }
    }

    public static class InvalidImportStringException extends TableException {
        @Serial
        private static final long serialVersionUID = -630396255249263650L;

        public InvalidImportStringException(String message) {
            super(message);
        }
    }

    public static class IdNotFoundException extends TableException {
        @Serial
        private static final long serialVersionUID = -4772466937804301430L;

        public IdNotFoundException(int id) {
            super("Id not found: " + id + ".");
        }
    }

    public static class ReadTableFailedException extends TableException {
        @Serial
        private static final long serialVersionUID = -1848179880422269787L;

        public ReadTableFailedException(String file) {
            super("Failed to read table from file:\"" + file + "\".");
        }
    }

    public static class WriteTableFailedException extends TableException {
        @Serial
        private static final long serialVersionUID = -2170929296228399317L;

        public WriteTableFailedException(String file) {
            super("Failed to write table to file: \"" + file + "\".");
        }
    }
}
