package com.hoaxify.hoaxify;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepository {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    
}
