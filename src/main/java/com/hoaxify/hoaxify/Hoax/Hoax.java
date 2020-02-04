package com.hoaxify.hoaxify.Hoax;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.user.User;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;


    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
//    @JsonIgnore
    private User user;

    @OneToOne(mappedBy = "hoax", orphanRemoval = true)
    private FileAttachment attachment;
}
