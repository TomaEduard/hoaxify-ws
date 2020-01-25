package com.hoaxify.hoaxify.file;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileService {

    AppConfiguration appConfiguration;

    public FileService(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    public String saveProfileImage(String base64Image) throws IOException {
        // convert Base64 in byte[]
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

        // generate name of document
        String imageName = UUID.randomUUID().toString().replace("-", "");
        // cr8 target file + name of document
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);

        // write byte to this file
        FileUtils.writeByteArrayToFile(target, decodedBytes);
        return imageName;
    }
}
