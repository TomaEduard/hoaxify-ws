package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.security.CustomUserDetailsService;
import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.request.LoginRequest;
import com.hoaxify.hoaxify.shared.request.LoginRequestOAuth2;
import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

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

    @PostMapping("/oauth2/facebook")
    public ResponseEntity<?> authenticateUserFacebook(@RequestBody LoginRequestOAuth2 loginRequestOAuth2) throws Exception {

        // verify if email exist
        boolean result = userService.getUserByUsernameReturnBoolean(loginRequestOAuth2.getUsername());
        logger.info("Verify if " + loginRequestOAuth2.getUsername() + " exist: " + result);

        if (result) {
            logger.info(loginRequestOAuth2.getUsername() + " exist in DB");

            // get user from db and set image & displayName from loginRequestOAuth2 request if exist
            User inDB = userService.getUserByUsername(loginRequestOAuth2.getUsername());
            inDB.setImage(loginRequestOAuth2.getImage());
            inDB.setDisplayName(loginRequestOAuth2.getDisplayName());

            UserPrincipal user = customUserDetailsService.loadUserByUsername(inDB.getUsername());
            final String jwt = utils.generateToken(user);
            user.setJwt(jwt);
            return ResponseEntity.ok(user);

        } else {
            logger.info(loginRequestOAuth2.getUsername() + " does not exist in DB, it will be created");
            User user = userService.saveOAuth2(loginRequestOAuth2, AuthProvider.facebook);
            logger.info("created successfully!");
            return ResponseEntity.ok(new UserVM(user));
        }
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleAccessDeniedException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return new ApiError(401, "Access error", "/api/1.0/login");
    }
}
