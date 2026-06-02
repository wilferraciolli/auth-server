package com.wiltech.auth_server.webhooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Dispatches incoming Keycloak webhook events to typed handler methods.
 *
 * The vymalo/keycloak-webhook plugin sends a compound `type` field such as
 * "ADMIN_USER_CREATE", "ADMIN_USER_UPDATE", "ADMIN_USER_DELETE", or user events
 * like "REGISTER", "LOGIN". Dispatching uses substring matching so it works
 * regardless of the exact type format the plugin uses.
 *
 * For CREATE and UPDATE events the `representation` field is a JSON string
 * containing the full user object — deserialized into {@link WebhookUserRepresentation}.
 *
 * Override the on* methods to add real business logic.
 */
@Slf4j
@Service
public class KeycloakEventService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handle(KeycloakEventDto event) {
        String type = event.getType();

        if (type == null) {
            log.warn("Received Keycloak webhook event with null type: {}", event);
            return;
        }

        String typeUpper = type.toUpperCase();
        log.debug("Received Keycloak event type={} realm={}", type, event.getRealmId());

        if (typeUpper.contains("USER")) {
            String userId = resolveUserId(event);
            WebhookUserRepresentation user = parseRepresentation(event.getRepresentation());
            if (typeUpper.contains("CREATE") || typeUpper.contains("REGISTER")) {
                onUserCreated(userId, user, event);
            } else if (typeUpper.contains("UPDATE")) {
                onUserUpdated(userId, user, event);
            } else if (typeUpper.contains("DELETE")) {
                onUserDeleted(userId, event);
            } else {
                onUserAction(userId, event);
            }
        } else {
            log.debug("Unhandled event type: {}", type);
        }
    }

    // --- Typed handlers — add your business logic here ---

    protected void onUserCreated(String userId, WebhookUserRepresentation user, KeycloakEventDto event) {
        log.info("User CREATED: id={} username={} email={}", userId,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null);
    }

    protected void onUserUpdated(String userId, WebhookUserRepresentation user, KeycloakEventDto event) {
        log.info("User UPDATED: id={} username={} email={}", userId,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null);
    }

    protected void onUserDeleted(String userId, KeycloakEventDto event) {
        log.info("User DELETED: id={}", userId);
    }

    protected void onUserAction(String userId, KeycloakEventDto event) {
        log.info("User ACTION type={}: id={}", event.getType(), userId);
    }

    // --- Helpers ---

    /**
     * Resolves the target user ID from the event.
     *
     * When Keycloak fires an admin event the target resource is always in resourcePath
     * (e.g. "users/abc-123"). userId in that case is the admin actor, not the target.
     * Prefer resourcePath when it contains a users/ segment; fall back to userId
     * for user-initiated events (LOGIN, REGISTER, etc.) where resourcePath is absent.
     */
    private String resolveUserId(KeycloakEventDto event) {
        String path = event.getResourcePath();
        if (path != null && path.contains("users/")) {
            return extractLastSegment(path);
        }
        return event.getUserId();
    }

    /**
     * Deserializes the `representation` JSON string into a {@link WebhookUserRepresentation}.
     * Returns null if the string is absent or cannot be parsed (e.g. on DELETE events).
     */
    private WebhookUserRepresentation parseRepresentation(String representation) {
        if (representation == null || representation.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(representation, WebhookUserRepresentation.class);
        } catch (Exception e) {
            log.warn("Failed to parse webhook user representation: {}", e.getMessage());
            return null;
        }
    }

    private String extractLastSegment(String resourcePath) {
        if (resourcePath == null) return null;
        String[] parts = resourcePath.split("/");
        return parts[parts.length - 1];
    }
}
