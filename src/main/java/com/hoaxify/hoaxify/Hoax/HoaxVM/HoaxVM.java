package com.hoaxify.hoaxify.Hoax.HoaxVM;

import com.hoaxify.hoaxify.Hoax.Hoax;
import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentVM;
import com.hoaxify.hoaxify.user.userVM.UserVM;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HoaxVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    public HoaxVM(Hoax hoax) {
        this.setId(hoax.getId());
        this.setContent(hoax.getContent());
        this.setDate(hoax.getTimestamp().getTime());
        this.setUser(new UserVM(hoax.getUser()));
        if (hoax.getAttachment() != null) {
            this.setAttachment(new FileAttachmentVM(hoax.getAttachment()));
        }
    }
}
