package com.hoaxify.hoaxify.userPreference;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreferenceResponse {

    private long id;

    private boolean favorite;

    private boolean like;

    private boolean bookmark;

}