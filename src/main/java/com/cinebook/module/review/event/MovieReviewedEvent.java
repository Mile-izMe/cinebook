package com.cinebook.module.review.event;

import java.io.Serializable;
import java.util.UUID;

/**
 * Published after a review is inserted/updated/deleted. Consumed
 * asynchronously to recalculate movie.score - never done synchronously
 * in the request that creates the review.
 */
public record MovieReviewedEvent(UUID movieId) implements Serializable {
}