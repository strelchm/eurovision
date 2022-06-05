package ru.strelchm.gateway.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.strelchm.gateway.exception.BadRequestException;
import ru.strelchm.gateway.exception.NotFoundException;
import ru.strelchm.gateway.exception.VoteServiceException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AbstractController {
    private Logger LOG  = Logger.getLogger(AbstractController.class.getName());

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequestExceptions(Exception ex) {
        return getResponseFromException(ex);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundExceptions(Exception ex) {
        return getResponseFromException(ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleIntervalServerErrorExceptions(Exception ex) {
        return getResponseFromException(ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIntervalServerErrorExceptions(MethodArgumentTypeMismatchException ex) {
        return getResponseFromException(ex, String.format("Parameter %s is invalid", ex.getName()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIntervalServerErrorExceptions(ConstraintViolationException ex) {
        return getResponseFromException(ex, String.format("Parameters %s are invalid", ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(Path::toString)
            .collect(Collectors.joining(","))));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIntervalServerErrorExceptions(MethodArgumentNotValidException ex) {
        return getResponseFromException(ex, String.format("Parameters %s are invalid",
            ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField).distinct()
                .collect(Collectors.joining(",")))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIntervalServerErrorExceptions(HttpMessageNotReadableException ex) {
        return getResponseFromException(ex,
            ex.getCause().getMessage()
        );
    }

    @ExceptionHandler(VoteServiceException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIntervalServerErrorExceptions(VoteServiceException ex) {
        return getResponseFromException(ex,
            ex.getCause().getMessage()
        );
    }

    @NotNull
    private HashMap<String, String> getResponseFromException(Exception ex, String exMessage) {
        HashMap<String, String> response = new HashMap<>();
        response.put("message", exMessage);
        ex.printStackTrace();
        return response;
    }

    @NotNull
    private HashMap<String, String> getResponseFromException(Exception ex) {
        return getResponseFromException(ex, ex.getMessage());
    }
}
