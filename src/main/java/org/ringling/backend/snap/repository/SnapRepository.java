package org.ringling.backend.snap.repository;

import java.util.List;
import org.ringling.backend.snap.dto.SnapCountResponse;
import org.ringling.backend.snap.entity.Snap;

public interface SnapRepository {

    Snap save(Snap snap);
    List<Snap> findSnaps(Integer userId, Integer lastSnapId, int limit);
    SnapCountResponse getSnapCounts(Integer userId);
    Snap findById(Integer id);
}
