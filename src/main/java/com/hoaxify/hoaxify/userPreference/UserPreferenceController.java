package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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


    @PostMapping("/preference/favorite/{id:[0-9]+}")
    List<HoaxVM> getFavoritePrefferenceByUser(@PathVariable long id, @RequestBody UserPreferenceVM serPreferenceVM) {
        List<UserPreference> allFavoriteTrueByUser = userPreferenceService.getAllFavoriteTrueByUser(id, serPreferenceVM);

        // TODO: Need implement page
        ArrayList<HoaxVM> returnValue = new ArrayList<>();

        for (int i = 0; i < allFavoriteTrueByUser.size(); i++) {
            Hoax hoax = hoaxRepository.findByUserPreference(allFavoriteTrueByUser.get(i));

            HoaxVM hoaxVM = new HoaxVM(hoax);
            hoaxVM.setUserPreference(new UserPreferenceVM(allFavoriteTrueByUser.get(i)));
            returnValue.add(hoaxVM);
        }

        return returnValue;
    }

}
