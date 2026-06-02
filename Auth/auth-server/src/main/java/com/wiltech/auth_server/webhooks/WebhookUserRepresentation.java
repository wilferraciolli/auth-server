package com.wiltech.auth_server.webhooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Deserialized form of the `representation` field in a Keycloak webhook event.
 * Present on CREATE and UPDATE events; null on DELETE.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookUserRepresentation {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
    private Boolean enabled;
    private Long createdTimestamp;
    private Map<String, List<String>> attributes;
    private List<String> requiredActions;
}
