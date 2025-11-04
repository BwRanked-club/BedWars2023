package com.tomkeuper.bedwars.api.arena.shop;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.hologram.containers.IHoloLine;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ShopHolo {
    /**
     * Shop holograms per language <iso, holo></iso,>
     */
    @Getter
    private static HashMap<Player, List<ShopHolo>> shopHolo = new HashMap<>();

    @Getter
    private final IHologram hologram;
    private final Location l;
    private final IArena a;
    private final ITeam t;

    public ShopHolo(@Nonnull IHologram hologram, Location l, IArena a, ITeam t) {
        this.l = l;
        this.hologram = hologram;
        this.a = a;
        this.t = t;
        shopHolo.putIfAbsent(hologram.getPlayer(), new ArrayList<>());
        shopHolo.get(hologram.getPlayer()).add(this);
    }

    public void update() {
        if (l == null) Bukkit.broadcastMessage("LOCATION IS NULL");
        hologram.getLines().forEach(IHoloLine::reveal);
    }

    public static void clearForArena(IArena arena) {
        shopHolo.forEach((p, h) -> h.stream()
                .filter(holo -> holo.getArena() == arena)
                .collect(Collectors.toList())
                .forEach(holo -> holo.getHologram().remove()));
        shopHolo.entrySet().removeIf(entry -> entry.getValue().stream().anyMatch(holo -> holo.getArena() == arena));
    }

    public IArena getArena() {
        return a;
    }

    public ITeam getTeam() {
        return t;
    }

    public static void clearForPlayer(Player p) {
        if (!shopHolo.containsKey(p)) return;
        shopHolo.get(p).forEach(h -> h.getHologram().remove());
        shopHolo.remove(p);
    }

    public static void clear() {
        shopHolo.forEach((p, h) -> h.forEach(holo -> holo.getHologram().remove()));
        shopHolo.clear();
    }

    public static void add(Player p, ShopHolo holo) {
        if (shopHolo.containsKey(p)) {
            shopHolo.get(p).add(holo);
        } else {
            shopHolo.put(p, new ArrayList<>());
            shopHolo.get(p).add(holo);
        }
    }

    public static List<ShopHolo> getShopHolograms(Player p) {
        if (!shopHolo.containsKey(p)) shopHolo.put(p, new ArrayList<>());
        return shopHolo.get(p);
    }
}
