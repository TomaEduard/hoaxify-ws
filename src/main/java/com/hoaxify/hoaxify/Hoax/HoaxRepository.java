package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.userPreference.UserPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//public interface HoaxRepository extends JpaRepository<Hoax, Long>, JpaSpecificationExecutor<Hoax> {
public interface HoaxRepository extends JpaRepository<Hoax, Long> {

//    Page<Hoax> findAlla(Pageable pageable, Sort sort);

    Page<Hoax> findByUser(User user, Pageable pageable);

    Page<Hoax> findByIdLessThan(long id, Pageable pageable);

    List<Hoax> findByIdGreaterThanAndUserPreference(long id, Sort sort, long userPreferenceId);

    List<Hoax> findByIdGreaterThan(long id, Sort sort);

    Page<Hoax> findByIdLessThanAndUser(long id, User user, Pageable pageable);

    List<Hoax> findByIdGreaterThanAndUser(long id, User user, Sort sort);

    long countByIdGreaterThan(long id);

    long countByIdGreaterThanAndUser(long id, User user);

//    List<Hoax> findByIdGreaterThanAndUser

//    List<Hoax> findByUserPreference

    Hoax findByUserPreference(UserPreference userPreference);
}
