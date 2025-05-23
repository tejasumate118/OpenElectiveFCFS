package edu.kdkce.openelectivefcfs.dto;

import java.time.ZonedDateTime;

public record ElectiveTimeResponse(
        ZonedDateTime allocationStartDate,
        ZonedDateTime allocationEndDate,
        Boolean isAllocationActive
) {
}