package com.gammatunes.model.dto;

import net.dv8tion.jda.api.entities.Member;

/**
 * Represents information about the requester of an interaction, typically a Discord user.
 * This record captures the user's ID, display name, and avatar URL.
 */
public record RequesterInfo(String userId, String displayName, String avatarUrl) {
    public static RequesterInfo fromMember(Member member) {
        String avatarUrl = member.getEffectiveAvatarUrl();
        return new RequesterInfo(member.getId(), member.getEffectiveName(), avatarUrl);
    }
}
