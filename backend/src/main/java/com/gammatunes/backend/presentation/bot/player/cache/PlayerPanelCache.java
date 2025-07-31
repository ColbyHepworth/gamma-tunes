package com.gammatunes.backend.presentation.bot.player.cache;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A cache for the player panel state, which is used to store the message reference,
 * the last status text, and the progress bar state for each guild.
 */
@Component
public class PlayerPanelCache {

    private final Map<String, MessageRef> messages   = new ConcurrentHashMap<>();
    private final Map<String, String>     lastStatus = new ConcurrentHashMap<>();
    private final Map<String, Integer>    lastBarIdx = new ConcurrentHashMap<>();
    private final Map<String, Long>       lastEditTs = new ConcurrentHashMap<>();

    /* MessageRef */
    public Optional<MessageRef> getMessage(String guild)        { return Optional.ofNullable(messages.get(guild)); }
    public void putMessage(String guild, MessageRef ref)        { messages.put(guild, ref); }
    public void removeMessage(String guild)                     { messages.remove(guild); }

    /* Status */
    public void setStatus(String guild, String txt)             { lastStatus.put(guild, txt); }
    public String getStatus(String guild)                       { return lastStatus.get(guild); }

    /* Progress-bar bookkeeping */
    public int  getBarIdx(String guild)                         { return lastBarIdx.getOrDefault(guild, -1); }
    public void setBarIdx(String guild, int idx)                { lastBarIdx.put(guild, idx); }
    public long getEditTs(String guild)                         { return lastEditTs.getOrDefault(guild, 0L); }
    public void setEditTs(String guild, long ts)                { lastEditTs.put(guild, ts); }

    public Set<String> guildIds() {
        return messages.keySet();
    }
}
