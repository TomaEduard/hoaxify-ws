package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.Utils.TestUtil;
import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class) // junit4
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

    private static final String API_1_0_HOAXES_UPLOAD = "/api/1.0/hoaxes/upload";

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Before
    public void init() throws IOException {
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        // clear authentication from client
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void uploadFile_withImageFromUnauthorizedUser_receiveUnauthorized() {
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithDate() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(response.getBody().getDate()).isNotNull();
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithRandomName() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(response.getBody().getName()).isNotNull();
        assertThat(response.getBody().getName()).isNotEqualTo("profile.png");
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_imageSaveToFolder(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + response.getBody().getName();
        File storedImage = new File(imagePath);
        assertThat(storedImage.exists()).isTrue();
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentSaveToDatabase(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);
        assertThat(fileAttachmentRepository.count()).isEqualTo(1);
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentStoredWithFileType(){
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);
        FileAttachment storedFile = fileAttachmentRepository.findAll().get(0);
        assertThat(storedFile.getFileType()).isEqualTo("image/png");
    }

    // Utils

    private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_HOAXES_UPLOAD, HttpMethod.POST, requestEntity, responseType);
    }

    // set Authentication Basic Auth with Username and Password
    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
    }
}
