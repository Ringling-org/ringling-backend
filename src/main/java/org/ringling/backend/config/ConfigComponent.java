package org.ringling.backend.config;

import froggy.winterframework.boot.web.servlet.FilterRegistrationBean;
import froggy.winterframework.context.annotation.Bean;
import froggy.winterframework.context.annotation.Configuration;

@Configuration
public class ConfigComponent {

    @Bean
    public FilterRegistrationBean<ExceptionHandlerFilter> exceptionHandlerFilter() {
        ExceptionHandlerFilter filter = new ExceptionHandlerFilter();
        FilterRegistrationBean<ExceptionHandlerFilter> registration = new FilterRegistrationBean<>(
            filter);

        registration.setOrder(2);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
