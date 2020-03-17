package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.security.CustomUserDetailsService;
import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.request.LoginRequest;
import com.hoaxify.hoaxify.shared.response.AuthenticationResponse;
import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private Utils utils;

    // for basic auth
/*    @PostMapping("/login")
    UserVM handleLogin(@CurrentUser User loggedInUser) {
        return new UserVM(loggedInUser);
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws Exception {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())

            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        UserPrincipal user = customUserDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String jwt = utils.generateToken(user);
        user.setJwt(jwt);

        return ResponseEntity.ok(user);
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}
