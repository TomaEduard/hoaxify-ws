package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FileSizeValidator implements ConstraintValidator<FileSize, String> {

    @Autowired
    FileService fileService;

    long maxByteSize;

    // In this one, we also need to override the initialize method of ConstraintValidator.
    // because we want to get the parameter from our FileSize annotation
    @Override
    public void initialize(FileSize constraintAnnotation) {
        int max = constraintAnnotation.max();
        String unit = constraintAnnotation.unit();
        // then do the math based on this max value and unit and calculate the maxByteSize
        this.maxByteSize = max * 1048576 ;
    }

    @Override
    public boolean isValid(String base64EncodedFile, ConstraintValidatorContext context) {
        if (base64EncodedFile == null || base64EncodedFile.isEmpty()) {
            // less than file size limit
            return true;
        }
        // if we have file calculate the byte size of this string
        long incomingFileByteSize = (3 * (base64EncodedFile.length() / 4));

        if(incomingFileByteSize < maxByteSize ) {
            return true;
        }
        return false;
    }
}
