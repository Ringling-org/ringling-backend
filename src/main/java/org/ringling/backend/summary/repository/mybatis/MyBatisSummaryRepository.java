package org.ringling.backend.summary.repository.mybatis;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Repository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.summary.entity.Summary;
import org.ringling.backend.summary.repository.SummaryRepository;

@Slf4j
@Repository
public class MyBatisSummaryRepository implements SummaryRepository {

    private final SummaryMapper summaryMapper;

    @Autowired
    public MyBatisSummaryRepository(SummaryMapper summaryMapper) {
        this.summaryMapper = summaryMapper;
    }

    @Override
    public Summary save(Summary summary) {
        if (summary.getId() != null) {
            return merge(summary);
        }
        summary.prePersist();
        summaryMapper.save(summary);

        return summary;
    }

    private Summary merge(Summary summary) {
        summary.preUpdate();
        summaryMapper.merge(summary);

        return findById(summary.getId());
    }

    @Override
    public Summary findById(Integer id) {
        return summaryMapper.findById(id);
    }

    @Override
    public List<Summary> findAllById(List<Integer> summaryIds) {
        return summaryMapper.findAllById(summaryIds);
    }

    @Override
    public Summary findByUrl(String url) {
        return summaryMapper.findByUrl(url);
    }

}
