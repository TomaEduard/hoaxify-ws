package com.hoaxify.hoaxify.Hoax;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaxRepository extends JpaRepository<Hoax, Long> {

    long countByUserUsername(String username);
}
