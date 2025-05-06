package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String role,
        DepartmentName department,
        String rollNumber,
        Long contactNumber,
        ElectiveSelected electiveSelected
) {
    public static UserProfileResponse adminPanelStudentData(Long id, String name, Long contactNumber,String rollNumber,DepartmentName department, ElectiveSelected electiveSelected) {
        return new UserProfileResponse(id, name, null, null, department, rollNumber, contactNumber, electiveSelected);
    }
}
