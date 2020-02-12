package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
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

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
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

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUserDisplaynameAndImage(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdateVM) {
        User updated = userService.update(id, userUpdateVM);
        return new UserVM(updated);
    }

    /*
    * email-verification: resend, confirmation
    * */
    @PostMapping(path = "/users/email-verification/confirmation/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<?> renewAndResendEmailConfirmation(@PathVariable long id) {
        try {
            boolean isSentWithSuccess = userService.resendEmailById(id);
            if (isSentWithSuccess) {
                System.out.println("Email Resending was successfully!");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("An error has occurred on the process of sending email!");
            return ResponseEntity.ok(Collections.singletonMap("value", "FAILING"));
        }
        return null;
    }

    @GetMapping(path = "/users/email-verification/confirmationToken/{token}")
    public ResponseEntity verifyEmailToken(@PathVariable String token) {
        try {
            boolean isVerified = userService.verifyEmailToken(token);

            if (isVerified) {
                System.out.println("SUCCESS");
                return ResponseEntity.ok(Collections.singletonMap("value", "SUCCESS"));
            }
        } catch (Exception e) {
            System.out.println("FAILING");
//            return ResponseEntity.ok(Collections.singletonMap("value", "FAILING"));

//            Map<String, String> validationErrors = new HashMap<>(); 
//            validationErrors.put("ERROR", "ERROR")
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(Collections.singletonMap("value", "UNDEFINED"));
    }

//    @GetMapping(path = "/email-verification",
//            produces = {MediaType.APPLICATION_JSON_VALUE,
//                    MediaType.APPLICATION_XML_VALUE})
//    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
//
//        OperationStatusModel returnValue = new OperationStatusModel();
//        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
//
//        try {
//            boolean isVerifreturnValueied = userService.verifyEmailToken(token);
//
//            if (isVerified) {
//                System.out.println("SUCCESS");
//                returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
//            }
//
//        } catch (Exception e) {
//            System.out.println("FAILING");
//            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
//        }
//
//        return returnValue;
//    }
}
