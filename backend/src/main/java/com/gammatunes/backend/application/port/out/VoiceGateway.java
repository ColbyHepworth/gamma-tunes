package com.gammatunes.backend.application.port.out;

import com.gammatunes.backend.domain.model.VoiceConnectRequest;
import com.gammatunes.backend.domain.model.VoiceDisconnectRequest;
import com.gammatunes.backend.presentation.bot.exception.MemberNotInVoiceChannelException;

public interface VoiceGateway {

    /**
     * Connects to a voice channel based on the provided VoiceConnectRequest.
     *
     * @param voiceConnectRequest the request containing details for connecting to a voice channel
     * @throws MemberNotInVoiceChannelException if the member is not in a voice channel
     */
    void connect(VoiceConnectRequest voiceConnectRequest);


    /**
     * Disconnects from a voice channel based on the provided VoiceDisconnectRequest.
     *
     * @param voiceConnectRequest the request containing details for disconnecting from a voice channel
     */
    void disconnect(VoiceDisconnectRequest voiceConnectRequest);
}
