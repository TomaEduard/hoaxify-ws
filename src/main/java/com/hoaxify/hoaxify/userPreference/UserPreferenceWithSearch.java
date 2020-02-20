package com.hoaxify.hoaxify.userPreference;

import lombok.Data;

@Data
public class UserPreferenceWithSearch {

    private boolean favorite;

    private boolean like;

    private boolean bookmark;

    private String partialName;
}
