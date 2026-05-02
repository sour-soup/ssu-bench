package org.soup.ssu.bench.exception;

public class UnauthorizedException extends BaseApplicationException {
    public UnauthorizedException(String message) {
        super(401, "Unauthorized", message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(401, "Unauthorized", message, cause);
    }
}
