package be.intecbrussel.shoppinglist.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({MissingDataException.class})
    public ResponseEntity<Map<String, String>> handleMissingDataException(MissingDataException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(error);
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(errors);
    }

    @ExceptionHandler({DuplicateEnrollmentException.class})
    public ResponseEntity<Map<String, String>> handleDuplicateEnrollmentException(
            DuplicateEnrollmentException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(error);
    }

    @ExceptionHandler({UnauthorizedActionException.class})
    public ResponseEntity<Map<String, String>> handleUnauthorizedActionException(
            UnauthorizedActionException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) //403
                .body(error);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) //404
                .body(error);
    }

    // Must be last.
    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) //500
                .body(exception.getMessage());
    }

}
