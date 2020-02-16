package com.hoaxify.hoaxify.verificationToken;

import com.hoaxify.hoaxify.user.User;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class VerificationToken {

    @Id
    @GeneratedValue
    private long id;

//    @Temporal(TemporalType.TIMESTAMP)
//    private Date date;

    private String changeEmailToken = "xxx";

    private String changePasswordToken = "xxx";

    @OneToOne
    private User user;
}
