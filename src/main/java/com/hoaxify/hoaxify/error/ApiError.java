package com.hoaxify.hoaxify.error;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ApiError {

    private long timestamp = new Date().getTime();

    private int status;

    private String message;

    private String url;

    public ApiError(int status, String message, String url) {
        this.status = status;
        this.message = message;
        this.url = url;
    }
}
