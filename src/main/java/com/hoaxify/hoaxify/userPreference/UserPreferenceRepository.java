package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    UserPreference findByHoaxIdAndUserId(long hoaxId, long userId);


    List<UserPreference> findByUserAndFavoriteTrueAndLikeTrueAndBookmarkTrue(Optional<User> user);
    List<UserPreference> findByUserAndFavoriteTrueAndLikeTrue(Optional<User> user);
    List<UserPreference> findByUserAndFavoriteTrueAndBookmarkTrue(Optional<User> user);
    List<UserPreference> findByUserAndLikeTrueAndBookmarkTrue(Optional<User> user);
    List<UserPreference> findByUserAndFavoriteTrue(Optional<User> user);
    List<UserPreference> findByUserAndLikeTrue(Optional<User> user);
    List<UserPreference> findByUserAndBookmarkTrue(Optional<User> user);
    List<UserPreference> findByUser(Optional<User> user);

}
