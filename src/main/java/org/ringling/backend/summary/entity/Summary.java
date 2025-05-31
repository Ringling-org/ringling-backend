package org.ringling.backend.summary.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ringling.backend.common.entity.BaseEntity;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonPropertyOrder({
    "id",
    "url",
    "summaryTitle",
    "summaryStatus",
    "createdAt",
    "updatedAt"
})
public class Summary extends BaseEntity {

    private Integer id;
    private String url;
    private String summaryTitle;
    private SummaryStatus summaryStatus;

    public void updateSummaryTitle(String summaryTitle) {
        this.summaryTitle = summaryTitle;
    }

    public void updateSummaryStatus(SummaryStatus status) {
        this.summaryStatus = status;
    }
}
