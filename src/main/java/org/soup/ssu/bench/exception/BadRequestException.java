package org.soup.ssu.bench.exception;

public class BadRequestException extends BaseApplicationException {
    public BadRequestException(String message) {
        super(400, "Bad Request", message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(400, "Bad Request", message, cause);
    }
}
