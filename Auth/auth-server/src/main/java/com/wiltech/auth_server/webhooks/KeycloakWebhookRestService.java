package com.wiltech.auth_server.webhooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Receives webhook events from the Keycloak webhook plugin (vymalo/keycloak-webhook).
 *
 * The plugin POSTs to {WEBHOOK_HTTP_BASE_PATH}/ so both /webhooks/keycloak and
 * /webhooks/keycloak/ are mapped here to handle trailing-slash variants.
 *
 * Security: the plugin sends HTTP Basic Auth credentials configured via
 * WEBHOOK_HTTP_AUTH_USERNAME / WEBHOOK_HTTP_AUTH_PASSWORD in docker-compose.yml.
 * These must match webhook.username / webhook.password in application.yml.
 */
@Slf4j
@RestController
@RequestMapping("/webhooks/keycloak")
@RequiredArgsConstructor
public class KeycloakWebhookRestService {

    private final KeycloakEventService keycloakEventService;

    @Value("${webhook.username}")
    private String webhookUsername;

    @Value("${webhook.password}")
    private String webhookPassword;

    @PostMapping({"", "/"})
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody KeycloakEventDto event) {

        if (!isValidBasicAuth(authHeader)) {
            log.warn("Rejected webhook request — invalid or missing Basic Auth credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        keycloakEventService.handle(event);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates HTTP Basic Auth using constant-time comparison to prevent timing attacks.
     */
    private boolean isValidBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
            String expected = webhookUsername + ":" + webhookPassword;
            return MessageDigest.isEqual(
                    decoded.getBytes(StandardCharsets.UTF_8),
                    expected.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
