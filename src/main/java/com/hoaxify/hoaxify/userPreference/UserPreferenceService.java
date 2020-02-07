package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserPreferenceService {

    UserPreferenceRepository userPreferenceRepository;

    HoaxRepository hoaxRepository;

    UserRepository userRepository;

    UserService userService;

    public UserPreference saveUserPreference(User loggedInUser, UserPreference userPreference, long id) {
        Hoax inDBHoax = hoaxRepository.findById(id).get();
        User inDBUser = userRepository.findByUsername(loggedInUser.getUsername());

        // verify if preference already exist in db for update
        UserPreference inDB = userPreferenceRepository.findByHoaxIdAndUserId(inDBHoax.getId(), inDBUser.getId());
        if (inDB == null) {
            userPreference.setHoax(inDBHoax);
            userPreference.setUser(inDBUser);
            return userPreferenceRepository.save(userPreference);
        }

        inDB.setFavorite(userPreference.isFavorite());
        inDB.setLike(userPreference.isLike());
        inDB.setBookmark(userPreference.isBookmark());
        inDB.setHoax(inDBHoax);
        return userPreferenceRepository.save(inDB);
    }

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository, HoaxRepository hoaxRepository, UserRepository userRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.hoaxRepository = hoaxRepository;
        this.userRepository = userRepository;
    }

}