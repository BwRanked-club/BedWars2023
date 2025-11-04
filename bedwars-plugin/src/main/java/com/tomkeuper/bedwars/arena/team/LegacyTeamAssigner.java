package com.tomkeuper.bedwars.arena.team;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.TeamAssignEvent;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegacyTeamAssigner {

    private LegacyTeamAssigner(){}

    public static void assignTeams(IArena arena){
        //Check who is having parties
        List<Player> skip = new ArrayList<>(), owners = new ArrayList<>();
        for (Player p : arena.getPlayers()) {
            if (BedWars.getPartyManager().hasParty(p) && BedWars.getPartyManager().isOwner(p)) {
                owners.add(p);
            }
        }

        //Mix teams order
        Collections.shuffle(arena.getTeams());

        //Team-up parties
        for (Player p : arena.getPlayers()) {
            if (owners.contains(p)) {
                for (ITeam t : arena.getTeams()) {
                    if (skip.contains(p)) continue;
                    if (t.getSize() + BedWars.getPartyManager().partySize(p) <= arena.getMaxInTeam()) {
                        skip.add(p);
                        p.closeInventory();
                        TeamAssignEvent e = new TeamAssignEvent(p, t, arena);
                        Bukkit.getPluginManager().callEvent(e);
                        if (!e.isCancelled()) {
                            t.addPlayers(p);
                        }
                        for (Player mem : BedWars.getPartyManager().getMembers(p)) {
                            if (mem != p) {
                                IArena ia = Arena.getArenaByPlayer(mem);
                                if (ia == null) {
                                    continue;
                                } else if (!ia.equals(arena)) {
                                    continue;
                                }
                                TeamAssignEvent ee = new TeamAssignEvent(p, t, arena);
                                Bukkit.getPluginManager().callEvent(ee);
                                if (!e.isCancelled()) {
                                    t.addPlayers(mem);
                                }
                                skip.add(mem);
                                mem.closeInventory();
                            }
                        }
                    }
                }
            }
        }

        //Give a team to players without a party
        for (Player p : arena.getPlayers()) {
            if (skip.contains(p)) continue;
            ITeam addhere = arena.getTeams().get(0);
            for (ITeam t : arena.getTeams()) {
                if (t.getMembers().size() < arena.getMaxInTeam() && t.getMembers().size() < addhere.getMembers().size()) {
                    addhere = t;
                }
            }
            TeamAssignEvent e = new TeamAssignEvent(p, addhere, arena);
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isCancelled()) {
                addhere.addPlayers(p);
            }
            p.closeInventory();
        }
    }
}
