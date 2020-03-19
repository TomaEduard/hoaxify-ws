package com.hoaxify.hoaxify.configuration;

import com.hoaxify.hoaxify.security.CustomUserDetailsService;
import com.hoaxify.hoaxify.security.TokenAuthenticationFilter;
import com.hoaxify.hoaxify.security.oauth2.CustomOAuth2UserService;
import com.hoaxify.hoaxify.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.hoaxify.hoaxify.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.hoaxify.hoaxify.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    TokenAuthenticationFilter tokenAuthenticationFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
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
                .antMatchers("/images/**", "/api/1.0/hoaxes/upload", "/api/1.0/login",
                        "/auth/**", "/oauth2/**", "/api/1.0/auth/**", "/api/1.0/oauth2/**",
                        "http://localhost:3000/oauth2/redirect]", "/oauth2/redirect").permitAll()
                .and()

                .authorizeRequests()
                    .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/1.0/hoaxes/**").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/1.0/hoaxes/{id:[0-9]+}").authenticated()
                    .antMatchers(HttpMethod.POST, "/api/1.0/preference/{id:[0-9]+}").authenticated()
                    .anyRequest().permitAll()
                    // email-verification
                    // .antMatchers(HttpMethod.GET, "/api/1.0/users/email-verification/**").authenticated()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/api/1.0/oauth2/authorize")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        http.headers().frameOptions().disable();
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }

}
