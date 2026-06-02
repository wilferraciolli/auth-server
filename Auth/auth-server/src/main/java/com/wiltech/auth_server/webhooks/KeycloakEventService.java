package com.wiltech.auth_server.webhooks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Dispatches incoming Keycloak admin events to typed handler methods.
 *
 * Override or extend the on* methods to react to specific lifecycle events.
 * Currently logs each event — replace log statements with real business logic
 * (e.g. publishing to a message broker, updating a cache, sending a notification).
 */
@Slf4j
@Service
public class KeycloakEventService {

    public void handle(KeycloakEventDto event) {
        String resourceType = event.getResourceType();
        String operationType = event.getOperationType();

        if (resourceType == null || operationType == null) {
            log.warn("Received Keycloak event with null resourceType or operationType: {}", event);
            return;
        }

        if ("USER".equals(resourceType)) {
            String userId = extractId(event.getResourcePath());
            switch (operationType) {
                case "CREATE" -> onUserCreated(userId, event);
                case "UPDATE" -> onUserUpdated(userId, event);
                case "DELETE" -> onUserDeleted(userId, event);
                case "ACTION" -> onUserAction(userId, event);
                default -> log.debug("Unhandled USER operation: {}", operationType);
            }
        } else {
            log.debug("Unhandled resource type: {} / {}", resourceType, operationType);
        }
    }

    // --- Typed handlers — add your business logic here ---

    protected void onUserCreated(String userId, KeycloakEventDto event) {
        log.info("User CREATED: id={} realm={}", userId, event.getRealmName());
    }

    protected void onUserUpdated(String userId, KeycloakEventDto event) {
        log.info("User UPDATED: id={} realm={}", userId, event.getRealmName());
    }

    protected void onUserDeleted(String userId, KeycloakEventDto event) {
        log.info("User DELETED: id={} realm={}", userId, event.getRealmName());
    }

    protected void onUserAction(String userId, KeycloakEventDto event) {
        log.info("User ACTION: id={} realm={}", userId, event.getRealmName());
    }

    // --- Helpers ---

    /**
     * Extracts the entity ID from a Keycloak resource path.
     * e.g. "users/abc-123" → "abc-123"
     */
    private String extractId(String resourcePath) {
        if (resourcePath == null) return null;
        String[] parts = resourcePath.split("/");
        return parts[parts.length - 1];
    }
}
