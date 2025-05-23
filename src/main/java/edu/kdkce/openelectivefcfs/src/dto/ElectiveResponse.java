package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

import java.util.Set;

public record ElectiveResponse(
        String id,
        String name,
        DepartmentName department,
        Integer maxCapacity,
        Integer capacity,
        String description
) {
}
