package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @Autowired
    UserPreferenceRepository userPreferenceRepository;

    @Autowired
    UserPreferenceService userPreferenceService;

    @Autowired
    UserService userService;

    @PostMapping("/hoaxes")
    HoaxVM createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        return new HoaxVM(hoaxService.save(user, hoax)) ;

    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable, @CurrentUser User loggedInUser) {
        return hoaxService.getHoaxesOfUser(username, pageable).map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            // TODO: The duplicate code must be resolved
            if(loggedInUser != null) {
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
            return hoaxVM;
        });
    }

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

    @GetMapping("/hoaxes")
    public Page<HoaxVM> getAllHoaxes(Pageable pageable, @CurrentUser User loggedInUser) {
        return hoaxService.getAllHoaxes(pageable).map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            if(loggedInUser != null) {
                // find if loggedInUser have preference of this hoax and save it
                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
                if(userPreference != null){
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                }
                if (userPreference == null) {
                    UserPreference userPreferenceFalse = userPreferenceService.returnUserPreferenceIfNotExistWithoutSaving(loggedInUser, hoax);
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                }
            }
            return hoaxVM;
        });
    }

    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@CurrentUser User loggedInUser,
                                        @PathVariable long id, Pageable pageable,
                                        @RequestParam(name="direction", defaultValue="after") String direction,
                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {
        if(!direction.equalsIgnoreCase("after")) {
            // so here, for just to make this part readable instead of immediately returning this line,
            // lets assign it to a variable first.
            // think it like the implementation in @GetMapping("/hoaxes")
            // we have a page response. And we are mapping Hoax to HoaxVM in this part
            Page<HoaxVM> pageResponse = hoaxService.getOldHoaxes(id, pageable).map(hoax -> {
                HoaxVM hoaxVM = new HoaxVM(hoax);
                if (loggedInUser != null) {
                    UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
                    if(userPreference != null){
                        hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                    }
                    if (userPreference == null) {
                        UserPreference userPreferenceFalse = userPreferenceService.saveUserPreferenceIfNotExist(loggedInUser, hoax);
                        hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                    }
                }
                return hoaxVM; // you shouldn't be returning ResponseEntity.ok(hoaxVM) here
            });
            // then we can return our ResponseEntity with our page object
            return ResponseEntity.ok(pageResponse);
        }

        // the getNewHoaxes part is returning List<Hoax> .. so in this one it will be like
        Stream<Object> newHoaxes = hoaxService.getNewHoaxes(id, pageable).stream().map(hoax -> {
            HoaxVM hoaxVM = new HoaxVM(hoax);
            if (loggedInUser != null) {
                UserPreference userPreference = userPreferenceRepository.findByHoaxIdAndUserId(hoax.getId(), loggedInUser.getId());
                // find if loggedInUser have preference of this hoax and save it
                if (userPreference != null) {
                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
//                    HoaxVM hoaxVM = new HoaxVM(hoax);
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreference));
                }
                // if the user has no preference, will create a preference object with false values and return
                if (userPreference == null) {
                    UserPreference userPreferenceFalse = userPreferenceService.returnUserPreferenceIfNotExistWithoutSaving(loggedInUser, hoax);
                        hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
//                    HoaxVM hoaxVM = new HoaxVM(hoax);
//                    hoaxVM.setUserPreference(new UserPreferenceVM(userPreferenceFalse));
                }
            }
            return hoaxVM; // again returning hoaxVM here.. not ResponseEntity.
        });
        // then
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
        }
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
