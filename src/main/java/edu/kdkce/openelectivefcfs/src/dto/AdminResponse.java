package edu.kdkce.openelectivefcfs.src.dto;

public record AdminResponse(
        Long id,
        String name,
        String email,
        String role
) {}