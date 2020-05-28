package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import com.hoaxify.hoaxify.userPreference.UserPreference;
import com.hoaxify.hoaxify.userPreference.UserPreferenceRepository;
import com.hoaxify.hoaxify.userPreference.UserPreferenceService;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @Autowired
    UserPreferenceRepository userPreferenceRepository;

    @Autowired
    UserPreferenceService userPreferenceService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/hoaxes")
    HoaxVM createHoax(@Valid @RequestBody Hoax hoax, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return new HoaxVM(hoaxService.save(userPrincipal.getId(), hoax));
//        return new HoaxVM(hoaxService.save(authentication.getName(), hoax));
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable, @CurrentUser User loggedInUser) {
        return hoaxService.getHoaxesOfUser(username, pageable).map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            // TODO: The duplicate code must be resolved
            if (loggedInUser != null) {
                // find if loggedInUser have preference of this hoax and save it
                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
                if (userPreference != null) {
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                }
                // if the user has no preference, will create a preference object with false values
                if (userPreference == null) {
                    UserPreference userPreferenceFalse = userPreferenceService.saveUserPreferenceIfNotExist(loggedInUser, hoax);
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                }
            }
            return hoaxVM;
        });
    }

    @DeleteMapping("/hoaxes/{id:[0-9]+}")
    @PreAuthorize("@hoaxSecurityService.isAllowedToDelete(#id, principal)")
    GenericResponse deleteHoax(@PathVariable long id) {
        hoaxService.deleteHoax(id);
        return new GenericResponse("Hoax is removed");
    }

    @GetMapping("/hoaxes")
    public Page<HoaxVM> getAllHoaxes(Pageable pageable, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return hoaxService.getAllHoaxes(pageable).map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            if (userPrincipal != null) {
                User userDB = userRepository.findById(userPrincipal.getId()).get();

                // find if loggedInUser have preference of this hoax and save it
                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), userDB.getId());
                if (userPreference != null) {
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                }
                if (userPreference == null) {
                    UserPreference userPreferenceFalse = userPreferenceService.returnUserPreferenceIfNotExistWithoutSaving(userDB, hoax);
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                }
            }
            return hoaxVM;
        });
    }

    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                        @PathVariable long id, Pageable pageable,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction,
                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {

        if (count) {
            long newHoaxCount = hoaxService.getNewHoaxesCount(id);
            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
        }
        if (!direction.equalsIgnoreCase("after")) {
            Page<HoaxVM> pageResponse = hoaxService.getOldHoaxes(id, pageable)
                    .map(hoax -> {
                        HoaxVM hoaxVM = new HoaxVM(hoax);

                        if (userPrincipal != null) {
                            User userDB = userRepository.findById(userPrincipal.getId()).get();

                            UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), userDB.getId());
                            if (userPreference != null) {
                                hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                            }
                            if (userPreference == null) {
                                UserPreference userPreferenceFalse = userPreferenceService.saveUserPreferenceIfNotExist(userDB, hoax);
                                hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                            }
                        }
                        return hoaxVM;
                    });
            return ResponseEntity.ok(pageResponse);
        }

        // the getNewHoaxes part is returning List<Hoax> .. so in this one it will be like
        Stream<Object> newHoaxes = hoaxService.getNewHoaxes(id, pageable).stream().map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            if (userPrincipal != null) {
                User userDB = userRepository.findById(userPrincipal.getId()).get();

                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), userDB.getId());
                if (userPreference != null) {
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                }

                // if the user has no preference, will create a preference object with false values and return
                if (userPreference == null) {
                    UserPreference userPreferenceFalse = userPreferenceService.returnUserPreferenceIfNotExistWithoutSaving(userDB, hoax);
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
//                    HoaxVM hoaxVM = new HoaxVM(hoax);
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                }
            }
            return hoaxVM;
        });
        return ResponseEntity.ok(newHoaxes);
    }

}
