package br.com.petrepet.application.advicer;

import br.com.petrepet.application.exception.PedidoNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ValidationErrorHandler {

    @Autowired
    private MessageSource messageSource;


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationErrorsOutputDto handleValidationError(MethodArgumentNotValidException exception) {

        List<ObjectError> globalErrors = exception.getBindingResult().getGlobalErrors();
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        return buildValidationErrors(globalErrors,
                fieldErrors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ValidationErrorsOutputDto handleValidationError(BindException exception) {

        List<ObjectError> globalErrors = exception.getBindingResult().getGlobalErrors();
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        return buildValidationErrors(globalErrors,
                fieldErrors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ValidationErrorsOutputDto handleValidationError(HttpMessageNotReadableException exception) {
        log.error("Problema na de desserializar o objeto",exception);

        InvalidFormatException invalidFormat = (InvalidFormatException) exception.getCause();

        List<ObjectError> globalErrors = List.of(new ObjectError("", invalidFormat.getValue()+" não é um valor válido"));
        List<FieldError> fieldErrors = List.of();

        return buildValidationErrors(globalErrors,
                fieldErrors);
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(NoSuchElementException.class)
    public FieldErrorOutputDto handleValidationError(NoSuchElementException exception) {

        return new FieldErrorOutputDto(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(IllegalArgumentException.class)
    public FieldErrorOutputDto handleValidationError(IllegalArgumentException exception) {

        return new FieldErrorOutputDto(exception.getMessage());
    }

    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<ErroNegocioDto> teste(PedidoNotFoundException pedidoNotFoundException){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroNegocioDto(
                pedidoNotFoundException.getMessage(),
                HttpStatus.BAD_REQUEST,
                new Date(),
                pedidoNotFoundException.getCause().getMessage()));
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( ResponseStatusException.class)
    public FieldErrorOutputDto handleValidationError( ResponseStatusException exception) {

        return new FieldErrorOutputDto(exception.getMessage());
    }





    private ValidationErrorsOutputDto buildValidationErrors(List<ObjectError> globalErrors,
                                                            List<FieldError> fieldErrors) {
        ValidationErrorsOutputDto validationErrors = new ValidationErrorsOutputDto();

        globalErrors.forEach(error -> validationErrors.addError(getErrorMessage(error)));

        fieldErrors.forEach(error -> {
            String errorMessage = getErrorMessage(error);
            validationErrors.addFieldError(error.getField(), errorMessage);
        });
        return validationErrors;
    }

    private String getErrorMessage(ObjectError error) {
        return messageSource.getMessage(error, LocaleContextHolder.getLocale());
    }


}
