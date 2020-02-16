package com.hoaxify.hoaxify.verificationToken;

import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.request.UpdateEmail;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationTokenService {

    VerificationTokenRepository verificationTokenRepository;

    UserRepository userRepository;

    UserService userService;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    public void saveChangeEmailToken(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    public boolean verifyEmailTokenChangeEmail(String token) {
        boolean returnValue = false;
        boolean hasTokenExpired = Utils.hasTokenExpired(token);

        if (!hasTokenExpired) {
            VerificationToken verificationTokenByChangeEmailToken = verificationTokenRepository.findByChangeEmailToken(token);
            verificationTokenRepository.deleteById(verificationTokenByChangeEmailToken.getId());
            returnValue = true;
        }

        return returnValue;
    }
}
