package com.hoaxify.hoaxify.shared.response;

import com.hoaxify.hoaxify.user.AuthProvider;
import com.hoaxify.hoaxify.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Service
@NoArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String displayName;
    private String password;
    private Boolean emailVerificationStatus;
    private String image;
    private String jwt;
    private Collection<? extends GrantedAuthority> authorities;
    private AuthProvider provider;
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String username, String displayName, String image, String password, boolean emailVerificationStatus,
                         Collection<? extends GrantedAuthority> authorities, AuthProvider authProvider) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.image = image;
        this.password = password;
        this.emailVerificationStatus = emailVerificationStatus;
        this.authorities = authorities;
        this.provider = authProvider;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.
                singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getImage(),
                user.getPassword(),
                user.getEmailVerificationStatus(),
                authorities,
                user.getProvider()
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
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

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return username;
    }

    @Override
    public String getUsername() {
        return displayName;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }


}
