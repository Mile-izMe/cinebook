package com.cinebook.module.seatlock.listener;

import com.cinebook.module.seatlock.websocket.SeatMapBroadcaster;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SeatLockExpirationListener extends KeyExpirationEventMessageListener {

    private final SeatMapBroadcaster broadcaster;

    public SeatLockExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      SeatMapBroadcaster broadcaster) {
        super(listenerContainer);
        this.broadcaster = broadcaster;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString(); // eg: "lock:seat:{showtimeId}:{seatId}"

        if (!expiredKey.startsWith("lock:seat:")) return; // pass other key not related

        String[] parts = expiredKey.split(":");
        if (parts.length != 4) return;

        UUID showtimeId = UUID.fromString(parts[2]);
        UUID seatId = UUID.fromString(parts[3]);

        // Note: No Booking to update - notify FE chair is AVAILABLE.
        broadcaster.broadcastSeatReleased(showtimeId, seatId);
    }
}
