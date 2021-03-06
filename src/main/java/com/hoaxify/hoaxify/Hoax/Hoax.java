package com.hoaxify.hoaxify.Hoax;

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
@Entity
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 3, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;

    @OneToOne(mappedBy = "hoax", orphanRemoval = true)
    private FileAttachment attachment;

    @OneToMany(mappedBy = "hoax", orphanRemoval = true)
    private List<UserPreference> userPreference;

}
