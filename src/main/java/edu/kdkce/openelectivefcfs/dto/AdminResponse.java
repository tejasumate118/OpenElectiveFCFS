package edu.kdkce.openelectivefcfs.dto;

public record AdminResponse(
        String id,
        String name,
        String email,
        String role
) {}