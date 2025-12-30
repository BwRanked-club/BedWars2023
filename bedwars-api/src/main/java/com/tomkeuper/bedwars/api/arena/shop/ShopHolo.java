package com.tomkeuper.bedwars.api.arena.shop;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ShopHolo {

    private final IHologram hologram;
    private final IArena a;
    private final ITeam t;

    public ShopHolo(@Nonnull IHologram hologram, IArena a, ITeam t) {
        this.hologram = hologram;
        this.a = a;
        this.t = t;
    }

    public IHologram getHologram() {
        return hologram;
    }

    public IArena getArena() {
        return a;
    }

    public ITeam getTeam() {
        return t;
    }

    public void update() {
        hologram.update();
    }

    public void update(Player p) {
        hologram.update(p);
    }

    public void clear() {
        hologram.remove();
    }

    public void clearForPlayer(Player p) {
        hologram.removePlayer(p);
    }
}