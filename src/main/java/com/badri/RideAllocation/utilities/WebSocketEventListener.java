package com.badri.RideAllocation.utilities;

import com.badri.RideAllocation.controller.WebSocketController;
import com.badri.RideAllocation.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {
    private final Map<String, Long> sessionToDriver = new ConcurrentHashMap<>();
    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleDriverSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(destination != null && destination.startsWith("/topic/driver/")) {
            Long driverId = Long.parseLong(destination.substring("/topic/driver/".length()));

            sessionToDriver.put(accessor.getSessionId(), driverId);
            System.out.println("sessionId: " + accessor.getSessionId() + " driverId: " + driverId );

            if(driverId != null) {
                String status = "ONLINE";
                String lastSeen = String.valueOf(Instant.now().toEpochMilli());
                presenceService.updateDriverPresence(String.valueOf(driverId), status, lastSeen);
            }
        }
    }

    @EventListener
    public void handleDriverDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long driverId = sessionToDriver.remove(accessor.getSessionId());
        System.out.println("Disconnected Driver: " + driverId);

        if(driverId != null) {
            String status = "OFFLINE";
            String lastSeen = String.valueOf(Instant.now().toEpochMilli());
            presenceService.updateDriverPresence(String.valueOf(driverId), status, lastSeen);
        }
    }
}
