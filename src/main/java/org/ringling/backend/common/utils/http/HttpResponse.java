package org.ringling.backend.common.utils.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpResponse<T> {

    int statusCode;
    String responseMessage;
    T responseBody;
}
