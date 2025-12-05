package org.ringling.backend.snap.repository;

import java.util.List;
import org.ringling.backend.snap.dto.SnapCountResponse;
import org.ringling.backend.snap.entity.Snap;

public interface SnapRepository {

    Snap save(Snap snap);
    List<Snap> findAll();
    List<Snap> findSnaps(Integer userId);
    SnapCountResponse getSnapCounts(Integer userId);
    Snap findById(Integer id);
}
