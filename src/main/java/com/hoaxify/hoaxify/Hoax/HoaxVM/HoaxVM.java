package com.hoaxify.hoaxify.Hoax.HoaxVM;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.file.FileAttachmentVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import com.hoaxify.hoaxify.userPreference.UserPreference;
import com.hoaxify.hoaxify.userPreference.userPreferenceVM.UserPreferenceVM;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class HoaxVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    private UserPreferenceVM userPreference;

    public HoaxVM(Hoax hoax) {
        this.setId(hoax.getId());
        this.setContent(hoax.getContent());
        this.setDate(hoax.getTimestamp().getTime());
        this.setUser(new UserVM(hoax.getUser()));
        if (hoax.getAttachment() != null) {
            this.setAttachment(new FileAttachmentVM(hoax.getAttachment()));
        }
//        this.setUserPreference(new UserPreferenceVM((UserPreference) hoax.getUserPreference()));
//        this.setUserPreference(new UserPreferenceVM(hoax.getUserPreference()));

    }
}