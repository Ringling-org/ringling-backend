package org.ringling.backend.summary.repository;

import java.util.List;
import org.ringling.backend.summary.entity.Summary;

public interface SummaryRepository {

    Summary save(Summary summary);
    Summary findById(Integer id);
    List<Summary> findAllById(List<Integer> summaryIds);
    Summary findByUrl(String url);
}
