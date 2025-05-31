package org.ringling.backend.snap.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private String url;
    private SummaryStatus summaryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SnapResponse toDto(Snap snap, Summary summary) {
        return new SnapResponse(
            snap.getId(),
            summary.getId(),
            summary.getSummaryTitle(),
            summary.getUrl(),
            summary.getSummaryStatus(),
            snap.getCreatedAt(),
            snap.getUpdatedAt()
        );
    }

    public static List<SnapResponse> toDtoList(List<Snap> snaps, List<Summary> summaries) {
        Map<Integer, Summary> summaryMap = summaries.stream()
            .collect(Collectors.toMap(Summary::getId, Function.identity()));

        return snaps.stream()
            .map(snap -> {
                Summary related = summaryMap.get(snap.getSummaryId());
                return SnapResponse.toDto(snap, related);
            })
            .collect(Collectors.toList());
    }
}