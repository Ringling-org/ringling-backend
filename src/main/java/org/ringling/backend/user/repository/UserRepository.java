package org.ringling.backend.user.repository;

import org.ringling.backend.user.entity.SocialType;
import org.ringling.backend.user.entity.User;

public interface UserRepository {

    User save(User user);
    User findById(Integer id);
    User findBySocialIdAndSocialType(Long socialId, SocialType socialType);
}
