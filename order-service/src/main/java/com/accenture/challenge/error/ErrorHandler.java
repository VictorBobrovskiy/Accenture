package com.accenture.challenge.error;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;


@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class, IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final Exception e) {

        log.error("----- Exception " + e.getClass() + " caused BAD_REQUEST status" + getStackTrace(e));

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final OrderNotFoundException e) {

        log.error("----- Error " + e.getClass() + " caused NOT_FOUND status" + getStackTrace(e));

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflicts(final Exception e) {

        log.error("----- Error " + e.getClass() + " caused CONFLICT status");

        return new ErrorResponse(e.getMessage());
    }




    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final Throwable e) {

        log.error("----- Error " + e.getClass() + " caused INTERNAL_SERVER_ERROR status" + getStackTrace(e));

        return new ErrorResponse("Unknown error processing order");
    }


    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}