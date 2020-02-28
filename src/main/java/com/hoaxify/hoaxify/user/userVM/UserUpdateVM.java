package com.hoaxify.hoaxify.user.userVM;

import com.hoaxify.hoaxify.shared.FileSize;
import com.hoaxify.hoaxify.shared.ProfileImage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateVM {

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @ProfileImage
    @FileSize(max=1)
    private String image;
}
