package com.hoaxify.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    // this class check by Spring for Web Configuration like: static resources path, setting interceptors

    @Autowired
    AppConfiguration appConfiguration;

    @Bean
    CommandLineRunner creteUploadFolder() {
        return (args) -> {

            createNonExistingFolder(appConfiguration.getUploadPath());
            createNonExistingFolder(appConfiguration.getFullProfileImagesPath());
            createNonExistingFolder(appConfiguration.getFullAttachmentsPath());
        };
    }

    private void createNonExistingFolder(String path) {
        File uploadFolder = new File(path);
        boolean folderExist = uploadFolder.exists() && uploadFolder.isDirectory();
        if(!folderExist) {
            uploadFolder.mkdir();
        }
    }
}
