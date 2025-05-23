package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

public record UserProfileResponse(
        String id,
        String name,
        String email,
        String role,
        DepartmentName department,
        String rollNumber,
        Long contactNumber,
        ElectiveSelected electiveSelected,
        Integer classRollNumber
) {
    public static UserProfileResponse adminPanelStudentData(String id, String name, Long contactNumber,String rollNumber,DepartmentName department, ElectiveSelected electiveSelected, Integer classRollNumber) {
        return new UserProfileResponse(id, name, null, null, department, rollNumber, contactNumber, electiveSelected, classRollNumber);
    }
}
