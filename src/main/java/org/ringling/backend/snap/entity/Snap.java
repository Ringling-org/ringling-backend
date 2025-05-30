package org.ringling.backend.snap.entity;

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
    "summaryId",
    "createdAt",
    "updatedAt"
})
public class Snap extends BaseEntity {

    private Integer id;
    private Integer summaryId;
}
