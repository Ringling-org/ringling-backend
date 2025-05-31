package org.ringling.backend.snap.service;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.snap.dto.SnapResponse;
import org.ringling.backend.snap.entity.Snap;
import org.ringling.backend.snap.repository.SnapRepository;
import org.ringling.backend.summary.entity.Summary;
import org.ringling.backend.summary.service.SummaryService;

@Slf4j
@Service
public class SnapService {

    private final SnapRepository snapRepository;
    private final SummaryService summaryService;

    @Autowired
    public SnapService(SnapRepository snapRepository, SummaryService summaryService) {
        this.snapRepository = snapRepository;
        this.summaryService = summaryService;
    }

    public SnapResponse processSnap(String requestUrl) {
        Summary summary = summaryService.processSummary(requestUrl);
        Snap snap = Snap.builder()
            .summaryId(summary.getId())
            .build();

        snapRepository.save(snap);

        return SnapResponse.toDto(snap, summary);
    }

}
