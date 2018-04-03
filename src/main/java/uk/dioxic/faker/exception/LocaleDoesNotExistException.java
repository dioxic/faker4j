package uk.dioxic.faker.exception;

public class LocaleDoesNotExistException extends RuntimeException {
    public LocaleDoesNotExistException(String message) {
        super(message);
    }
}