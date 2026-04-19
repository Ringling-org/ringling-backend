package org.ringling.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorDetail extends ErrorDetail {

    private final String scope;
    private final String field;
    private final Object rejectedValue;

    public ValidationErrorDetail(
        String scope,
        String code,
        String message,
        String field,
        Object rejectedValue
    ) {
        super("validation", code, message);
        this.scope = scope;
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}
