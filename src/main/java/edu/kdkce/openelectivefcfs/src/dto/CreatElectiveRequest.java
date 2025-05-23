package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

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
