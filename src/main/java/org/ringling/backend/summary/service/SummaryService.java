package org.ringling.backend.summary.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.summary.entity.Summary;
import org.ringling.backend.summary.entity.SummaryStatus;
import org.ringling.backend.summary.repository.SummaryRepository;

@Slf4j
@Service
public class SummaryService {

    private final SummaryRepository summaryRepository;

    @Autowired
    public SummaryService(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    public Summary processSummary(String url) {
        Summary existingSummary = summaryRepository.findByUrl(url);
        if (existingSummary != null) {
            return existingSummary;
        }

        Summary newSummary = Summary.builder()
            .url(url)
            .summaryStatus(SummaryStatus.PENDING)
            .build();

        summaryRepository.save(newSummary);

        /**
         * TODO: 비동기 AI 요약 요청 수행
         * 요청 결과로 받은 summaryTitle 값으로 엔티티 필드 갱신 후 DB에 반영
         */

        return newSummary;
    }

}
