package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.Hoax.HoaxRepository;
import com.hoaxify.hoaxify.Hoax.HoaxService;
import com.hoaxify.hoaxify.Hoax.HoaxVM.HoaxVM;
import com.hoaxify.hoaxify.Utils.TestPage;
import com.hoaxify.hoaxify.Utils.TestUtil;
import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @Autowired
    HoaxService hoaxService;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileService fileService;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @After
    public void cleanupAfter() {
        fileAttachmentRepository.deleteAll();
        hoaxRepository.deleteAll();
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

    @Test
    public void getHoaxes_whenThereAreNoHoaxes_receiveOk() {
        ResponseEntity<Object> response = getHoaxesOfUser(new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxes_whenThereAreNoHoaxes_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getHoaxesOfUser(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxes_whenThereAreHoaxes_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<Object>> response = getHoaxesOfUser(new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxes_whenThereAreHoaxes_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getHoaxesOfUser(new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        HoaxVM storedHoax = response.getBody().getContent().get(0);
        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveHoaxVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        Hoax hoax = TestUtil.createValidHoax();

        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_fileAttachmentHoaxRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtil.createValidHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        FileAttachment inDb = fileAttachmentRepository.findAll().get(0);
        assertThat(inDb.getHoax().getId()).isEqualTo(response.getBody().getId());
    }

    @Test
    public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_hoaxFileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtil.createValidHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        Hoax inDb = hoaxRepository.findById(response.getBody().getId()).get();
        assertThat(inDb.getAttachment().getId()).isEqualTo(savedFile.getId());
    }

    @Test
    public void postHoax_whenHoaxHasFileAttachmentAndUserIsAuthorized_receiveHoaxVMWithAttachment() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Hoax hoax = TestUtil.createValidHoax();
        hoax.setAttachment(savedFile);
        ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);

        assertThat(response.getBody().getAttachment().getName()).isEqualTo(savedFile.getName());
    }

    // Util
    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        return new MockMultipartFile("profile.png", fileAsByte);
    }

    @Test
    public void getHoaxesOfUser_whenUserExists_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHoaxesOfUser_whenUserExists_receiveNotFound() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getHoaxesOfUser("unknown-user", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getHoaxesOfUser_whenUserExists_PageWithZeroHoaxes() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<TestPage<Object>> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getHoaxesOfUser_whenUserExistWithHoax_receivePageWithHoaxVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        HoaxVM storedHoax = response.getBody().getContent().get(0);
        assertThat(storedHoax.getUser().getUsername()).isEqualTo("user1");
//        assertThat(response.getBody().getContent().get(0).getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    public void getHoaxesOfUser_whenUserExistWithMultipleHoax_receivePageWithMatchingHoaxesCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getHoaxesOfUser("user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getHoaxesOfUser_whenMultipleUserExistWithMultipleHoax_receivePageWithMatchingHoaxesCount() {
        User userWithThreeHoaxes = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            hoaxService.save(userWithThreeHoaxes, TestUtil.createValidHoax());
        });

        User userWithFiveHoaxes = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            hoaxService.save(userWithFiveHoaxes, TestUtil.createValidHoax());
        });

        ResponseEntity<TestPage<HoaxVM>> response = getHoaxesOfUser(userWithFiveHoaxes.getUsername(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
    }

    @Test
    public void getOldHoaxes_whenThereAreNoHoaxes_receiveOK() {
        ResponseEntity<Object> response = getOldHoaxes(5, new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<Object>> response = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxes_whenThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getOldHoaxes(fourth.getId(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithItemsProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(fourth.getId(), "user1" ,new ParameterizedTypeReference<TestPage<Object>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithHoaxVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getOldHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getContent().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserDoesNotExistThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> response = getOldHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        userService.save(TestUtil.createValidUser("user2"));

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<TestPage<HoaxVM>> response = getOldHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<Object>> response = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<HoaxVM>> response = getNewHoaxes(fourth.getId(), new ParameterizedTypeReference<List<HoaxVM>>() {});
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<Object>> response = getNewHoaxesOfUser(fourth.getId(), "user1" ,new ParameterizedTypeReference<List<Object>>() {});
        assertThat(response.getBody().size()).isEqualTo(1);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserExistAndThereAreHoaxes_receiveListWithHoaxVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        // --- return this hoaxes because is sorter based on the id
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        // ---

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<HoaxVM>> response = getNewHoaxesOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<List<HoaxVM>>() {});
        assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserDoesNotExistThereAreNoHoaxes_receiveNotFound() {
        ResponseEntity<Object> response = getNewHoaxesOfUser(5, "user1", new ParameterizedTypeReference<Object>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getNewHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receiveListWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        userService.save(TestUtil.createValidUser("user2"));

        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<List<HoaxVM>> response = getNewHoaxesOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<List<HoaxVM>>() {});
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getNewHoaxesCount_whenThereAreHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<Map<String, Long>> response = getNewHoaxCount(fourth.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    @Test
    public void getNewHoaxesCountOfUser_whenThereAreHoaxes_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());
        Hoax fourth = hoaxService.save(user, TestUtil.createValidHoax());
        hoaxService.save(user, TestUtil.createValidHoax());

        ResponseEntity<Map<String, Long>> response = getNewHoaxCountOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<Map<String, Long>>() {});
        assertThat(response.getBody().get("count")).isEqualTo(1);
    }

    // Utils

    public <T> ResponseEntity<T> getNewHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxCount(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxCountOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId +  "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldHoaxesOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getHoaxesOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = "/api/1.0/users/" + username + "/hoaxes";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getHoaxesOfUser(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
    }

    private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
    }

    // set Authentication Basic Auth with Username and Password
    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
