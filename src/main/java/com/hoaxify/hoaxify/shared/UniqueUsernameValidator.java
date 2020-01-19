package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.io.entity.User;
import com.hoaxify.hoaxify.io.repository.UserRepository;
import com.hoaxify.hoaxify.shared.UniqueUsername;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
	
	@Autowired
    UserRepository userRepository;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		
		User inDB = userRepository.findByUsername(value);
		if(inDB == null) {
			return true;
		}

		return false;
	}

}
