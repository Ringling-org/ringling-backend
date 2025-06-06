package org.ringling.backend.summary.repository.mybatis;

import froggy.mybatis.winter.annotation.Mapper;
import java.util.List;
import org.ringling.backend.summary.entity.Summary;

@Mapper
public interface SummaryMapper {

    int save(Summary summary);
    int merge(Summary summary);
    Summary findById(Integer id);
    List<Summary> findAllById(List<Integer> summaryIds);
    Summary findByUrl(String url);

}