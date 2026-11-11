package com.energyex.websocket;

import com.energyex.dto.DashboardSummaryDTO;
import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(TelemetryBroadcaster.class);
    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastDashboard(DashboardSummaryDTO summary) {
        try {
            messagingTemplate.convertAndSend("/topic/telemetry", summary);
        } catch (Exception e) {
            log.debug("WebSocket broadcast error: {}", e.getMessage());
        }
    }

    public void broadcastApplianceUpdate(Appliance appliance) {
        try {
            messagingTemplate.convertAndSend("/topic/appliances", appliance);
        } catch (Exception e) {
            log.debug("WebSocket appliance broadcast error: {}", e.getMessage());
        }
    }

    public void broadcastAlert(Alert alert) {
        try {
            messagingTemplate.convertAndSend("/topic/alerts", alert);
        } catch (Exception e) {
            log.debug("WebSocket alert broadcast error: {}", e.getMessage());
        }
    }
}

