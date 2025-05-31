package org.ringling.backend.common.utils.http;

import lombok.Getter;

@Getter
public enum ContentType {
    JSON("application/json"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    XML("application/xml"),
    OCTET_STREAM("application/octet-stream");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }
}