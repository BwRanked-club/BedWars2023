package com.tomkeuper.bedwars.support.vipfeatures;

import com.andrei1058.vipfeatures.api.MiniGame;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VipFeatures extends MiniGame {

    public VipFeatures(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isPlaying(Player p) {
        IArena a = Arena.getArenaByPlayer(p);
        if (a != null) {
            return !(a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting);
        }
        return false;
    }

    @Override
    public boolean hasBoosters() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "BedWars2023";
    }
}
