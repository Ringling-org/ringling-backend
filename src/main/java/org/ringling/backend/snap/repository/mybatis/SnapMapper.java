package org.ringling.backend.snap.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import java.util.List;
import org.ringling.backend.snap.entity.Snap;

@Mapper
public interface SnapMapper {

    int save(Snap snap);
    int merge(Snap snap);
    List<Snap> findAll();
    Snap findById(Integer id);
}