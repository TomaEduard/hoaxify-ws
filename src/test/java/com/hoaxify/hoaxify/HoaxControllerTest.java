package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.Utils.TestUtil;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class) // junit4
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

    public static final String API_1_0_HOAXES = "/api/1.0/hoaxes";
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HoaxRepository hoaxRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() {
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveUnauthorized() {
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveApiError() {
        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();

        postHoax(hoax, Object.class);
        assertThat(hoaxRepository.count()).isEqualTo(1);
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDatabaseWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getTimestamp()).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxContentNullAndUserIsValidAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentLess10CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = new Hoax();
        hoax.setContent("123456789");

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentIs5000CharactersAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());

        Hoax hoax = new Hoax();
        hoax.setContent(veryLongString);

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postHoax_whenHoaxContentMoreThan5001CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());

        Hoax hoax = new Hoax();
        hoax.setContent(veryLongString);

        ResponseEntity<Object> response = postHoax(hoax, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postHoax_whenHoaxContentLess10CharactersAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = new Hoax();

        ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
        Map<String, String> validationError = response.getBody().getValidationErrors();
        assertThat(validationError.get("content")).isNotNull();
    }

    @Test
    public void postHoax_whenHoaxIsValidAbdUserUsAuthorized_hoaxSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        Hoax inDb = hoaxRepository.findAll().get(0);

        assertThat(inDb.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postHoax_whenHoaxIsValidAbdUserUsAuthorized_hoaxCanBeAccessedFromUserEntity() {
        User user1 = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Hoax hoax = TestUtil.createValidHoax();
        postHoax(hoax, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        User inDbUser = entityManager.find(User.class, user1.getId());
        assertThat(inDbUser.getHoaxes().size()).isEqualTo(1);
    }

    // Utils

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }
    // set Authentication Basic Auth with Username and Password
    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }


}
