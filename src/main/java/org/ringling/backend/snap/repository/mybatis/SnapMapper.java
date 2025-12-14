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
    List<Snap> findSnaps(
        @Param("userId") Integer userId,
        @Param("lastSnapId") Integer lastSnapId,
        @Param("limit") int limit
    );
    SnapCountResponse getSnapCounts(@Param("userId") Integer userId);
    Snap findById(Integer id);
}