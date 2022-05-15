package com.jfcdevs.app.core.api.exceptions;

public class EvenProcessingException extends RuntimeException{
    public EvenProcessingException(){

    }
    public EvenProcessingException(String message){
        super(message);
    }
    public EvenProcessingException(String message, Throwable cause){
        super(message, cause);
    }
    public EvenProcessingException(Throwable cause){
        super(cause);
    }
}
