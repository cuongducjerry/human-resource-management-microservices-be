package com.hrm.notification.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public class SecurityUtil {

    // ===============================
    // GET JWT
    // ===============================
    private static Jwt getCurrentJwt() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return null;

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }

        return null;
    }

    // ===============================
    // GET KEYCLOAK USER ID (sub)
    // ===============================
    public static String getCurrentUserId() {

        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;

        return jwt.getSubject(); // keycloakUserId
    }

    // ===============================
    // GET EMAIL
    // ===============================
    public static String getCurrentUserEmail() {

        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;

        return jwt.getClaimAsString("email");
    }

    // ===============================
    // GET ROLES FROM REALM_ACCESS
    // ===============================
    public static List<String> getCurrentUserRoles() {

        Jwt jwt = getCurrentJwt();
        if (jwt == null) return List.of();

        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        if (realmAccess == null) return List.of();

        return (List<String>) realmAccess.get("roles");
    }

    // ===============================
    // CHECK ROLE
    // ===============================
    public static boolean hasRole(String role) {

        return getCurrentUserRoles().contains(role);
    }

    // ===============================
    // CHECK PERMISSION (authority)
    // ===============================
    public static boolean hasAuthority(String authority) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return false;

        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    // ===============================
    // GET EMPLOYEE ID (custom claim)
    // ===============================
    public static String getCurrentEmployeeId() {

        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;

        return jwt.getClaimAsString("employeeId");
    }

}
