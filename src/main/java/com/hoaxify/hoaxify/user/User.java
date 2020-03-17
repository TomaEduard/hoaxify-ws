package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.shared.UniqueUsername;
import com.hoaxify.hoaxify.verificationToken.VerificationToken;
import com.hoaxify.hoaxify.userPreference.UserPreference;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.beans.Transient;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class User{

    private static final long serialVersionUID = 4074374728582967483L;

    @Id
    @GeneratedValue
    private long id;

    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    @Size(min = 5, max = 255)
    @Pattern(regexp=".+@.+\\..+", message="Please provide a valid email address")
    @UniqueUsername
    private String username;

    @NotNull
    @Size(min = 3, max = 255)
    private String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{hoaxify.constraints.password.Pattern.message}")
    private String password;

    private String image;

    private String emailVerificationToken = "";

    @NotNull
    private Boolean emailVerificationStatus = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @OneToOne(mappedBy = "user", orphanRemoval = true)
    private VerificationToken verificationToken;

    @OneToMany(mappedBy = "user")
    private List<Hoax> hoaxes;

    @OneToMany(mappedBy = "user")
    private List<UserPreference> userPreference;

}