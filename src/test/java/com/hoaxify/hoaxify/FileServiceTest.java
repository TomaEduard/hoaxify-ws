package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.file.FileService;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FileServiceTest {

    FileService fileService;

    AppConfiguration appConfiguration;

    @MockBean
    FileAttachmentRepository fileAttachmentRepository;

    @Before
    public void init() {
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration, fileAttachmentRepository);

        new File(appConfiguration.getUploadPath()).mkdirs();
        new File(appConfiguration.getFullProfileImagesPath()).mkdirs();
        new File(appConfiguration.getFullAttachmentsPath()).mkdirs();
    }

    @After
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void deleteType_whenPngFileProvided_returnsImagePng() throws IOException {
        ClassPathResource resourceFile = new ClassPathResource("test-png.png");
        byte[] fileArr = FileUtil.readAsByteArray(resourceFile.getFile());
        String fileType = fileService.detectType(fileArr);
        assertThat(fileType).isEqualToIgnoringCase("image/png");
    }

    @Test
    public void cleanUpStorage_whenOldFileExist_removesFilesFromStorage() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        File storedImage = new File(filePath);
        assertThat(storedImage.exists()).isFalse();
    }

    @Test
    public void cleanUpStorage_whenOldFileExist_removesFilesAttachmentsFromDatabase() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("profile.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        Mockito.verify(fileAttachmentRepository).deleteById(5L);
    }

}
