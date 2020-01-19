package com.hoaxify.hoaxify.ui.transfer.response;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)  // because will use this annotation in methods
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal    // save the principal values into a class object
public @interface CurrentUser {

}
