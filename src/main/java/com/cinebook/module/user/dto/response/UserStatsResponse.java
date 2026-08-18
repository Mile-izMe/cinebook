package com.cinebook.module.user.dto.response;

import lombok.Builder;

@Builder
public record UserStatsResponse(
        Integer totalBookings,
        Integer totalTickets,
        Integer totalSpent
) {
}
