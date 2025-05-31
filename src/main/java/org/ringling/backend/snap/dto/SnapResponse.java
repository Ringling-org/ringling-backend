package org.ringling.backend.snap.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ringling.backend.snap.entity.Snap;
import org.ringling.backend.summary.entity.Summary;
import org.ringling.backend.summary.entity.SummaryStatus;

@Getter
@AllArgsConstructor
public class SnapResponse {
    private Integer id;
    private Integer summaryId;
    private String summaryTitle;
    private SummaryStatus summaryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SnapResponse toDto(Snap snap, Summary summary) {
        return new SnapResponse(
            snap.getId(),
            summary.getId(),
            summary.getSummaryTitle(),
            summary.getSummaryStatus(),
            snap.getCreatedAt(),
            snap.getUpdatedAt()
        );
    }
}