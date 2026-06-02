package com.wiltech.auth_server.users;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getUsers(int page, int size) {
        return userRepository.findAll(page * size, size)
                .stream()
                .map(UserDto::fromRepresentation)
                .toList();
    }

    public UserDto getUser(String id) {
        return userRepository.findById(id)
                .map(UserDto::fromRepresentation)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    /**
     * Creates the user then, if a password was supplied, sets it in a second call.
     * Keycloak's create endpoint does not accept credentials in the same request.
     * Returns the new user's Keycloak-assigned ID.
     */
    public String createUser(UserDto dto) {
        UserRepresentation rep = dto.toRepresentation();
        rep.setEnabled(true);

        String id = userRepository.create(rep);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            setPassword(id, dto.getPassword(), Boolean.TRUE.equals(dto.getTemporaryPassword()));
        }

        return id;
    }

    /**
     * Partial update — fetches the existing user first, then only overwrites
     * fields that are non-null in the incoming DTO.
     */
    public void updateUser(String id, UserDto dto) {
        UserRepresentation existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
        if (dto.getEnabled() != null) existing.setEnabled(dto.getEnabled());

        userRepository.update(id, existing);
    }

    public void deleteUser(String id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        userRepository.delete(id);
    }

    public void setPassword(String id, String password, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(temporary);
        userRepository.resetPassword(id, credential);
    }

    /**
     * Searches by exact username or email — at least one must be provided.
     * Returns the first match or empty if not found.
     */
    public List<UserDto> searchUsers(String username, String email) {
        return userRepository.search(username, email)
                .stream()
                .map(UserDto::fromRepresentation)
                .toList();
    }

}
