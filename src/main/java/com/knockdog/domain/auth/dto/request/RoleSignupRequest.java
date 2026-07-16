package com.knockdog.domain.auth.dto.request;

import com.knockdog.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleSignupRequest(@NotNull Role role) {
}
