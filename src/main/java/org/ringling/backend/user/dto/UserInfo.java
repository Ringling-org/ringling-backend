package org.ringling.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ringling.backend.user.entity.User;

@Getter
@AllArgsConstructor
public class UserInfo {

    private Integer id;
    private String nickname;

    public static UserInfo from(User user) {
        return new UserInfo(user.getId(), user.getNickname());
    }
}
