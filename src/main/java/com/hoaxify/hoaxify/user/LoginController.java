package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class LoginController {

    @PostMapping("/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return new UserVM(loggedInUser);
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}
