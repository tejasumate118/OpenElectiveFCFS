package edu.kdkce.openelectivefcfs.src.dto;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;

public record AdminPanelElectiveStatResponse(
        String id,
        DepartmentName name,
        String electiveName,
        Integer maxCapacity,
        Integer capacity,
        Integer enrolled

) {
}
