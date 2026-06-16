package com.badri.RideAllocation.service;

import com.badri.RideAllocation.events.RideQueueEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyRideRequestToDriverThroughWebSocket(String driverId, RideQueueEvent rideData) {
        messagingTemplate.convertAndSend(
                "/topic/driver/" + driverId,
                rideData
        );
    }

    public void notifyRider(String userId, String msg) {
        messagingTemplate.convertAndSend(
                "/topic/rider/" + userId,
                msg
        );
    }

    public void notifyDriver(String driverId, String msg) {
        messagingTemplate.convertAndSend(
                "/topic/driver/" + driverId,
                msg
        );
    }
}
