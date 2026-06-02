package com.wiltech.auth_server.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordDto {

    @NotBlank
    private String password;

    /** When true the user must change their password on next login. Defaults to false. */
    private boolean temporary = false;
}
