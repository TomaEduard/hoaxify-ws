package com.hoaxify.hoaxify.file;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.shared.FileSize;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class FileAttachment {

    @Id
    @GeneratedValue
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private String name;

    @FileSize(max=1)
    private String fileType;

    @OneToOne
    private Hoax hoax;


}
