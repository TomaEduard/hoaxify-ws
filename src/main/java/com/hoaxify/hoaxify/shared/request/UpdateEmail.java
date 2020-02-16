package com.hoaxify.hoaxify.shared.request;

import com.hoaxify.hoaxify.shared.UniqueUsername;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateEmail {

    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    @Size(min = 4, max = 255)
    @Pattern(regexp=".+@.+\\..+", message="Please provide a valid email address")
    @UniqueUsername
    private String newEmail;

}
