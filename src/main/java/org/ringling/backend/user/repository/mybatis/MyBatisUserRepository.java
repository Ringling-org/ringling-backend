package org.ringling.backend.user.repository.mybatis;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.user.entity.SocialType;
import org.ringling.backend.user.entity.User;
import org.ringling.backend.user.repository.UserRepository;

@Slf4j
@Repository
public class MyBatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    @Autowired
    public MyBatisUserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public User findBySocialIdAndSocialType(Long socialId, SocialType socialType) {
        return userMapper.findBySocialIdAndSocialType(socialId, socialType);
    }

    @Override
    public User save(User user) {
        if (user.getId() != null) {
            return merge(user);
        }
        user.prePersist();
        userMapper.save(user);

        return user;
    }

    private User merge(User user) {
        user.preUpdate();
        userMapper.merge(user);

        return findById(user.getId());
    }
}
