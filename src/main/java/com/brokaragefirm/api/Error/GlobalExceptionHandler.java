package com.brokaragefirm.api.Error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.brokaragefirm.api.Error.Errors.BadRequestException;
import com.brokaragefirm.api.Error.Errors.ForbiddenException;
import com.brokaragefirm.api.Error.Errors.InsufficientBalanceException;
import com.brokaragefirm.api.Error.Errors.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), path(req)));
  }

  @ExceptionHandler({ BadRequestException.class, MethodArgumentNotValidException.class })
  public ResponseEntity<ApiError> handleBadRequest(Exception ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), path(req)));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiError.of(HttpStatus.FORBIDDEN, ex.getMessage(), path(req)));
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<ApiError> handleInsufficient(InsufficientBalanceException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), path(req)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleOther(Exception ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), path(req)));
  }

  private static String path(WebRequest req) {
    return (req instanceof org.springframework.web.context.request.ServletWebRequest sw)
        ? sw.getRequest().getRequestURI()
        : req.getDescription(false);
  }

}
