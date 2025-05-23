package edu.kdkce.openelectivefcfs.dto;

import edu.kdkce.openelectivefcfs.enums.DepartmentName;

public record SignupRequest(String  name, String rollNumber, DepartmentName department, String email, String password, Long contactNumber) {
}
