package edu.kdkce.openelectivefcfs.src.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Entity
public class Settings {
    @Id
    private Long id = 1L;

    private ZonedDateTime electiveOpeningTime;
    private ZonedDateTime electiveClosingTime;

    public Settings() {
        // Default values (Example: IST Timezone)
        this.electiveOpeningTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        this.electiveClosingTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusDays(1);
    }

    // Getters and Setters
    public ZonedDateTime getElectiveOpeningTime() {
        return electiveOpeningTime;
    }

    public void setElectiveOpeningTime(ZonedDateTime electiveOpeningTime) {
        this.electiveOpeningTime = electiveOpeningTime;
    }

    public ZonedDateTime getElectiveClosingTime() {
        return electiveClosingTime;
    }

    public void setElectiveClosingTime(ZonedDateTime electiveClosingTime) {
        this.electiveClosingTime = electiveClosingTime;
    }
}
