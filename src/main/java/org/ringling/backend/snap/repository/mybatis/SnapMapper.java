package org.ringling.backend.snap.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.ringling.backend.snap.dto.SnapCountResponse;
import org.ringling.backend.snap.entity.Snap;

@Mapper
public interface SnapMapper {

    int save(Snap snap);
    int merge(Snap snap);
    List<Snap> findAll();
    List<Snap> findSnaps(@Param("userId") Integer userId);
    SnapCountResponse getSnapCounts(@Param("userId") Integer userId);
    Snap findById(Integer id);
}