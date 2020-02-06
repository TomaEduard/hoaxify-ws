package com.hoaxify.hoaxify.userPreference;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.user.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(name="favorite")
    private boolean favorite = false;

    @NotNull
    @Column(name="likes")
    private boolean like = false;

    @NotNull
    @Column(name="bookmarks")
    private boolean bookmark = false;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "hoax_id")
    private Hoax hoax;

}