package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.shared.UniqueUsername;
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
import java.util.List;

@Data
@Entity
public class User implements UserDetails {

    private static final long serialVersionUID = 4074374728582967483L;

    @Id
    @GeneratedValue
    private long id;


    @NotNull(message = "{hoaxify.constraints.username.NotNull.message}")
    @Size(min = 4, max = 255)
    @Pattern(regexp=".+@.+\\..+", message="Please provide a valid email address")
    @UniqueUsername
    private String username;

    @NotNull
    @Size(min = 4, max = 255)
    private String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{hoaxify.constraints.password.Pattern.message}")
    private String password;

    private String image;

    private String emailVerificationToken = "";

    @NotNull
    private Boolean emailVerificationStatus = false;

    @OneToMany(mappedBy = "user")
    private List<Hoax> hoaxes;

    @OneToMany(mappedBy = "user")
    private List<UserPreference> userPreference;

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_USER");
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

}