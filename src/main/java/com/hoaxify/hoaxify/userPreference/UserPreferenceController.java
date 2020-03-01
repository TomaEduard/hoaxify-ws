package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    @PostMapping("/preference/{id:[0-9]+}")
    UserPreferenceResponse createUserPreference(@CurrentUser User loggedInUser,
                                         @RequestBody UserPreference userPreference,
                                         @PathVariable long id) {
        UserPreference inDB = userPreferenceService.saveUserPreference(loggedInUser, userPreference, id);

        ModelMapper modelMapper = new ModelMapper();
        UserPreferenceResponse returnValue = modelMapper.map(inDB, UserPreferenceResponse.class);
        return returnValue;
    }


    @PostMapping("/preference/filter/{id:[0-9]+}")
    List<HoaxVM> getAllPrefferenceAndFilterByUser(@PathVariable long id, @RequestBody UserPreferenceVM serPreferenceVM) {
        List<UserPreference> allPreferenceTrueByUser = userPreferenceService.getAllPreferenceTrueByUser(id, serPreferenceVM);

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
