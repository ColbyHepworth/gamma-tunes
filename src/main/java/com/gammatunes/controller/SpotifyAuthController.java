package com.gammatunes.controller;

import com.gammatunes.service.SpotifyAccountLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyAuthController {

    private final SpotifyAccountLinkService spotifyAccountLinkService;

    @GetMapping("/connect")
    public ResponseEntity<Void> connect(
        @RequestParam long discordUserId,
        @RequestParam long guildId
    ) {
        URI authorizeUri = spotifyAccountLinkService.createAuthorizationUri(discordUserId, guildId);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(authorizeUri)
            .build();
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> callback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String error,
        @RequestParam String state
    ) {
        if (error != null && !error.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                .body("Spotify authorization failed: " + error));
        }
        if (code == null || code.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                .body("Spotify authorization failed: missing code."));
        }

        return spotifyAccountLinkService.completeLink(code, state)
            .map(account -> ResponseEntity.ok(
                "Spotify connected for Discord user " + account.discordUserId()
                    + ". You can return to Discord."
            ));
    }
}
