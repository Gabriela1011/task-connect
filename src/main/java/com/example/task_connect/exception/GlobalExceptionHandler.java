package com.example.task_connect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    //handle account uniqueness
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException e, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; //409

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                request.getDescription(false) //path
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    //handle user not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND; // 404

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    //handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400

        //combines all @NotBlank/@Email error messages into one string
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
