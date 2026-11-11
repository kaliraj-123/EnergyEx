package com.energyex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_commands")
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appliance_id", nullable = false)
    private Appliance appliance;

    @Column(name = "relay_channel", nullable = false)
    private Integer relayChannel;

    @Column(name = "command_state", nullable = false, length = 20)
    private String commandState; // ON, OFF

    @Column(name = "execution_status", length = 30)
    private String executionStatus = "PENDING"; // PENDING, EXECUTED, CONFIRMED, FAILED

    @Column(name = "is_simulated")
    private Boolean isSimulated = true;

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    public DeviceCommand() {}

    public DeviceCommand(String deviceId, Appliance appliance, Integer relayChannel, String commandState, Boolean isSimulated) {
        this.deviceId = deviceId;
        this.appliance = appliance;
        this.relayChannel = relayChannel;
        this.commandState = commandState;
        this.isSimulated = isSimulated;
        this.executionStatus = "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Appliance getAppliance() { return appliance; }
    public void setAppliance(Appliance appliance) { this.appliance = appliance; }

    public Integer getRelayChannel() { return relayChannel; }
    public void setRelayChannel(Integer relayChannel) { this.relayChannel = relayChannel; }

    public String getCommandState() { return commandState; }
    public void setCommandState(String commandState) { this.commandState = commandState; }

    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }

    public Boolean getIsSimulated() { return isSimulated; }
    public void setIsSimulated(Boolean isSimulated) { this.isSimulated = isSimulated; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}

