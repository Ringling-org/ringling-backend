package org.ringling.backend.auth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoAccountProfile {

    @JsonProperty("id")
    Long id;

    @JsonProperty("connected_at")
    String connectedAt;
}