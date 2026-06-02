package com.wiltech.auth_server.webhooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Payload sent by the Keycloak webhook plugin (vymalo/keycloak-webhook).
 *
 * Plugin sends a compound `type` field (e.g. "ADMIN_USER_CREATE", "REGISTER", "LOGIN")
 * rather than separate resourceType/operationType fields.
 * userId is provided directly; resourcePath is also present for admin events.
 * representation contains the full entity JSON as a raw String on CREATE/UPDATE.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakEventDto {

    /** Compound event type, e.g. "ADMIN_USER_CREATE", "REGISTER", "LOGIN". */
    private String type;

    private String id;
    private Long time;
    private String realmId;
    private String realmName;
    private String clientId;

    /** User ID directly provided by the plugin (admin events). */
    private String userId;

    private String ipAddress;
    private String resourcePath;
    private String representation;
    private String error;
}
