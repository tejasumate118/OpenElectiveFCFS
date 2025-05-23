package edu.kdkce.openelectivefcfs.dto;

import java.time.ZonedDateTime;

public record ElectiveTimeUpdateRequest(
        ZonedDateTime allocationStartDate,
        ZonedDateTime allocationEndDate
) {
}
