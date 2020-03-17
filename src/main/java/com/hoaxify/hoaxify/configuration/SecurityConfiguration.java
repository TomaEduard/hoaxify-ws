package com.hoaxify.hoaxify.configuration;

import com.hoaxify.hoaxify.security.CustomUserDetailsService;
import com.hoaxify.hoaxify.security.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable().authorizeRequests();
//        http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable().authorizeRequests().and()
                .formLogin().disable()
                .httpBasic().disable()
                .headers().frameOptions().disable().and()

//                .authorizeRequests().antMatchers("/api/1.0/login").permitAll()
//                    .anyRequest().authenticated();
//                    .antMatchers(HttpMethod.POST, "/api/1.0/users").permitAll()
//                    .antMatchers("/h2-console/**").permitAll()

                .authorizeRequests()
                .antMatchers("/",
                        "/error",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js").permitAll()

                .antMatchers("/images/**", "/api/1.0/hoaxes/upload", "/api/1.0/login", "/auth/**", "/oauth2/**").permitAll()
                .and()

                .authorizeRequests()

                    .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/1.0/hoaxes/**").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/1.0/hoaxes/{id:[0-9]+}").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/1.0/preference/{id:[0-9]+}").authenticated()

                    .anyRequest().permitAll();
                    // email-verification
//                    .antMatchers(HttpMethod.GET, "/api/1.0/users/email-verification/**").authenticated()
//                .and()
//                .authorizeRequests().anyRequest().permitAll();

        http.headers().frameOptions().disable();
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
