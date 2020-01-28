package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.user.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HoaxService {

    final
    HoaxRepository hoaxRepository;

    public HoaxService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public void save(User user, Hoax hoax) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        hoaxRepository.save(hoax);
    }
}
