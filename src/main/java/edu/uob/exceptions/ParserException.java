package edu.uob.exceptions;

import java.io.Serial;

public class ParserException extends Exception {
    @Serial
    private static final long serialVersionUID = 2822482689382526773L;

    public ParserException(String message) {
        super(message);
    }

    public static class InvalidTokenException extends ParserException {
        @Serial
        private static final long serialVersionUID = 6460199000268556302L;

        public InvalidTokenException(String token) {
            super("Token \"" + token + "\" is invalid.");
        }
    }

    public static class MissingTokenException extends ParserException {
        @Serial
        private static final long serialVersionUID = 1732408999841280788L;

        public MissingTokenException(String token) {
            super("Token \"" + token + "\" is missing.");
        }
    }

    public static class UnexpectedTokenException extends ParserException {
        @Serial
        private static final long serialVersionUID = -1907440999314503514L;

        public UnexpectedTokenException(String token, String expected) {
            super("Token \"" + token + "\" should be " + expected + ".");
        }
    }

    public static class RemainTokensException extends ParserException {
        @Serial
        private static final long serialVersionUID = 4546633966632707011L;

        public RemainTokensException(String token) {
            super("Redundant token(s) start from \"" + token + "\".");
        }
    }
}
