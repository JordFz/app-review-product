package com.jfcdevs.app.core.util;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;


@NoArgsConstructor(force = true)
@Getter
@Setter
public class HttpErrorInfo {


    private final ZonedDateTime timestamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this.timestamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
