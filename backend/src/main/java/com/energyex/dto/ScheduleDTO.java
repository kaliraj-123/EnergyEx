package com.energyex.dto;

public class ScheduleDTO {
    private Long id;
    private Long applianceId;
    private String applianceName;
    private String scheduleName;
    private String actionType; // TURN_ON, TURN_OFF
    private String scheduledTime; // "HH:mm"
    private String repeatDays;
    private Boolean isActive;

    public ScheduleDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplianceId() { return applianceId; }
    public void setApplianceId(Long applianceId) { this.applianceId = applianceId; }

    public String getApplianceName() { return applianceName; }
    public void setApplianceName(String applianceName) { this.applianceName = applianceName; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getRepeatDays() { return repeatDays; }
    public void setRepeatDays(String repeatDays) { this.repeatDays = repeatDays; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

