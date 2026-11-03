package com.app.playerservicejava.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized utility for password-protecting the APIs via the Authorization request header.
 */
public final class AuthGuard {

    // Hardcoded placeholder password; to be replaced by a real secret store later
    private static final String DUMMY_PASSWORD = "dummy-password";

    private AuthGuard() {
    }

    /**
     * Checks the Authorization header value against the expected password.
     * Uses a constant-time comparison to avoid leaking information through timing differences.
     */
    public static boolean isAuthorized(String authorizationHeader) {
        // a missing header can never be authorized
        if (authorizationHeader == null) {
            return false;
        }
        return MessageDigest.isEqual(
                DUMMY_PASSWORD.getBytes(StandardCharsets.UTF_8),
                authorizationHeader.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Guard clause helper: returns an empty Optional when the request is authorized,
     * or a 401 response when the Authorization header is missing or incorrect.
     */
    public static Optional<ResponseEntity<Object>> rejectIfUnauthorized(String authorizationHeader) {
        if (isAuthorized(authorizationHeader)) {
            return Optional.empty();
        }
        return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized")));
    }
}
