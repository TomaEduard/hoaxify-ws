package com.hoaxify.hoaxify.userPreference.userPreferenceVM;

import com.hoaxify.hoaxify.userPreference.UserPreference;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPreferenceVM {

    private long id;

    private boolean favorite;

    private boolean like;

    private boolean bookmark;

    public UserPreferenceVM(UserPreference userPreference) {
        this.setId(userPreference.getId());
        this.setFavorite(userPreference.isFavorite());
        this.setLike(userPreference.isLike());
        this.setBookmark(userPreference.isBookmark());
    }
}
