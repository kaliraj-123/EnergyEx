package com.energyex.service;

import com.energyex.dto.ApplianceControlRequestDTO;
import com.energyex.dto.ScheduleDTO;
import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import com.energyex.entity.Schedule;
import com.energyex.repository.AlertRepository;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.ScheduleRepository;
import com.energyex.websocket.TelemetryBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleRunnerService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleRunnerService.class);

    private final ScheduleRepository scheduleRepository;
    private final ApplianceRepository applianceRepository;
    private final ApplianceService applianceService;
    private final AlertRepository alertRepository;
    private final TelemetryBroadcaster broadcaster;

    public ScheduleRunnerService(ScheduleRepository scheduleRepository,
                                 ApplianceRepository applianceRepository,
                                 ApplianceService applianceService,
                                 AlertRepository alertRepository,
                                 TelemetryBroadcaster broadcaster) {
        this.scheduleRepository = scheduleRepository;
        this.applianceRepository = applianceRepository;
        this.applianceService = applianceService;
        this.alertRepository = alertRepository;
        this.broadcaster = broadcaster;
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Transactional
    public Schedule createSchedule(ScheduleDTO dto) {
        Appliance appliance = applianceRepository.findById(dto.getApplianceId())
                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + dto.getApplianceId()));

        Schedule schedule = new Schedule();
        schedule.setAppliance(appliance);
        schedule.setScheduleName(dto.getScheduleName());
        schedule.setActionType(dto.getActionType().toUpperCase());
        schedule.setScheduledTime(LocalTime.parse(dto.getScheduledTime()));
        schedule.setRepeatDays(dto.getRepeatDays() != null ? dto.getRepeatDays().toUpperCase() : "DAILY");
        schedule.setIsActive(Boolean.TRUE.equals(dto.getIsActive()));

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public boolean deleteSchedule(Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Runs every 30 seconds to check and trigger active schedules
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void evaluateSchedules() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        List<Schedule> activeSchedules = scheduleRepository.findByIsActiveTrue();
        for (Schedule s : activeSchedules) {
            LocalTime scheduled = s.getScheduledTime().withSecond(0).withNano(0);

            if (scheduled.equals(now)) {
                // Check repeat condition
                if (matchesRepeat(s.getRepeatDays(), today)) {
                    log.info("Triggering schedule '{}' for appliance '{}'", s.getScheduleName(), s.getAppliance().getName());

                    String action = s.getActionType().contains("ON") ? "ON" : "OFF";
                    applianceService.controlAppliance(s.getAppliance().getId(), new ApplianceControlRequestDTO(action));

                    s.setLastTriggeredAt(LocalDateTime.now());
                    if ("ONCE".equalsIgnoreCase(s.getRepeatDays())) {
                        s.setIsActive(false);
                    }
                    scheduleRepository.save(s);

                    Alert alert = new Alert(
                            "Schedule Executed: " + s.getScheduleName(),
                            "Appliance '" + s.getAppliance().getName() + "' was automatically turned " + action + " by schedule.",
                            "SCHEDULE_TRIGGER", "INFO", s.getAppliance(), true
                    );
                    alertRepository.save(alert);
                    broadcaster.broadcastAlert(alert);
                }
            }
        }
    }

    private boolean matchesRepeat(String repeatDays, DayOfWeek today) {
        if (repeatDays == null || "DAILY".equalsIgnoreCase(repeatDays)) return true;
        if ("WEEKDAYS".equalsIgnoreCase(repeatDays)) {
            return today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
        }
        if ("WEEKENDS".equalsIgnoreCase(repeatDays)) {
            return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
        }
        return true;
    }
}

