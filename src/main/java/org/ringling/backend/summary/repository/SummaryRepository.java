package org.ringling.backend.summary.repository;

import org.ringling.backend.summary.entity.Summary;

public interface SummaryRepository {

    Summary save(Summary summary);
    Summary findById(Integer id);
    Summary findByUrl(String url);
}
