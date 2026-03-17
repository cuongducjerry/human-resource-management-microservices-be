package com.hrm.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtDecoder jwtDecoder;

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        ServletServerHttpRequest servlet =
                (ServletServerHttpRequest) request;

        String token = servlet.getServletRequest().getParameter("token");

        if (token == null || token.isEmpty()) {
            return null;
        }

        Jwt jwt = jwtDecoder.decode(token);

        String employeeId = jwt.getClaimAsString("employeeId");

        attributes.put("employeeId", employeeId);
        attributes.put("email", jwt.getClaimAsString("email"));

        return () -> employeeId;
    }
}