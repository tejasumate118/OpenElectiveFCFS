package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.ElectiveTimeResponse;
import edu.kdkce.openelectivefcfs.src.dto.ElectiveTimeUpdateRequest;
import edu.kdkce.openelectivefcfs.src.model.Settings;
import edu.kdkce.openelectivefcfs.src.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public ElectiveTimeResponse getElectiveTime() {
        Settings settings = settingsRepository.findById("1").orElseThrow(() -> new RuntimeException("Settings not found"));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        boolean isActive = now.isAfter(settings.getElectiveOpeningTime()) && now.isBefore(settings.getElectiveClosingTime());

        return new ElectiveTimeResponse(
                settings.getElectiveOpeningTime(),
                settings.getElectiveClosingTime(),
                isActive
        );
    }

    public void updateElectiveTime(ElectiveTimeUpdateRequest request) {
        Settings settings = settingsRepository.findById("1").orElse(new Settings());
        settings.setElectiveOpeningTime(request.allocationStartDate());
        settings.setElectiveClosingTime(request.allocationEndDate());
        settingsRepository.update(settings);
    }
}