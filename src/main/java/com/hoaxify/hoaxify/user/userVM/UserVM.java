package com.hoaxify.hoaxify.user.userVM;

import com.hoaxify.hoaxify.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVM {

    private long id;

    private String username;

    private String displayName;

    private String image;

    private Boolean emailVerificationStatus;

    public UserVM(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setDisplayName(user.getDisplayName());
        this.setImage(user.getImage());
        this.setEmailVerificationStatus(user.getEmailVerificationStatus());
    }

}
