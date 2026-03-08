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
                    if (holoLoc == null) continue;
                    double distance = pLoc.distance(holoLoc);
                    if (distance > BedWars.hologramUpdateDistance) continue;
                    shopHolo.update(p);
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
                    double distance = pLoc.distance(bedLoc);
                    if (distance > BedWars.hologramUpdateDistance) continue;
                    bedHolo.update(p);
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
                    double distance = pLoc.distance(genLoc);
                    if (distance > BedWars.hologramUpdateDistance) continue;

                    holo.update(p);
                    if (holder != null) holder.update(p);
                }
            }
        }
    }
}
