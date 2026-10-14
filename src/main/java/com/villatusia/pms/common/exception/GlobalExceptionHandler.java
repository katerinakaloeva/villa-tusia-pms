package com.villatusia.pms.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePropertyNotFound(
            PropertyNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Property not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(InvalidReservationDatesException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReservationDates(
            InvalidReservationDatesException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid reservation dates");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(MinimumStayNotMetException.class)
    public ResponseEntity<ProblemDetail> handleMinimumStayNotMet(
            MinimumStayNotMetException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Minimum stay not met");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(ReservationUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleReservationUnavailable(
            ReservationUnavailableException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Reservation dates unavailable");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(PropertyInactiveException.class)
    public ResponseEntity<ProblemDetail> handlePropertyInactive(
            PropertyInactiveException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Property is inactive");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(GuestCountExceedsPropertyLimitException.class)
    public ResponseEntity<ProblemDetail> handleGuestCountExceedsPropertyLimit(
            GuestCountExceedsPropertyLimitException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Guest count exceeds property limit");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more fields are invalid."
        );

        problem.setTitle("Request validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("errors", errors);

        return ResponseEntity
                .badRequest()
                .body(problem);
    }
}
