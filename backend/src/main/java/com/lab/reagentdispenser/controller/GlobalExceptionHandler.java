package com.lab.reagentdispenser.controller;

import com.lab.reagentdispenser.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		log.error("IllegalArgumentException: {}", ex.getMessage());
		ErrorResponse error = ErrorResponse.of(
				"Invalid Request",
				ex.getMessage(),
				HttpStatus.BAD_REQUEST.value()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
		log.error("IllegalStateException: {}", ex.getMessage());
		ErrorResponse error = ErrorResponse.of(
				"Invalid State",
				ex.getMessage(),
				HttpStatus.CONFLICT.value()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		String details = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		
		log.error("Validation error: {}", details);
		ErrorResponse error = ErrorResponse.of(
				"Validation Failed",
				details,
				HttpStatus.BAD_REQUEST.value()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.error("Unexpected error", ex);
		ErrorResponse error = ErrorResponse.of(
				"Internal Server Error",
				ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
