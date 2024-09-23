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

    /**
     * Maneja excepciones relacionadas con la validación de datos (BAD_REQUEST).
     *
     * @param e Excepción que se generó
     * @return ErrorResponse con el mensaje de error
     */
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final Exception e) {
        log.error("----- Excepción " + e.getClass() + " causó estado BAD_REQUEST: " + getStackTrace(e));

        return new ErrorResponse("Error de validación: " + e.getMessage()); // Mensaje traducido
    }

    /**
     * Maneja la excepción cuando una orden no es encontrada (NOT_FOUND).
     *
     * @param e Excepción de tipo OrderNotFoundException
     * @return ErrorResponse con el mensaje de error
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final OrderNotFoundException e) {
        log.error("----- Error " + e.getClass() + " causó estado NOT_FOUND: " + getStackTrace(e));

        return new ErrorResponse("Orden no encontrada: " + e.getMessage()); // Mensaje traducido
    }

    /**
     * Maneja conflictos en la integridad de los datos (CONFLICT).
     *
     * @param e Excepción relacionada con violaciones de integridad de datos
     * @return ErrorResponse con el mensaje de error
     */
    @ExceptionHandler({
            DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflicts(final Exception e) {
        log.error("----- Error " + e.getClass() + " causó estado CONFLICT: " + getStackTrace(e));

        return new ErrorResponse("Conflicto de datos: " + e.getMessage()); // Mensaje traducido
    }

    /**
     * Maneja cualquier otro tipo de error (INTERNAL_SERVER_ERROR).
     *
     * @param e Excepción que se generó
     * @return ErrorResponse con el mensaje de error general
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final Throwable e) {
        log.error("----- Error " + e.getClass() + " causó estado INTERNAL_SERVER_ERROR: " + getStackTrace(e));

        return new ErrorResponse("Error desconocido al procesar la orden"); // Mensaje traducido
    }

    /**
     * Obtiene el stack trace de la excepción como cadena de texto.
     *
     * @param e Excepción
     * @return Stack trace en formato de cadena
     */
    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
