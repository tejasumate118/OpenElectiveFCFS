package edu.kdkce.openelectivefcfs.dto;

import edu.kdkce.openelectivefcfs.enums.DepartmentName;

public record AdminPanelElectiveStatResponse(
        String id,
        DepartmentName name,
        String electiveName,
        Integer maxCapacity,
        Integer capacity,
        Integer enrolled

) {
}
