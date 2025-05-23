package edu.kdkce.openelectivefcfs.src.dto;

public record AdminResponse(
        String id,
        String name,
        String email,
        String role
) {}