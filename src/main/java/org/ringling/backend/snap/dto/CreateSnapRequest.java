package org.ringling.backend.snap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ringling.backend.config.ValidSnapUrl;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class CreateSnapRequest {

    @NotBlank(message = "URL을 입력해 주세요")
    @ValidSnapUrl
    private String url;
}
