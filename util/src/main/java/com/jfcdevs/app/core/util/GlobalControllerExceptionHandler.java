package com.jfcdevs.app.core.util;

import com.jfcdevs.app.core.api.exceptions.InvalidInputException;
import com.jfcdevs.app.core.api.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(
            ServerHttpRequest request, NotFoundException ex
    ){
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputException(
            ServerHttpRequest request, InvalidInputException ex
    ){
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex);
    }


    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus,
                                              ServerHttpRequest request,
                                              Exception ex){
        final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();
        log.info("Returning HTTP status: {} for path: {}, message: {}", httpStatus,path,message);
        return new HttpErrorInfo(httpStatus, path, message);
    }
}
