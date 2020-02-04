package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoaxSecurityService {

    final
    HoaxRepository hoaxRepository;

    public HoaxSecurityService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public boolean isAllowedToDelete(long hoaxId, User loggedInUser) {
        Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
        if (optionalHoax.isPresent()) {
            Hoax inDB = optionalHoax.get();
            return inDB.getUser().getId() == loggedInUser.getId();
        }
        return false;
    }
}
