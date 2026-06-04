package io.nh_backend.rest_quest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UseCreateRequest(
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Size(min=8, max = 64) String password,
        @NotBlank @Size(min=2, max = 20) String nickname) {
}
