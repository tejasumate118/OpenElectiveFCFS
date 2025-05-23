package edu.kdkce.openelectivefcfs.dto;

import edu.kdkce.openelectivefcfs.enums.DepartmentName;

import java.util.Set;

public record CreatElectiveRequest(
        String name,
        DepartmentName department,
        Integer maxCapacity,
        Integer capacity,
        String description,
        Set<DepartmentName> allowedDepartments
) {
}
