package com.energyex.controller;

import com.energyex.dto.ScheduleDTO;
import com.energyex.entity.Schedule;
import com.energyex.service.ScheduleRunnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleRunnerService scheduleRunnerService;

    public ScheduleController(ScheduleRunnerService scheduleRunnerService) {
        this.scheduleRunnerService = scheduleRunnerService;
    }

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleRunnerService.getAllSchedules());
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleDTO dto) {
        Schedule created = scheduleRunnerService.createSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (scheduleRunnerService.deleteSchedule(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

