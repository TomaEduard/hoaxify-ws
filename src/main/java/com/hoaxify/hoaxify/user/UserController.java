package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.shared.request.UpdateEmail;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import com.hoaxify.hoaxify.verificationToken.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    VerificationTokenService verificationTokenService;

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
        user.setTimestamp(new Date());
        userService.save(user);
        return new GenericResponse("User saved");
    }

    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable page) {
        return userService.getUser(loggedInUser, page).map(UserVM::new);
    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return new UserVM(user);
    }


    // TODO: need implement another endpoint for password
    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUserDisplaynameAndImage(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdateVM) {
        User updated = userService.update(id, userUpdateVM);
        return new UserVM(updated);
    }


    // generate new token, save it over the old one at db and send email with it to specific page for validated email
    @PostMapping(path = "/users/email-verification/confirmation/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> renewAndResendEmailConfirmation(@PathVariable long id) {
        try {
            boolean isSaveToDBAndSentWithSuccess = userService.resendEmailById(id);

            if (isSaveToDBAndSentWithSuccess) {
                System.out.println("Email Resending was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("value", "FAILING"));
        }
        return null;
    }
    // validated email page send token from #1 to this endpoint
    @GetMapping(path = "/users/email-verification/confirmationToken/{token}")
    public ResponseEntity verifyEmailTokenForEmailVerification(@PathVariable String token) {
        try {
            boolean isVerified = userService.verifyEmailToken(token);

            if (isVerified) {
                System.out.println("SUCCESS");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("FAILING");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(Collections.singletonMap("value", "UNDEFINED"));
    }

    // generate new token, save it over the old one at db and send email with it to specific page for change email
    // this page have 2 fields
    // generate new token and send email with it to specific page for validated email
    @PostMapping(path = "/users/email-verification/changeEmail/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> ChangeEmailToken(@PathVariable long id) {
        try {
//            boolean isSaveToDBAndSentWithSuccess = userService.changeEmailById(id);
            boolean isSaveToDBAndSentWithSuccess = verificationTokenService.changeEmailById(id);
            if (isSaveToDBAndSentWithSuccess) {
                System.out.println("Email ChangeEmailToken was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("value", "FAILING"));
        }

        return null;
    }
    @PostMapping(path = "/users/email-verification/changeEmailToken/{token}")
    public ResponseEntity verifyEmailTokenForChangeEmail(@CurrentUser User loggedInUser,
                                                         @PathVariable String token,
                                                         @Valid @RequestBody(required = false) UpdateEmail updateEmail ) {
        try {
            System.out.println("###");

            boolean isVerifiedAndDeleted = verificationTokenService.verifyEmailTokenChangeEmail(token);
            boolean isChangeEmail = userService.changeEmail(loggedInUser, updateEmail);
            if (isVerifiedAndDeleted && isChangeEmail) {
                System.out.println("SUCCESS");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("FAILING");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(Collections.singletonMap("value", "UNDEFINED"));
    }


}
