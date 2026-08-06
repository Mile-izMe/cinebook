package com.cinebook.module.seatlock.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatMapBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatLocked(UUID showtimeId, UUID seatId) {
        send(showtimeId, new SeatEvent("SEAT_LOCKED", seatId));
    }

    public void broadcastSeatReleased(UUID showtimeId, UUID seatId) {
        send(showtimeId, new SeatEvent("SEAT_RELEASED", seatId));
    }

    private void send(UUID showtimeId, SeatEvent event) {
        messagingTemplate.convertAndSend("/topic/showtimes/" + showtimeId + "/seats", event);
    }

    public record SeatEvent(
            String type,
            UUID seat
    ) {
    }
}
