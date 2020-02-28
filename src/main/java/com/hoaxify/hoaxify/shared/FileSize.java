package com.hoaxify.hoaxify.shared;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD}) // define where the annotation can be use
@Constraint(validatedBy = FileSizeValidator.class) // validation annotation
@Retention(RetentionPolicy.RUNTIME)// tell JVM to persist annotation in runtime
public @interface FileSize {
    String message() default "{hoaxify.constraint.FileSize.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

    // we may want to use this file size annotation dynamically
    int max(); // so wherever it's used, we enforce max property to be set
    String unit() default "MB"; // and the unit.. This may be an enumerated value also. and we can set default value for it
}
