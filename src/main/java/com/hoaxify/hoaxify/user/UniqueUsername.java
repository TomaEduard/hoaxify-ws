package com.hoaxify.hoaxify.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueUsernameValidator.class) // validation annotation
@Target(ElementType.FIELD) // define where the annotation can be use
@Retention(RetentionPolicy.RUNTIME)// tell JVM to persist annotation in runntime
public @interface UniqueUsername {
	
	String message() default "{hoaxify.constraints.username.UniqueUsername.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
