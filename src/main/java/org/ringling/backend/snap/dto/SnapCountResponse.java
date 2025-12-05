package org.ringling.backend.snap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnapCountResponse {
    private int allCount;
    private int myCount;
}
