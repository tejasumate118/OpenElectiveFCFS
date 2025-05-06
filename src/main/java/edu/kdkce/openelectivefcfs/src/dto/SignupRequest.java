package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

public record SignupRequest(String  name, String rollNumber, DepartmentName department, String email, String password, Long contactNumber) {
}
