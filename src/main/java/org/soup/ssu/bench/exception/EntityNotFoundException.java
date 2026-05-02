package org.soup.ssu.bench.exception;

public class EntityNotFoundException extends BaseApplicationException {
    public EntityNotFoundException(String entity, Object id) {
        super(404, "Not Found", String.format("%s not found with id: %s", entity, id));
    }

    public EntityNotFoundException(String message) {
        super(404, "Not Found", message);
    }
}
