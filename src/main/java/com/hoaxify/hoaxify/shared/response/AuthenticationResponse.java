package com.hoaxify.hoaxify.shared.response;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

public class AuthenticationResponse implements Serializable {

    private final String jwt;
    private UserDetails userDetails;

    public AuthenticationResponse(String jwt, UserDetails userDetails) {
        this.jwt = jwt;
        this.userDetails = userDetails;
    }

    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}
