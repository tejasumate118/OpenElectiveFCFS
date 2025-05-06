package edu.kdkce.openelectivefcfs.src.dto;

public record UserUpdateRequest(
        String name,
        String rollNumber,
        Long contactNumber
) {
}
