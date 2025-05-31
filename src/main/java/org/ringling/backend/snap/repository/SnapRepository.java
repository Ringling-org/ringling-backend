package org.ringling.backend.snap.repository;

import java.util.List;
import org.ringling.backend.snap.entity.Snap;

public interface SnapRepository {

    Snap save(Snap snap);
    List<Snap> findAll();
    Snap findById(Integer id);
}
