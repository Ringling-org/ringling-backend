package org.ringling.backend.snap.repository.mybatis;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Repository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.snap.entity.Snap;
import org.ringling.backend.snap.repository.SnapRepository;

@Slf4j
@Repository
public class MyBatisSnapRepository implements SnapRepository {

    private final SnapMapper snapMapper;

    @Autowired
    public MyBatisSnapRepository(SnapMapper snapMapper) {
        this.snapMapper = snapMapper;
    }

    @Override
    public Snap save(Snap snap) {
        if (snap.getId() != null) {
            return merge(snap);
        }
        snap.prePersist();
        snapMapper.save(snap);

        return snap;
    }

    private Snap merge(Snap snap) {
        snap.preUpdate();
        snapMapper.merge(snap);

        return findById(snap.getId());
    }

    @Override
    public List<Snap> findAll() {
        return snapMapper.findAll();
    }

    @Override
    public Snap findById(Integer id) {
        return snapMapper.findById(id);
    }
}
