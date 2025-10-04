package org.ringling.backend.user.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.user.dto.UserInfo;
import org.ringling.backend.user.entity.SocialType;
import org.ringling.backend.user.entity.User;
import org.ringling.backend.user.repository.UserRepository;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(User user, String refreshToken) {
        user.issueRefreshToken(refreshToken);
        user.recordLastLogin();
        return userRepository.save(user);
    }

    public User signUpFromKakao(Long kakaoId, String nickname) {
        if (findByKaKaoId(kakaoId) != null) {
            throw new AuthException(ErrorCode.EXISTS_USER);
        }

        User user = User.builder()
            .socialId(kakaoId)
            .socialType(SocialType.KAKAO)
            .nickname(nickname)
            .build();

        user.prePersist();
        return userRepository.save(user);
    }

    public User findByKaKaoId(Long kakaoId) {
        return userRepository.findBySocialIdAndSocialType(kakaoId, SocialType.KAKAO);
    }

    public User clearRefreshToken(Integer userId) {
        User user = userRepository.findById(userId);

        user.clearRefreshToken();
        user.preUpdate();
        return userRepository.save(user);
    }

    public User getUserByUserId(Integer userId) {
        return userRepository.findById(userId);
    }

    public UserInfo getUserInfo(Integer userId) {
        User selectUser = userRepository.findById(userId);

        return UserInfo.from(selectUser);
    }

    public User updateFcmToken(User user, String fcmToken) {
        user.updateFcmToken(fcmToken);
        return userRepository.save(user);
    }
}
