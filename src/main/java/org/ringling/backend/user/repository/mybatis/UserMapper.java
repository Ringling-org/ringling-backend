package org.ringling.backend.user.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ringling.backend.user.entity.SocialType;
import org.ringling.backend.user.entity.User;

@Mapper
public interface UserMapper {

    int save(User user);
    int merge(User user);
    User findById(Integer id);
    User findBySocialIdAndSocialType(@Param("socialId") Long socialId, @Param("socialType") SocialType socialType);
}
