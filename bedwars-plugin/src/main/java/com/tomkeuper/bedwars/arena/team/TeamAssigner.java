package com.tomkeuper.bedwars.arena.team;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.ITeamAssigner;
import com.tomkeuper.bedwars.api.events.gameplay.TeamAssignEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TeamAssigner implements ITeamAssigner {

    private final LinkedList<Player> skip = new LinkedList<>();

    @Override
    public void assignTeams(IArena arena) {
        if (arena == null || arena.getTeams() == null || arena.getTeams().isEmpty()) return;

        skip.clear();

        final int maxInTeam = Math.max(1, arena.getMaxInTeam());
        final List<ITeam> teams = new ArrayList<>(arena.getTeams());

        if (arena.getPlayers().size() > maxInTeam && maxInTeam > 1) {
            List<List<Player>> parties = arena.getPlayers().stream()
                    .filter(BedWars.getPartyManager()::isOwner)
                    .map(owner -> buildPartySnapshot(arena, owner))
                    .filter(list -> list.size() > 1)
                    .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                    .toList();

            for (List<Player> party : parties) {
                ITeam bestSingle = chooseTeamWithCapacity(teams, maxInTeam, party.size());
                if (bestSingle != null) {
                    addPlayersToTeamRespectingEvent(arena, bestSingle, party);
                    continue;
                }

                LinkedList<Player> remaining = new LinkedList<>(party);
                while (!remaining.isEmpty()) {
                    ITeam target = chooseMostAvailableTeam(teams, maxInTeam);
                    if (target == null) break;

                    int free = maxInTeam - target.getSize();
                    if (free <= 0) {
                        if (teams.stream().noneMatch(t -> t.getSize() < maxInTeam)) break;
                        continue;
                    }
                    List<Player> block = new ArrayList<>();
                    for (int i = 0; i < free && !remaining.isEmpty(); i++) {
                        block.add(remaining.poll());
                    }
                    addPlayersToTeamRespectingEvent(arena, target, block);
                }
            }
        }

        List<Player> remainingPlayers = arena.getPlayers().stream()
                .filter(p -> !skip.contains(p))
                .toList();

        if (!remainingPlayers.isEmpty()) {
            BedWars.debug("Assigning teams for arena: " + arena.getArenaName() +
                    " | maxInTeam: " + maxInTeam +
                    " | players: " + arena.getPlayers().size() +
                    " | teams: " + teams.size());

            for (Player player : remainingPlayers) {
                player.closeInventory();

                ITeam pairFirst = pickTeamToPairIfMakesSense(teams, maxInTeam, arena.getPlayers().size());
                ITeam target = pairFirst != null ? pairFirst : chooseMostAvailableTeam(teams, maxInTeam);

                if (target != null) {
                    fireEventAndAdd(arena, target, player);
                } else {
                    BedWars.debug("Nenhum time disponível para " + player.getName());
                }
            }
        }
    }

    private ITeam chooseTeamWithCapacity(List<ITeam> teams, int maxInTeam, int partySize) {
        ITeam best = null;
        int bestSize = Integer.MAX_VALUE;
        for (ITeam t : teams) {
            int free = maxInTeam - t.getSize();
            if (free >= partySize) {
                if (t.getSize() < bestSize) {
                    best = t;
                    bestSize = t.getSize();
                }
            }
        }
        return best;
    }

    private List<Player> buildPartySnapshot(IArena arena, Player owner) {
        if (arena == null || owner == null || !arena.isPlayer(owner)) return Collections.emptyList();

        List<Player> snapshot = new ArrayList<>();
        snapshot.add(owner);

        List<Player> members = BedWars.getPartyManager().getMembers(owner);
        if (members == null) return snapshot;

        for (Player member : members) {
            if (member == null || !arena.isPlayer(member)) continue;
            boolean alreadyAdded = snapshot.stream()
                    .anyMatch(existing -> existing.getUniqueId().equals(member.getUniqueId()));
            if (!alreadyAdded) {
                snapshot.add(member);
            }
        }
        return snapshot;
    }

    private ITeam chooseMostAvailableTeam(List<ITeam> teams, int maxInTeam) {
        ITeam best = null;
        int bestSize = Integer.MAX_VALUE;
        for (ITeam t : teams) {
            int size = t.getSize();
            if (size < maxInTeam && size < bestSize) {
                best = t;
                bestSize = size;
            }
        }
        return best;
    }

    private ITeam pickTeamToPairIfMakesSense(List<ITeam> teams, int maxInTeam, int totalPlayers) {
        if (maxInTeam <= 1 || totalPlayers <= 2) return null;
        for (ITeam t : teams) {
            if (t.getSize() == 1 && t.getSize() < maxInTeam) {
                return t;
            }
        }
        return null;
    }

    private void addPlayersToTeamRespectingEvent(IArena arena, ITeam team, List<Player> players) {
        for (Player p : players) {
            fireEventAndAdd(arena, team, p);
        }
    }

    private void fireEventAndAdd(IArena arena, ITeam team, Player toAdd) {
        if (team.getSize() >= Math.max(1, arena.getMaxInTeam())) return;
        TeamAssignEvent e = new TeamAssignEvent(toAdd, team, arena);
        Bukkit.getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            toAdd.closeInventory();
            team.addPlayers(toAdd);
            skip.add(toAdd);
        }
    }
}
