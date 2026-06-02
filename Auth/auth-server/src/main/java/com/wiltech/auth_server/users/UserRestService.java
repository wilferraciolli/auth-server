package com.wiltech.auth_server.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestService {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getUsers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserDto dto) {
        String id = userService.createUser(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable String id, @RequestBody UserDto dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> setPassword(@PathVariable String id, @Valid @RequestBody PasswordDto dto) {
        userService.setPassword(id, dto.getPassword(), dto.isTemporary());
        return ResponseEntity.noContent().build();
    }

    /**
     * Search by exact username or email.
     * At least one query param must be provided.
     * Example: GET /api/users/search?username=john.doe
     *          GET /api/users/search?email=john@example.com
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(userService.searchUsers(username, email));
    }


}
