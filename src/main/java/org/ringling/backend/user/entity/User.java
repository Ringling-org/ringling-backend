package org.ringling.backend.user.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ringling.backend.common.entity.BaseEntity;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonPropertyOrder({
    "id",
    "nickname",
    "socialId",
    "socialType",
    "refreshToken",
    "lastLoginAt",
    "createdAt",
    "updatedAt"
})
public class User extends BaseEntity {

    private Integer id;
    private String nickname;
    private Long socialId;
    private SocialType socialType;
    private String refreshToken;
    private LocalDateTime lastLoginAt;

    public User issueRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
