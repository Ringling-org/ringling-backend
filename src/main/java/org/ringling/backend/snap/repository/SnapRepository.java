package org.ringling.backend.snap.repository;

import org.ringling.backend.snap.entity.Snap;

public interface SnapRepository {

    Snap save(Snap snap);
    Snap findById(Integer id);
}
