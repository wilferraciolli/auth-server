package com.wiltech.auth_server.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** Populated in responses. Leave null when creating a user — Keycloak assigns the ID. */
    private String id;

    @NotBlank
    private String username;

    @Email
    private String email;

    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;

    /** Accepted on create. Never returned in responses. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** When true the user must change their password on next login. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean temporaryPassword;

    // --- Mappers ---

    /** Builds a UserDto from Keycloak's internal UserRepresentation. */
    public static UserDto fromRepresentation(UserRepresentation rep) {
        return UserDto.builder()
                .id(rep.getId())
                .username(rep.getUsername())
                .email(rep.getEmail())
                .firstName(rep.getFirstName())
                .lastName(rep.getLastName())
                .enabled(rep.isEnabled())
                .emailVerified(rep.isEmailVerified())
                .build();
    }

    /** Converts this DTO into a Keycloak UserRepresentation for create/update calls. */
    public UserRepresentation toRepresentation() {
        UserRepresentation rep = new UserRepresentation();
        rep.setUsername(username);
        rep.setEmail(email);
        rep.setFirstName(firstName);
        rep.setLastName(lastName);
        if (enabled != null) rep.setEnabled(enabled);
        if (emailVerified != null) rep.setEmailVerified(emailVerified);
        return rep;
    }
}
