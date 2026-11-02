package com.scheduler.distributed_job_scheduler_backend.config;

import com.scheduler.distributed_job_scheduler_backend.filter.RequestIdFilter;
import com.scheduler.distributed_job_scheduler_backend.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {

        FilterRegistrationBean<RequestIdFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestIdFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
