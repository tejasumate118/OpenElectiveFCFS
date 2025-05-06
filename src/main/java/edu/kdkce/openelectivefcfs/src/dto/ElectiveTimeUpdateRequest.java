package edu.kdkce.openelectivefcfs.src.dto;

import java.time.ZonedDateTime;

public record ElectiveTimeUpdateRequest(
        ZonedDateTime allocationStartDate,
        ZonedDateTime allocationEndDate
) {
}
