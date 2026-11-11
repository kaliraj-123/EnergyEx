package com.energyex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // HIGH_CONSUMPTION, ANOMALY, VOLTAGE_FLUCTUATION, HARDWARE_STATUS, SCHEDULE_TRIGGER

    @Column(length = 20)
    private String severity = "WARNING"; // INFO, WARNING, CRITICAL

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appliance_id")
    private Appliance appliance;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_demo_alert")
    private Boolean isDemoAlert = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Alert() {}

    public Alert(String title, String message, String alertType, String severity, Appliance appliance, Boolean isDemoAlert) {
        this.title = title;
        this.message = message;
        this.alertType = alertType;
        this.severity = severity;
        this.appliance = appliance;
        this.isDemoAlert = isDemoAlert;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Appliance getAppliance() { return appliance; }
    public void setAppliance(Appliance appliance) { this.appliance = appliance; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Boolean getIsDemoAlert() { return isDemoAlert; }
    public void setIsDemoAlert(Boolean isDemoAlert) { this.isDemoAlert = isDemoAlert; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

