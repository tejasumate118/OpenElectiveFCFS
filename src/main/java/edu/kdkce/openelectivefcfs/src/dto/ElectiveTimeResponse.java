package edu.kdkce.openelectivefcfs.src.dto;

import java.time.ZonedDateTime;

public record ElectiveTimeResponse(
        ZonedDateTime allocationStartDate,
        ZonedDateTime allocationEndDate,
        Boolean isAllocationActive
) {
}