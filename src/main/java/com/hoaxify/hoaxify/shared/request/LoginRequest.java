package com.hoaxify.hoaxify.shared.request;

import lombok.Data;

import javax.validation.constraints.Email;
import java.io.Serializable;

@Data
public class LoginRequest implements Serializable {

    @Email
    private String username;

    private String password;

}
