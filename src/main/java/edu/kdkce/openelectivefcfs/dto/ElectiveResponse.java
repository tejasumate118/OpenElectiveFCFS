package edu.kdkce.openelectivefcfs.dto;

import edu.kdkce.openelectivefcfs.enums.DepartmentName;

public record ElectiveResponse(
        String id,
        String name,
        DepartmentName department,
        Integer maxCapacity,
        Integer capacity,
        String description
) {
}
