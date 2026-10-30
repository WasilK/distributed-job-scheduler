
package com.scheduler.distributed_job_scheduler_backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
@Slf4j
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Generate request ID
        String reqId = UUID.randomUUID().toString();

        try {
            // Store request ID in MDC
            MDC.put("requestId", reqId);

            log.info("Testing request ID logging");

            // Add request ID to response header
            response.setHeader("X-Request-ID", reqId);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Clear MDC to prevent thread pool leakage
            MDC.remove("requestId");
        }
    }
}
