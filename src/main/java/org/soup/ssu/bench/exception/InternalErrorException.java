package org.soup.ssu.bench.exception;

public class InternalErrorException extends BaseApplicationException {
    public InternalErrorException(String message) {
        super(500, "Internal Error", message);
    }

    public InternalErrorException(String message, Throwable e) {
        super(500, "Internal Error", message, e);
    }
}
