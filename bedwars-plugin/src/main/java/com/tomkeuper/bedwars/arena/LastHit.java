package com.tomkeuper.bedwars.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class LastHit {

    private static final ConcurrentHashMap<UUID, LastHit> lastHit = new ConcurrentHashMap<>();
    private final UUID victim;
    @Setter
    private Entity damager;
    @Setter
    private long time;

    public LastHit(@NotNull Player victim, Entity damager, long time) {
        this.victim = victim.getUniqueId();
        this.damager = damager;
        this.time = time;
        lastHit.put(victim.getUniqueId(), this);
    }

    public static LastHit getLastHit(@NotNull Player player) {
        return lastHit.getOrDefault(player.getUniqueId(), null);
    }

    public void remove() {
        lastHit.remove(victim);
    }
}
