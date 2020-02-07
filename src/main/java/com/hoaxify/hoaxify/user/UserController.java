package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
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

}
