package com.energyex.dto;

public class SystemModeDTO {
    private String mode; // DEMO or LIVE_IOT
    private String label;
    private Boolean simulationActive;
    private String message;

    public SystemModeDTO() {}

    public SystemModeDTO(String mode, String label, Boolean simulationActive, String message) {
        this.mode = mode;
        this.label = label;
        this.simulationActive = simulationActive;
        this.message = message;
    }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getSimulationActive() { return simulationActive; }
    public void setSimulationActive(Boolean simulationActive) { this.simulationActive = simulationActive; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

