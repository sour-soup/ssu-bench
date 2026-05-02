package org.soup.ssu.bench.exception;

public class ForbiddenException extends BaseApplicationException {
    public ForbiddenException(String message) {
        super(403, "Forbidden", message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(403, "Forbidden", message, cause);
    }
}
