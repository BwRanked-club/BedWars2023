package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.GeneratorType;
import com.tomkeuper.bedwars.api.arena.generator.IGenHolo;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.shop.ShopHolo;
import com.tomkeuper.bedwars.api.arena.team.IBedHolo;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.entity.GeneratorHolder;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class HologramTask implements Runnable {

    @Override
    public void run() {
        for (IArena a : Arena.getArenas()) {
            if (a == null) continue;
            World world = a.getWorld();
            if (world == null) continue;
            if (a.getStatus() != GameState.playing) continue;

            if (world.getPlayers().isEmpty()) continue;

            for (Player p : world.getPlayers()) {
                String iso = Language.getPlayerLanguage(p).getIso();
                Location pLoc = p.getLocation();
                List<ShopHolo> shopHolos = a.getShopHolograms(iso);
                if (shopHolos == null || shopHolos.isEmpty()) continue;
                for (ShopHolo shopHolo : shopHolos) {
                    if (shopHolo == null) continue;
                    IHologram hologram = shopHolo.getHologram();
                    if (hologram == null) continue;
                    Location holoLoc = hologram.getLocation();
                    if (!isWithinRange(pLoc, holoLoc)) continue;
                    try {
                        shopHolo.update(p);
                    } catch (Throwable t) {
                        logHologramFailure("shop", a, p, t);
                    }
                }
            }

            List<ITeam> teams = a.getTeams();
            if (teams == null) continue;
            for (ITeam team : teams) {
                if (!(team instanceof BedWarsTeam)) continue;
                for (Player p : world.getPlayers()) {
                    String iso = Language.getPlayerLanguage(p).getIso();
                    IBedHolo bedHolo = ((BedWarsTeam) team).getBedHologram(iso);
                    if (bedHolo == null || bedHolo.getHologram() == null) continue;
                    Location bedLoc = bedHolo.getHologram().getLocation();

                    Location pLoc = p.getLocation();
                    if (!isWithinRange(pLoc, bedLoc)) continue;
                    try {
                        bedHolo.update(p);
                    } catch (Throwable t) {
                        logHologramFailure("bed", a, p, t);
                    }
                }
            }

            List<IGenerator> generators = a.getOreGenerators();
            if (generators == null) continue;
            for (IGenerator generator : generators) {
                if (generator == null) continue;
                GeneratorType type = generator.getType();
                if (type != GeneratorType.EMERALD && type != GeneratorType.DIAMOND) continue;
                Location genLoc = generator.getLocation();
                if (genLoc == null) continue;
                Map<String, IGenHolo> languageHolograms = generator.getLanguageHolograms();
                if (languageHolograms == null || languageHolograms.isEmpty()) continue;
                for (Player p : world.getPlayers()) {
                    String iso = Language.getPlayerLanguage(p).getIso();
                    IGenHolo holo = languageHolograms.get(iso);
                    GeneratorHolder holder = generator.getHologramHolder();
                    if (holo == null) continue;
                    Location pLoc = p.getLocation();
                    if (!isWithinRange(pLoc, genLoc)) continue;

                    try {
                        holo.update(p);
                        if (holder != null) holder.update(p);
                    } catch (Throwable t) {
                        logHologramFailure("generator", a, p, t);
                    }
                }
            }
        }
    }

    private boolean isWithinRange(Location playerLocation, Location hologramLocation) {
        if (playerLocation == null || hologramLocation == null) {
            return false;
        }
        if (playerLocation.getWorld() == null || hologramLocation.getWorld() == null) {
            return false;
        }
        if (!playerLocation.getWorld().equals(hologramLocation.getWorld())) {
            return false;
        }

        double maxDistanceSquared = BedWars.hologramUpdateDistance * BedWars.hologramUpdateDistance;
        return playerLocation.distanceSquared(hologramLocation) <= maxDistanceSquared;
    }

    private void logHologramFailure(String type, IArena arena, Player player, Throwable throwable) {
        String arenaName = arena == null ? "unknown" : arena.getArenaName();
        String playerName = player == null ? "unknown" : player.getName();
        BedWars.plugin.getLogger().log(Level.SEVERE,
                "[HologramTask] Failed to update " + type + " hologram for player " + playerName + " in arena " + arenaName,
                throwable);
    }
}
