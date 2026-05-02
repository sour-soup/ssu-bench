package org.soup.ssu.bench.exception;

import lombok.Getter;

@Getter
public class BaseApplicationException extends RuntimeException {
    private final int status;
    private final String error;

    public BaseApplicationException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public BaseApplicationException(int status, String error, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.error = error;
    }
}
