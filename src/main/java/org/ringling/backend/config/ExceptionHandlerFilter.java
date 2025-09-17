package org.ringling.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.code.ErrorCode;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.common.exception.CustomException;

@Slf4j
public class ExceptionHandlerFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (CustomException e) {
            sendErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("Unhandled exception in filter", e);
            sendErrorResponse(response, ErrorCode.UNEXPECTED_ERROR);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<?> errorResponse = ApiResponse.error(errorCode);

        try (PrintWriter writer = response.getWriter()) {
            objectMapper.writeValue(writer, errorResponse);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    @Override
    public void destroy() {}
}