package com.energyex.repository;

import com.energyex.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByIsActiveTrue();
    List<Schedule> findByApplianceId(Long applianceId);
}

