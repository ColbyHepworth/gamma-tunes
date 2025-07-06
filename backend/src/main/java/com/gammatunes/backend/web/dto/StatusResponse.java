package com.gammatunes.backend.web.dto;

/**
 * Represents a response containing the status of an operation.
 * This can be used to indicate success, failure, or any other status message.
 *
 * @param status The status message to be returned in the response.
 */

public record StatusResponse(String status) {
}
