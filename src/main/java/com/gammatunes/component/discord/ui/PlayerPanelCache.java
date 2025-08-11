package com.gammatunes.component.discord.ui;

import com.gammatunes.model.dto.MessageRef;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache for player panel state, including message references, last status,
 * progress bar index, and last edit timestamp for each guild.
 * This cache is used to maintain the state of the player panel across interactions.
 */
@Component
public class PlayerPanelCache {

    private final Map<Long, MessageRef> messages   = new ConcurrentHashMap<>();
    private final Map<Long, String>     lastStatus = new ConcurrentHashMap<>();
    private final Map<Long, Integer>    lastBarIdx = new ConcurrentHashMap<>();
    private final Map<Long, Long>       lastEditTs = new ConcurrentHashMap<>();

    /* MessageRef */
    public Optional<MessageRef> getMessage(long guild)        { return Optional.ofNullable(messages.get(guild)); }
    public void putMessage(long guild, MessageRef ref)        { messages.put(guild, ref); }
    public void removeMessage(long guild)                     { messages.remove(guild); }

    /* Status */
    public void setStatus(long guild, String txt)             { lastStatus.put(guild, txt); }
    public String getStatus(long guild)                       { return lastStatus.get(guild); }

    /* Progress-bar bookkeeping */
    public int  getBarIdx(Long guild)                         { return lastBarIdx.getOrDefault(guild, -1); }
    public void setBarIdx(Long guild, int idx)                { lastBarIdx.put(guild, idx); }
    public long getEditTs(Long guild)                         { return lastEditTs.getOrDefault(guild, 0L); }
    public void setEditTs(Long guild, long ts)                { lastEditTs.put(guild, ts); }

    public Set<Long> guildIds() {
        return messages.keySet();
    }
}
