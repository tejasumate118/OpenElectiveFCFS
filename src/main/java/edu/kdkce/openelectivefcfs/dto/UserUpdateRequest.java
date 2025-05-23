package edu.kdkce.openelectivefcfs.dto;

public record UserUpdateRequest(
        String name,
        String rollNumber,
        Long contactNumber,
        Integer classRollNumber
) {
}
