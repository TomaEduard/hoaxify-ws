package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
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

//    @GetMapping("/users/{username}/hoaxes")

//    @GetMapping("/users/{username}/hoaxes")
////    Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable, @CurrentUser User loggedInUser) {
////            Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable, @PathVariable long id) {
//            Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable, Authentication authentication
//            ) {
//        return hoaxService.getHoaxesOfUser(username, pageable).map(hoax -> {
//            HoaxVM hoaxVM = new HoaxVM(hoax);
////            if (loggedInUser != null) {
//            if (authentication.getP != null) {
//                // find if loggedInUser have preference of this hoax and save it
//                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
//                if (userPreference != null) {
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
//                }
//                // if the user has no preference, will create a preference object with false values
//                if (userPreference == null) {
//                    UserPreference userPreferenceFalse = userPreferenceService.saveUserPreferenceIfNotExist(loggedInUser, hoax);
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
//                }
//            }
//            return hoaxVM;
//        });
//    }

    @DeleteMapping("/hoaxes/{id:[0-9]+}")
    @PreAuthorize("@hoaxSecurityService.isAllowedToDelete(#id, principal)")
    GenericResponse deleteHoax(@PathVariable long id) {
        hoaxService.deleteHoax(id);
        return new GenericResponse("Hoax is removed");
    }

/*    // without preferences
    @GetMapping("/hoaxes")
    public Page<HoaxVM> getAllHoaxes(Pageable pageable, @CurrentUser User user) {
        Page<HoaxVM> hoaxVMS = hoaxService.getAllHoaxes(pageable, user).map(HoaxVM::new);
        return hoaxVMS;
//        Page<Hoax> allHoaxes = hoaxService.getAllHoaxes(pageable, user);
//
//        Type listType = new TypeToken<Page<HoaxVM>>(){}.getType();
//        return (Page<HoaxVM>) new ModelMapper().<Object>map(allHoaxes, listType);
    }*/

    //    @GetMapping("/hoaxes")
//    public Page<HoaxVM> getAllHoaxes(Pageable pageable, @CurrentUser User loggedInUser) {
//        return hoaxService.getAllHoaxes(pageable).map(hoax -> {
//            HoaxVM hoaxVM = new HoaxVM(hoax);
//            if (loggedInUser != null) {
//                // find if loggedInUser have preference of this hoax and save it
//                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
//                if (userPreference != null) {
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
//                }
//                if (userPreference == null) {
//                    UserPreference userPreferenceFalse = userPreferenceService.returnUserPreferenceIfNotExistWithoutSaving(loggedInUser, hoax);
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
//                }
//            }
//            return hoaxVM;
//        });
//    }
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

/*
    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@CurrentUser User loggedInUser,
                                        @PathVariable long id, Pageable pageable,
                                        @RequestParam(name="direction", defaultValue="after") String direction,
                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {

        if(!direction.equalsIgnoreCase("after")) {
            return (ResponseEntity<?>) hoaxService.getOldHoaxes(id, pageable).map(hoax -> {
                HoaxVM hoaxVM = new HoaxVM(hoax);

                if (loggedInUser != null) {
                    // find if loggedInUser have preference of this hoax and save it
                    UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
                    if(userPreference != null){
                        hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                    }
                    // if the user has no preference, will create a preference object with false values
                    if (userPreference == null) {
                        UserPreference userPreferenceFalse = userPreferenceService.saveUserPreferenceIfNotExist(loggedInUser, hoax);
                        hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                    }
                }
                return ResponseEntity.ok(hoaxVM);
            });
        }count
        if (count) {
            long newHoaxCount = hoaxService.getNewHoaxesCount(id);
            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
        }

        List<HoaxVM> newHoaxes = hoaxService.getNewHoaxes(id, pageable).stream().map(HoaxVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newHoaxes);

        //        if(!direction.equalsIgnoreCase("after")) {
//            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, pageable).map(HoaxVM::new));
//        }

//        List<HoaxVM> newHoaxes = hoaxService.getNewHoaxes(id, pageable).stream().map(HoaxVM::new).collect(Collectors.toList());
//        List<Hoax> inDB = hoaxService.getNewHoaxes(id, pageable, loggedInUser);
//        ModelMapper modelMapper = new ModelMapper();
//        Type listType = new TypeToken<List<HoaxVM>>(){}.getType();
//        List<HoaxVM> newHoaxes = modelMapper.map(inDB, listType);
//        return ResponseEntity.ok(newHoaxes);
    }
    */

//    @GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
//    ResponseEntity<?> getHoaxesRelativeForUser(@PathVariable String username,
//                                     @PathVariable long id, Pageable pageable,
//                                     @RequestParam(name="direction", defaultValue="after") String direction,
//                                     @RequestParam(name = "count", defaultValue = "false", required = false) boolean count){
//        if(!direction.equalsIgnoreCase("after")) {
//            return ResponseEntity.ok(hoaxService.getOldHoaxesOfUser(id, username, pageable).map(HoaxVM::new));
//        }
//        if (count) {
//            long newHoaxCount = hoaxService.getNewHoaxesCountOfUser(id, username);
//            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
//        }
//        List<HoaxVM> newHoaxes = hoaxService.getNewHoaxesOfUser(id, username, pageable)
//                .stream().map(HoaxVM::new).collect(Collectors.toList());
//
//        return ResponseEntity.ok(newHoaxes);
//    }

//    @GetMapping({"/hoaxes/{id:[0-9]+}", "/users/{username}/hoaxes/{id:[0-9]+}"})
//    ResponseEntity<?> getHoaxesRelative(@PathVariable long id,
//                                        @PathVariable(required = false) String username,
//                                        Pageable pageable,
//                                        @RequestParam(name="direction", defaultValue="after") String direction,
//                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {
//        if(!direction.equalsIgnoreCase("after")) {
//            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, username ,pageable).map(HoaxVM::new));
//        }
//        if (count) {
//            long newHoaxCount = hoaxService.getNewHoaxesCount(id, username);
//            return ResponseEntity.ok(Collections.singletonMap("count", newHoaxCount));
//        }
//        List<HoaxVM> newHoaxes = hoaxService.getNewHoaxes(id, username, pageable)
//                .stream().map(HoaxVM::new).collect(Collectors.toList());
//        return ResponseEntity.ok(newHoaxes);
//    }
}
