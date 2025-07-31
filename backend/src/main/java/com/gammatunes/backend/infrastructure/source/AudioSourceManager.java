package com.gammatunes.backend.infrastructure.source;

import com.gammatunes.backend.application.port.out.TrackResolverPort;
import com.gammatunes.backend.domain.exception.TrackLoadException;
import com.gammatunes.backend.domain.exception.UnresolvableTrackException;
import com.gammatunes.backend.domain.model.Track;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages all available TrackResolvers and delegates the resolution of a query
 * to the appropriate one. This is the main entry point for resolving tracks.
 */
@Service
public class AudioSourceManager {

    private final List<TrackResolverPort> resolvers;

    /**
     * @param resolvers A list of all available TrackResolver beans.
     */
    public AudioSourceManager(List<TrackResolverPort> resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Iterates through the available resolvers to find one that can handle the query,
     * and then uses it to resolve the query into a Track.
     *
     * @param query The user's input (URL or search term).
     * @return A resolved {@link Track}.
     * @throws TrackLoadException if no suitable resolver is found or if loading fails.
     */
    public Track resolveTrack(String query) throws TrackLoadException {
        for (TrackResolverPort resolver : resolvers) {
            if (resolver.canResolve(query)) {
                return resolver.resolve(query);
            }
        }
        throw new UnresolvableTrackException("Could not find a resolver for query: " + query);
    }
}
