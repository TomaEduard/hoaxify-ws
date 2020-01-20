package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.Utils.TestUtil;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest        // clean db every time when run a test
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void findByUsername_whenUserExist_returnUser() {
        testEntityManager.persist(TestUtil.createValidUser());
        User inDb = userRepository.findByUsername("test-user");
        assertThat(inDb).isNotNull();
    }

    @Test
    public void findByUserrname_whenUserDoesNotExist_returnNull() {
        User inDb = userRepository.findByUsername("test-user");
        assertThat(inDb).isNull();

    }

}