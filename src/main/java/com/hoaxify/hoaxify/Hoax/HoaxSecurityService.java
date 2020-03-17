package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoaxSecurityService {

    final
    HoaxRepository hoaxRepository;

    public HoaxSecurityService(HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public boolean isAllowedToDelete(long hoaxId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Optional<Hoax> optionalHoax = hoaxRepository.findById(hoaxId);
        if (optionalHoax.isPresent()) {
            Hoax inDB = optionalHoax.get();
            return inDB.getUser().getId() == userPrincipal.getId();
        }
        return false;
    }
}
