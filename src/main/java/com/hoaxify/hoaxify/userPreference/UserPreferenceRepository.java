package com.hoaxify.hoaxify.userPreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    UserPreference findByHoaxIdAndUserId(long hoaxId, long userId);
}
