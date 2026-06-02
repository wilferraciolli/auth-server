package com.wiltech.auth_server.webhooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Payload sent by the Keycloak webhook plugin (vymalo/keycloak-webhook) for admin events.
 *
 * Fields map to Keycloak's AdminEvent structure:
 *   resourceType  → what changed: USER, GROUP, CLIENT, REALM_ROLE, etc.
 *   operationType → how it changed: CREATE, UPDATE, DELETE, ACTION
 *   resourcePath  → path to the resource, e.g. "users/abc-123"
 *   representation → full JSON of the entity (as a raw String) — present on CREATE and UPDATE
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakEventDto {

    private Long time;
    private String realmId;
    private String realmName;
    private String resourceType;
    private String operationType;
    private String resourcePath;
    private String representation;
    private String error;
}
