package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserPreferenceService {

    UserPreferenceRepository userPreferenceRepository;

    HoaxRepository hoaxRepository;

    UserRepository userRepository;

    UserService userService;

    public UserPreference saveUserPreferenceIfNotExist(User loggedInUser, Hoax hoax) {
        User inDBUser = userRepository.findByUsername(loggedInUser.getUsername());

        UserPreference userPreference = new UserPreference();
        userPreference.setHoax(hoax);
        userPreference.setUser(inDBUser);
        // no need to set false cuz the the Entity have default false
//        userPreference.setFavorite(false);
//        userPreference.setLike(false);
//        userPreference.setBookmark(false);

        return userPreferenceRepository.save(userPreference);
    }

    public UserPreference returnUserPreferenceIfNotExistWithoutSaving(User loggedInUser, Hoax hoax) {
        User inDBUser = userRepository.findByUsername(loggedInUser.getUsername());

        UserPreference userPreference = new UserPreference();
        userPreference.setHoax(hoax);
        userPreference.setUser(inDBUser);
        userPreference.setFavorite(false);
        userPreference.setLike(false);
        userPreference.setBookmark(false);

        return userPreference;
    }

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

    public List<UserPreference> getAllPreferenceTrueByUser(long id, UserPreferenceVM request) {
        Optional<User> userDB = userRepository.findById(id);

        if (request.isFavorite() &&
            request.isLike() &&
            request.isBookmark()) {
           return userPreferenceRepository.findByUserAndFavoriteTrueAndLikeTrueAndBookmarkTrue(userDB);

        } else if (request.isFavorite() &&
            request.isLike()) {
            return userPreferenceRepository.findByUserAndFavoriteTrueAndLikeTrue(userDB);
        } else if (request.isFavorite() &&
            request.isBookmark()) {
            return userPreferenceRepository.findByUserAndFavoriteTrueAndBookmarkTrue(userDB);
        } else if (request.isLike() &&
                request.isBookmark()) {
            return userPreferenceRepository.findByUserAndLikeTrueAndBookmarkTrue(userDB);

        } else if (request.isFavorite()) {
            return userPreferenceRepository.findByUserAndFavoriteTrue(userDB);
        } else if (request.isLike()) {
            return userPreferenceRepository.findByUserAndLikeTrue(userDB);
        } else if (request.isBookmark()) {
            return userPreferenceRepository.findByUserAndBookmarkTrue(userDB);
        } else {
            return userPreferenceRepository.findByUserAndFavoriteTrueOrBookmarkTrueOrLikeTrue(userDB);
        }

    }

//    public List<UserPreferenceWithHoax> getAllFavoriteTrueByUserWithHoax(long id) {
//        Optional<User> userDB = userRepository.findById(id);
//
//        return userPreferenceRepository.findByUserAndFavoriteTrue(userDB);
//    }
}