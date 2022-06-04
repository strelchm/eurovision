package ru.strelchm.eurovision.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.strelchm.eurovision.api.exception.BadRequestException;
import ru.strelchm.eurovision.api.exception.NotFoundException;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@EnableWebMvc
@RestControllerAdvice
public class GlobalExceptionHandler {
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

    @NotNull
    private HashMap<String, String> getResponseFromException(Exception ex) {
        HashMap<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        ex.printStackTrace();
        return response;
    }
}
