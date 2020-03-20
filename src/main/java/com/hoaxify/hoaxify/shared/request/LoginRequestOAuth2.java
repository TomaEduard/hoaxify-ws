package com.hoaxify.hoaxify.shared.request;

import lombok.Data;

@Data
public class LoginRequestOAuth2 {

    private String username;

    private String displayName;

    private String image;

    private String provider;

}
