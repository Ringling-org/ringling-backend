package org.ringling.backend.common.dto;

import lombok.Getter;

@Getter
public abstract class ErrorDetail {

    private final String type;
    private final String code;
    private final String message;

    protected ErrorDetail(String type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }
}
