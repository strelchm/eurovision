package ru.strelchm.gateway.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.strelchm.gateway.exception.BadRequestException;
import ru.strelchm.gateway.exception.NotFoundException;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

    @NotNull
    private HashMap<String, String> getResponseFromException(Exception ex) {
        HashMap<String, String> response = new HashMap<>();

        response.put(
            "message",
            ex.getMessage() == null && ex instanceof InvocationTargetException ?
                ((InvocationTargetException) ex).getTargetException().getMessage() :
                ex.getMessage()
        );
        ex.printStackTrace();
        return response;
    }
}
