package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/1.0")
@CrossOrigin
public class UserPreferenceController {

    @Autowired
    UserPreferenceService userPreferenceService;

    @Autowired
    HoaxRepository hoaxRepository;

    // 1. Current user    2. Id of hoax       3. UserPreference
    @PostMapping("/preference/{userId:[0-9]+}")
    UserPreferenceResponse createUserPreference(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @RequestBody UserPreference userPreference,
                                         @PathVariable long userId) {
        UserPreference inDB = userPreferenceService.saveUserPreference(userPrincipal, userPreference, userId);

        ModelMapper modelMapper = new ModelMapper();
        UserPreferenceResponse returnValue = modelMapper.map(inDB, UserPreferenceResponse.class);
        return returnValue;
    }

    @PostMapping("/preference/filter/{userId:[0-9]+}")
    List<HoaxVM> getAllPrefferenceAndFilterByUser(@PathVariable long userId, @RequestBody UserPreferenceVM serPreferenceVM) {
        List<UserPreference> allPreferenceTrueByUser = userPreferenceService.getAllPreferenceTrueByUser(userId, serPreferenceVM);

        // TODO: Need implement page
        ArrayList<HoaxVM> returnValue = new ArrayList<>();

        for (int i = 0; i < allPreferenceTrueByUser.size(); i++) {
            Hoax hoax = hoaxRepository.findByUserPreference(allPreferenceTrueByUser.get(i));

            HoaxVM hoaxVM = new HoaxVM(hoax);
            hoaxVM.setUserPreference(new UserPreferenceVM(allPreferenceTrueByUser.get(i)));
            returnValue.add(hoaxVM);
        }

        return returnValue;
    }

}
