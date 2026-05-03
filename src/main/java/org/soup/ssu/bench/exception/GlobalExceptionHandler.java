package org.soup.ssu.bench.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ssu.bench.model.ErrorResponse;
import ssu.bench.model.ErrorStatusEnum;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.FORBIDDEN, "Access is denied");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildResponse(ErrorStatusEnum.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ErrorResponse> handleBaseApplication(BaseApplicationException ex) {
        log.warn("Application error: {}", ex.getMessage());
        ErrorStatusEnum status = mapStatus(ex.getStatus());
        return buildResponse(status, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(ErrorStatusEnum.INTERNAL_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorStatusEnum status, String message) {
        ErrorResponse response = new ErrorResponse()
            .status(status)
            .message(message)
            .timestamp(LocalDateTime.now());
        return ResponseEntity.status(mapToHttp(status)).body(response);
    }

    private int mapToHttp(ErrorStatusEnum status) {
        return switch (status) {
            case BAD_REQUEST -> 400;
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND -> 404;
            case INTERNAL_ERROR -> 500;
        };
    }

    private ErrorStatusEnum mapStatus(int status) {
        return switch (status) {
            case 400 -> ErrorStatusEnum.BAD_REQUEST;
            case 401 -> ErrorStatusEnum.UNAUTHORIZED;
            case 403 -> ErrorStatusEnum.FORBIDDEN;
            case 404 -> ErrorStatusEnum.NOT_FOUND;
            default -> ErrorStatusEnum.INTERNAL_ERROR;
        };
    }
}
