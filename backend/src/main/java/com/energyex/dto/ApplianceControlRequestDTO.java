package com.energyex.dto;

public class ApplianceControlRequestDTO {
    private String state; // ON or OFF

    public ApplianceControlRequestDTO() {}

    public ApplianceControlRequestDTO(String state) {
        this.state = state;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}

