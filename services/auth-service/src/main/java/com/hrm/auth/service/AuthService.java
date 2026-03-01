package com.hrm.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.auth.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.auth.dto.response.ResLoginDTO;
import com.hrm.auth.util.error.InvalidLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    @Value("${keycloak.logout-url}")
    private String logoutUrl;

    // LOGIN CLIENT
    @Value("${keycloak.login-client-id}")
    private String loginClientId;

    @Value("${keycloak.login-client-secret}")
    private String loginClientSecret;

    // ADMIN CLIENT
    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    @Value("${keycloak.admin-base-url}")
    private String adminBaseUrl;

    /* =======================================================
                            LOGIN
       ======================================================= */

    public ResLoginDTO login(String username, String password) {
        Map<String, Object> response =
                callTokenEndpoint(username, password, "password", false);
        return buildLoginResponse(response);
    }

    public ResLoginDTO refresh(String refreshToken) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", loginClientId);
            body.add("client_secret", loginClientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(tokenUrl, request, Map.class);

            return buildLoginResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Refresh failed: " + e.getResponseBodyAsString());
        }
    }

    public void logout(String refreshToken) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(loginClientId, loginClientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", loginClientId);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(logoutUrl, request, String.class);

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Logout failed");
        }
    }

    /* =======================================================
                        CREATE USER (ADMIN)
       ======================================================= */

    private String getAdminAccessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminClientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    public String createUser(ReqCreateKeycloakUserDTO req) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> user = new HashMap<>();
        user.put("username", req.getUsername());
        user.put("email", req.getEmail());
        user.put("enabled", true);
        user.put("firstName",
                req.getFirstName() != null ? req.getFirstName() : "Default");
        user.put("lastName",
                req.getLastName() != null ? req.getLastName() : "User");


        user.put("requiredActions", Collections.emptyList());

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(user, headers);

        ResponseEntity<Void> response =
                restTemplate.postForEntity(adminBaseUrl + "/users", request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create user. Status: "
                    + response.getStatusCode());
        }

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("Keycloak did not return Location header");
        }

        String userId = location.toString()
                .substring(location.toString().lastIndexOf("/") + 1);

        // Set password
        setUserPassword(userId, req.getPassword(), adminToken);

        // Assign roles
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            assignRealmRoles(userId, req.getRoles(), adminToken);
        }

        return userId;
    }

    private void setUserPassword(String userId, String password, String adminToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> pass = new HashMap<>();
        pass.put("type", "password");
        pass.put("value", password);
        pass.put("temporary", false);

        String resetUrl = adminBaseUrl + "/users/" + userId + "/reset-password";

        ResponseEntity<Void> response = restTemplate.exchange(
                resetUrl,
                HttpMethod.PUT,
                new HttpEntity<>(pass, headers),
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to set password");
        }
    }

    public void disableUser(String userId) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("enabled", false);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(updateBody, headers);

        try {
            restTemplate.exchange(
                    adminBaseUrl + "/users/" + userId,
                    HttpMethod.PUT,
                    request,
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to disable user: " + e.getResponseBodyAsString());
        }
    }

    public void updateUserProfile(String userId,
                                  String firstName,
                                  String lastName) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        String url = adminBaseUrl + "/users/" + userId;

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update Keycloak profile");
        }
    }

    public void updateUserRoles(String userId, List<String> newRoles) {

        String adminToken = getAdminAccessToken();

        // Get all current roles
        List<String> currentRoles = getUserRoles(userId);

        // Only remove BUSINESS roles (prefix ROLE_)
        List<String> businessRoles = currentRoles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .toList();

        if (!businessRoles.isEmpty()) {
            removeRealmRoles(userId, businessRoles, adminToken);
        }

        // Assign new roles (business roles only)
        if (newRoles != null && !newRoles.isEmpty()) {

            List<String> filteredNewRoles = newRoles.stream()
                    .filter(role -> role.startsWith("ROLE_"))
                    .toList();

            if (!filteredNewRoles.isEmpty()) {
                assignRealmRoles(userId, filteredNewRoles, adminToken);
            }
        }
    }

    public List<String> getUserRoles(String userId) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = adminBaseUrl +
                "/users/" + userId + "/role-mappings/realm";

        ResponseEntity<List> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        List.class
                );

        List<Map<String, Object>> roles = response.getBody();

        if (roles == null) return List.of();

        return roles.stream()
                .map(r -> (String) r.get("name"))
                .toList();
    }

    public void deleteUser(String userId) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    adminBaseUrl + "/users/" + userId,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to delete user: " + e.getResponseBodyAsString());
        }
    }

    public void enableUser(String userId) {
        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("enabled", true);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(updateBody, headers);

        restTemplate.exchange(
                adminBaseUrl + "/users/" + userId,
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void changePassword(String userId,
                               String currentPassword,
                               String newPassword) {

        // 1. Validate current password by trying to log in
        try {
            callTokenEndpoint(
                    extractUsernameFromUserId(userId),
                    currentPassword,
                    "password",
                    false
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect"
            );
        }

        // 2. If correct -> reset password using admin token
        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> pass = new HashMap<>();
        pass.put("type", "password");
        pass.put("value", newPassword);
        pass.put("temporary", false);

        restTemplate.exchange(
                adminBaseUrl + "/users/" + userId + "/reset-password",
                HttpMethod.PUT,
                new HttpEntity<>(pass, headers),
                Void.class
        );
    }

    private void removeRealmRoles(String userId,
                                  List<String> roleNames,
                                  String adminToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> rolesToRemove = new ArrayList<>();

        for (String roleName : roleNames) {

            String roleUrl = adminBaseUrl + "/roles/" + roleName;

            ResponseEntity<Map> roleResponse =
                    restTemplate.exchange(
                            roleUrl,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            Map.class);

            Map<String, Object> roleBody = roleResponse.getBody();

            Map<String, Object> role = new HashMap<>();
            role.put("id", roleBody.get("id"));
            role.put("name", roleBody.get("name"));

            rolesToRemove.add(role);
        }

        String removeUrl =
                adminBaseUrl + "/users/" + userId + "/role-mappings/realm";

        restTemplate.exchange(
                removeUrl,
                HttpMethod.DELETE,
                new HttpEntity<>(rolesToRemove, headers),
                Void.class
        );
    }

    private void assignRealmRoles(String userId,
                                  List<String> roleNames,
                                  String adminToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> rolesToAssign = new ArrayList<>();

        for (String roleName : roleNames) {

            String roleUrl = adminBaseUrl + "/roles/" + roleName;

            ResponseEntity<Map> roleResponse =
                    restTemplate.exchange(
                            roleUrl,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            Map.class);

            Map<String, Object> roleBody = roleResponse.getBody();

            Map<String, Object> role = new HashMap<>();
            role.put("id", roleBody.get("id"));
            role.put("name", roleBody.get("name"));

            rolesToAssign.add(role);
        }

        String assignUrl =
                adminBaseUrl + "/users/" + userId + "/role-mappings/realm";

        restTemplate.postForEntity(
                assignUrl,
                new HttpEntity<>(rolesToAssign, headers),
                Void.class);
    }

    /* =======================================================
                        INTERNAL
       ======================================================= */

    private Map<String, Object> callTokenEndpoint(
            String username,
            String password,
            String grantType,
            boolean isAdmin
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", grantType);

            if (isAdmin) {
                body.add("client_id", adminClientId);
                body.add("client_secret", adminClientSecret);
            } else {
                body.add("client_id", loginClientId);
                body.add("client_secret", loginClientSecret);
            }

            if ("password".equals(grantType)) {
                body.add("username", username);
                body.add("password", password);
            }

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            tokenUrl,
                            new HttpEntity<>(body, headers),
                            Map.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new InvalidLoginException("Invalid username or password");
        }
    }

    private ResLoginDTO buildLoginResponse(Map<String, Object> tokenResponse) {

        String accessToken = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Integer expiresIn = (Integer) tokenResponse.get("expires_in");

        ResLoginDTO.UserAccount user = extractUser(accessToken);

        return new ResLoginDTO(
                accessToken,
                refreshToken,
                expiresIn.longValue(),
                user
        );
    }

    private ResLoginDTO.UserAccount extractUser(String accessToken) {
        try {
            String[] chunks = accessToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

            JsonNode jsonNode = objectMapper.readTree(payload);

            String id = jsonNode.get("sub").asText();
            String username = jsonNode.get("preferred_username").asText();
            String email = jsonNode.get("email").asText();

            List<String> roles = new ArrayList<>();
            JsonNode realmRoles =
                    jsonNode.get("realm_access").get("roles");

            for (JsonNode role : realmRoles) {
                roles.add(role.asText());
            }

            return new ResLoginDTO.UserAccount(id, username, email, roles);

        } catch (Exception e) {
            throw new RuntimeException("Cannot parse access token");
        }
    }

    private String extractUsernameFromUserId(String userId) {

        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        adminBaseUrl + "/users/" + userId,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

        return (String) response.getBody().get("username");
    }
}