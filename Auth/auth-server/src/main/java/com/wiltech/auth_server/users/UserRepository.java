package com.wiltech.auth_server.users;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    /** Returns the UsersResource for the configured realm — the entry point for all user operations. */
    private UsersResource usersResource() {
        return keycloak.realm(realm).users();
    }

    public List<UserRepresentation> findAll(int first, int max) {
        return usersResource().list(first, max);
    }

    public Optional<UserRepresentation> findById(String id) {
        try {
            return Optional.ofNullable(usersResource().get(id).toRepresentation());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Creates a user in Keycloak and returns the new user's ID.
     * Keycloak returns HTTP 201 with a Location header containing the new ID.
     * CreatedResponseUtil extracts that ID for us.
     */
    public String create(UserRepresentation user) {
        try (Response response = usersResource().create(user)) {
            if (response.getStatus() != 201) {
                throw new ResponseStatusException(
                        HttpStatus.valueOf(response.getStatus()),
                        "Keycloak error: " + response.getStatusInfo().getReasonPhrase()
                );
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    public void update(String id, UserRepresentation user) {
        usersResource().get(id).update(user);
    }

    public void delete(String id) {
        usersResource().get(id).remove();
    }

    public void resetPassword(String id, CredentialRepresentation credential) {
        usersResource().get(id).resetPassword(credential);
    }
}
