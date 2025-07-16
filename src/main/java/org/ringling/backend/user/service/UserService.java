package org.ringling.backend.user.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
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

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByKaKaoId(Long kakaoId) {
        return userRepository.findBySocialIdAndSocialType(kakaoId, SocialType.KAKAO);
    }

}
