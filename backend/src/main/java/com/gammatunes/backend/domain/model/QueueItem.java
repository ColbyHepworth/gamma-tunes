package com.gammatunes.backend.domain.model;

public record QueueItem(
    Track track,
    Requester requester
) {}
