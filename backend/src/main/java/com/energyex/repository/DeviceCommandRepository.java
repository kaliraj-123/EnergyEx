package com.energyex.repository;

import com.energyex.entity.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    List<DeviceCommand> findByDeviceIdAndExecutionStatus(String deviceId, String executionStatus);
    Optional<DeviceCommand> findFirstByApplianceIdOrderByRequestedAtDesc(Long applianceId);
}

