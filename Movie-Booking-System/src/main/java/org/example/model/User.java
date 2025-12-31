package org.example.model;

public record User(
        String userId,
        String username,
        String passwordHash
) {}
