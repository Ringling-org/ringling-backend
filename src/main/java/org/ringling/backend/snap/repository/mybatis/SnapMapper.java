package org.ringling.backend.snap.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import org.ringling.backend.snap.entity.Snap;

@Mapper
public interface SnapMapper {

    int save(Snap snap);
    int merge(Snap snap);
    Snap findById(Integer id);
}