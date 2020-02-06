package com.hoaxify.hoaxify.userPreferance;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.user.User;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity
public class UserPreference {

    @Id
    @GeneratedValue
    private long id;

    private boolean favorite;

    private boolean like;

    private boolean bookmark;

    @ManyToOne
    private User user;

    @ManyToOne
    private Hoax hoax;

}
