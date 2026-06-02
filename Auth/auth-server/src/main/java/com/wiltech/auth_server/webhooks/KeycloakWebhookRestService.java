package com.wiltech.auth_server.webhooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Receives admin event webhooks from the Keycloak webhook plugin (vymalo/keycloak-webhook).
 * Keycloak POSTs a JSON payload to POST /webhooks/keycloak whenever a user (or other resource)
 * is created, updated, or deleted — regardless of whether the change came from the Admin UI
 * or this application.
 * Security: requests must include the header  X-Webhook-Secret: <value of WEBHOOK_SECRET in .env>
 */
@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class KeycloakWebhookRestService {

    private final KeycloakEventService keycloakEventService;

    @Value("${webhook.secret}")
    private String webhookSecret;

    @PostMapping("/keycloak")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String incomingSecret,
            @RequestBody KeycloakEventDto event) {

        if (!isValidSecret(incomingSecret)) {
            log.warn("Rejected webhook request — invalid or missing X-Webhook-Secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        keycloakEventService.handle(event);
        return ResponseEntity.noContent().build();
    }

    /**
     * Constant-time comparison to prevent timing-based secret enumeration.
     */
    private boolean isValidSecret(String incoming) {
        if (incoming == null || webhookSecret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                incoming.getBytes(StandardCharsets.UTF_8),
                webhookSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
