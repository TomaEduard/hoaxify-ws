package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1.0")
public class UserPreferenceController {

    @Autowired
    UserPreferenceService userPreferenceService;

    // 1. Current user    2. Id of hoax       3. UserPreference
    @PostMapping("/preference/{id:[0-9]+}")
    UserPreferenceResponse createUserPreference(@CurrentUser User loggedInUser,
                                         @RequestBody UserPreference userPreference,
                                         @PathVariable long id) {
        UserPreference inDB = userPreferenceService.saveUserPreference(loggedInUser, userPreference, id);

        ModelMapper modelMapper = new ModelMapper();
        UserPreferenceResponse returnValue = modelMapper.map(inDB, UserPreferenceResponse.class);
        return returnValue;
    }

}
