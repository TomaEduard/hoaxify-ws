package com.hoaxify.hoaxify.shared.request;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.userPreference.UserPreference;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class HoaxRequest {

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private FileAttachment attachment;

    private long userId;



}
