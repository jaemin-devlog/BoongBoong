package org.hanseo.boongboong.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.exception.ErrorResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {

        ErrorCode ec = ErrorCode.UNAUTHORIZED;
        ErrorResponse body = ErrorResponse.of(ec, req.getRequestURI());

        res.setStatus(ec.getStatus().value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
