package com.gammatunes.component.spotify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties(prefix = "spotify")
public class SpotifyProperties {
    private String clientId;
    private String clientSecret;
    private URI redirectUri;
}
