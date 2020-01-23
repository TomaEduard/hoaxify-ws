package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

	UserRepository userRepository;

	PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User save(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	public Page<User> getUser(User loggedInUser, Pageable pageable) {
		if(loggedInUser != null) {
			return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
		}
		return userRepository.findAll(pageable);
	}

	public User getUserByUsername(String username) {
		User inDB = userRepository.findByUsername(username);
		if (inDB == null) {
			throw new NotFoundException(username + " not found");
		}
		return inDB;
	}

	public User updateDisplayName(long id, UserUpdateVM userUpdateVM) {
		User inDb = userRepository.getOne(id);
		inDb.setDisplayName(userUpdateVM.getDisplayName());

		String savedImageName = inDb.getUsername() + UUID.randomUUID().toString().replaceAll("-", "");
		inDb.setImage(savedImageName);

		return userRepository.save(inDb);
	}
}