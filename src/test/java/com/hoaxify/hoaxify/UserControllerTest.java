package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.Utils.TestPage;
import com.hoaxify.hoaxify.Utils.TestUtil;
import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class) // junit4
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Before
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @After
    public void cleanDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void postUser_whenUserIsValid_receiveOk() {
        User user = TestUtil.createValidUser();
        ResponseEntity<Object> responseEntity = postSignup(user, Object.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase() {
        User user = TestUtil.createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);

        assertThat(userRepository.count()).isEqualTo(1L);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage() {
        User user = TestUtil.createValidUser();
        ResponseEntity<GenericResponse> responseEntity = postSignup(user, GenericResponse.class);

        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    public void postUser_whenUserIsValid_passwordIsHashInDatabase() {
        User user = TestUtil.createValidUser();
        testRestTemplate.postForEntity(API_1_0_USERS, user, Object.class);
        List<User> users = userRepository.findAll();
        User inDB = users.get(0);
        assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());
    }

    // null
    @Test
    public void postUser_whenUserHasNullUsername_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // @Size(min)
    @Test
    public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("aGc$fhA");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //    @Size(max)
    @Test
    public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf256Characters = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf256Characters);
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf256Characters = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Characters);
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf256Characters = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(valueOf256Characters);
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // All Lowercase
    @Test
    public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("abcdefghijk");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //    All Uppercase
    @Test
    public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("ABCDEFGHIJK");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //    All Numbers
    @Test
    public void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("123456789");
        ResponseEntity<Object> response = postSignup(user, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationError() {
        User user = new User();
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);

        assertThat(responseEntity.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    //    Errors
    @Test
    public void postUser_whenUserIsInvalid_receiveApiError() {
        User user = new User();
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        assertThat(responseEntity.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
        User user = new User();
        user.setUsername(null);
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
        User user = new User();
        user.setPassword(null);
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
    }

    @Test
    public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizerError() {
        User user = new User();
        user.setUsername("abc");
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
    }

    @Test
    public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
        User user = new User();
        user.setPassword("alllowercase");
        ResponseEntity<ApiError> responseEntity = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = responseEntity.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();
        ResponseEntity<Object> objectResponseEntity = postSignup(user, Object.class);
        assertThat(objectResponseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsernamet() {
        userRepository.save(TestUtil.createValidUser());

        User user = TestUtil.createValidUser();
        ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDb_receiveOk() {
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDb_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    public void getUsers_whenThereIsAUserInDB_receivePageWithUser() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
    }

    @Test
    public void getUsers_whenThereIsAUserInDB_receiveUserWithoutPassword() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
        });
        Map<String, Object> entity = response.getBody().getContent().get(0);
        assertThat(entity.containsKey("password")).isFalse();
    }

    @Test
    public void getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDatabaseHas20Users_receive3Users() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUtil::createValidUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?page=0&size=3";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getContent().size()).isEqualTo(3);
    }

    @Test
    public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void getUsers_whenPageSizeGreaterThan100_receivePageSizeAs100() {
        String path = API_1_0_USERS + "?page=0&size=500";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getSize()).isEqualTo(100);
    }

    @Test
    public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
        String path = API_1_0_USERS + "?page=0&size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getSize()).isEqualTo(10);
    }

    @Test
    public void getUsers_whenPageSizeIsNegative_receiveFirstPage() {
        String path = API_1_0_USERS + "?page=0&size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getNumber()).isEqualTo(0);
    }

    @Test
    public void getUsers_whenUserLoggedIn_ReceivePageWithoutLoggedInUser() {
        userService.save(TestUtil.createValidUser("user1"));
        userService.save(TestUtil.createValidUser("user2"));
        userService.save(TestUtil.createValidUser("user3"));
        authenticate("user1");
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveOk() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity response = getUser(username, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity<String> response = getUser(username, String.class);
        assertThat(response.getBody().contains("password")).isFalse();
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getUser("unknown-user", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
        ResponseEntity<ApiError> response = getUser("unknown-user", ApiError.class);
        assertThat(response.getBody().getMessage().contains("unknown-user")).isTrue();
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveUnauthorized() {
        ResponseEntity<Object> response = putUser(123, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
        User user = userService.save(TestUtil.createValidUser("user1"));

        authenticate(user.getUsername());
        long anotherUserId = user.getId() + 123;
        ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveApiError() {
        ResponseEntity<ApiError> response = putUser(123, null, ApiError.class);
        assertThat(response.getBody().getUrl()).contains("users/123");
    }

    @Test
    public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveApiError() {
        User user = userService.save(TestUtil.createValidUser("user1"));

        authenticate(user.getUsername());
        long anotherUserId = user.getId() + 123;
        ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);
        assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
    }

    @Test
    public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM userUpdateVM = createValidUserUpdateVm();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(userUpdateVM);
        ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_displayNameUpdated() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM userUpdateVM = createValidUserUpdateVm();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(userUpdateVM);
        // call update controller
        putUser(user.getId(), requestEntity, Object.class);

        User userInDb = userRepository.findByUsername("user1");
        assertThat(userInDb.getDisplayName()).isEqualTo(userUpdateVM.getDisplayName());
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserVMWithUpdatedDisplayName() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM userUpdateVM = createValidUserUpdateVm();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(userUpdateVM);
        // call update controller
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getBody().getDisplayName()).isEqualTo(userUpdateVM.getDisplayName());
    }

    @Test
    public void putUser_whenValidRequestBodyWithSupportedImageFormatAuthorizedUser_receiveUser() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        // image
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        // create valid update user
        UserUpdateVM userUpdateVM = createValidUserUpdateVm();

        // convert image to byte array
        byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
        String imageString = Base64.getEncoder().encodeToString(imageArr);
        // set updated user the new image
        userUpdateVM.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(userUpdateVM);
        // call update controller
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthorizedUser_receiveUserVMWithRandomImageName() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
        assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthorizedUser_imageIsStoredUnderProfileFolder() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        String storedImageName = response.getBody().getImage();

        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;

        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void putUser_withInvalidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withInvalidRequestBodyWithLessThanMinSizeDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("abc");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withInvalidRequestBodyWithLessThanMaxSizeDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();
        String valueOf256Characters = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        updatedUser.setDisplayName(valueOf256Characters);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withValidRequestBodyWithJPGImageFromAuthorizedUser_receiveOk() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void putUser_withValidRequestBodyWithGIFImageFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void putUser_withValidRequestBodyWithTXTImageFromAuthorizedUser_receiveValidationErrorForProfileImage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, ApiError.class);
        Map<String, String> validationError = response.getBody().getValidationErrors();
        assertThat(validationError.get("image")).isEqualTo("Only PNG, JPG and GIF files are allowed");
    }

    @Test
    public void putUser_withValidRequestBodyWithJPGImageForUserWhereImage_removesOldImageFromStorage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        UserUpdateVM updatedUser = createValidUserUpdateVm();
        String imageString = readFileToBase65("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        putUser(user.getId(), requestEntity, UserVM.class);

        String storedImageName = response.getBody().getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
        File storedImage = new File(profilePicturePath);

        assertThat(storedImage.exists()).isFalse();
    }

    // Utils
    private String readFileToBase65(String fileName) throws IOException {
        ClassPathResource imageResources = new ClassPathResource(fileName);
        byte[] imageArr = FileUtil.readAsByteArray(imageResources.getFile());
        return Base64.getEncoder().encodeToString(imageArr);
    }

    private UserUpdateVM createValidUserUpdateVm() {
        UserUpdateVM userUpdateVM = new UserUpdateVM();
        userUpdateVM.setDisplayName("newDisplayName");
        return userUpdateVM;
    }

    // set Authentication Basic Auth with Username and Password
    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }

    public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity getUser(String username, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, responseType);
    }

    public <T> ResponseEntity putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + id;
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
    }


}
