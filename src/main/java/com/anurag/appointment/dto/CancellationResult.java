package com.anurag.appointment.dto;

import com.anurag.appointment.entity.Match;

public record CancellationResult(boolean cancelled, String message, Match offer) {
}
