package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

public record CreatElectiveRequest(
        String name,
        DepartmentName department,
        Integer maxCapacity,
        Integer capacity,
        String description
) {
}
