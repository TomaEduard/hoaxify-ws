package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;

@RestController
public class LoginController {

    @PostMapping("/api/1.0/login")
    void handleLogin() {

    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}
