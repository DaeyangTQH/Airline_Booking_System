package com.group3.api.dto.admin;
import jakarta.validation.constraints.NotNull;
public record UpdateUserStatusRequest(@NotNull Boolean active) { }
