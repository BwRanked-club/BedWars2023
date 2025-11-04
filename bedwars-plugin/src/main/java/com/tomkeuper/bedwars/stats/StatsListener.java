package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerStatChangeEvent;
import com.tomkeuper.bedwars.api.party.Party;
import com.tomkeuper.bedwars.api.stats.IPlayerStats;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.util.UUID;

public class StatsListener implements Listener {

    private boolean sameParty(Player a, Player b) {
        if (a == null || b == null) return false;
        Party util = BedWars.getAPI().getPartyUtil();
        if (util == null) return false;
        if (!util.hasParty(a) || !util.hasParty(b)) return false;
        Player ownerA = util.getOwner(a);
        Player ownerB = util.getOwner(b);
        return ownerA != null && ownerA.equals(ownerB);
    }

    private boolean sharesPartyWithVictimTeam(Player attacker, IArena arena, ITeam victimTeam) {
        if (attacker == null || arena == null || victimTeam == null) return false;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (arena.isPlayer(p)) {
                ITeam t = arena.getTeam(p);
                if (t != null && t.equals(victimTeam) && sameParty(attacker, p)) return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        IPlayerStats stats = BedWars.getRemoteDatabase().fetchStats(event.getUniqueId());
        stats.setName(event.getName());
        BedWars.getStatsManager().put(event.getUniqueId(), stats);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        Player breaker = event.getPlayer();
        if (sharesPartyWithVictimTeam(breaker, event.getArena(), event.getVictimTeam())) return;

        IPlayerStats stats = BedWars.getStatsManager().get(breaker.getUniqueId());
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(breaker, event.getArena(), PlayerStatChangeEvent.StatType.BEDS_DESTROYED);
        Bukkit.getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            stats.setBedsDestroyed(stats.getBedsDestroyed() + 1);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        Player victim = event.getVictim();
        Player killer = event.getKiller();

        if (killer != null && sameParty(killer, victim)) return;

        IPlayerStats victimStats = BedWars.getStatsManager().get(victim.getUniqueId());
        IPlayerStats killerStats = (killer != null && !victim.equals(killer))
                ? BedWars.getStatsManager().getUnsafe(killer.getUniqueId())
                : null;

        PlayerStatChangeEvent evKill = new PlayerStatChangeEvent(killer, event.getArena(), PlayerStatChangeEvent.StatType.KILLS);
        PlayerStatChangeEvent evFinalKill = new PlayerStatChangeEvent(killer, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_KILLS);
        PlayerStatChangeEvent evFinalDeaths = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_DEATHS);
        PlayerStatChangeEvent evLosses = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.LOSSES);
        PlayerStatChangeEvent evGamesPlayed = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.GAMES_PLAYED);
        PlayerStatChangeEvent evDeaths = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.DEATHS);

        if (event.getCause().isFinalKill()) {
            Bukkit.getPluginManager().callEvent(evFinalDeaths);
            if (!evFinalDeaths.isCancelled()) victimStats.setFinalDeaths(victimStats.getFinalDeaths() + 1);

            Bukkit.getPluginManager().callEvent(evLosses);
            if (!evLosses.isCancelled()) victimStats.setLosses(victimStats.getLosses() + 1);

            Bukkit.getPluginManager().callEvent(evGamesPlayed);
            if (!evGamesPlayed.isCancelled()) victimStats.setGamesPlayed(victimStats.getGamesPlayed() + 1);

            Bukkit.getPluginManager().callEvent(evFinalKill);
            if (!evFinalKill.isCancelled() && killerStats != null)
                killerStats.setFinalKills(killerStats.getFinalKills() + 1);
        } else {
            Bukkit.getPluginManager().callEvent(evDeaths);
            if (!evDeaths.isCancelled()) victimStats.setDeaths(victimStats.getDeaths() + 1);

            Bukkit.getPluginManager().callEvent(evKill);
            if (!evKill.isCancelled() && killerStats != null) killerStats.setKills(killerStats.getKills() + 1);
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        for (UUID uuid : event.getWinners()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            IPlayerStats stats = BedWars.getStatsManager().get(uuid);

            PlayerStatChangeEvent evWins = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.WINS);
            PlayerStatChangeEvent evGamesPlayed = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.GAMES_PLAYED);

            Bukkit.getPluginManager().callEvent(evWins);
            if (!evWins.isCancelled()) stats.setWins(stats.getWins() + 1);

            IArena playerArena = Arena.getArenaByPlayer(player);
            if (playerArena != null && playerArena.equals(event.getArena())) {
                Bukkit.getPluginManager().callEvent(evGamesPlayed);
                if (!evGamesPlayed.isCancelled()) stats.setGamesPlayed(stats.getGamesPlayed() + 1);
            }
        }
    }

    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent event) {
        final Player player = event.getPlayer();
        ITeam team = event.getArena().getExTeam(player.getUniqueId());
        if (team == null) return;
        if (event.getArena().getStatus() == GameState.starting || event.getArena().getStatus() == GameState.waiting)
            return;

        IPlayerStats playerStats = BedWars.getStatsManager().get(player.getUniqueId());
        if (playerStats == null) return;

        PlayerStatChangeEvent evFirst = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.FIRST_PLAY);
        PlayerStatChangeEvent evLast = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.LAST_PLAY);
        PlayerStatChangeEvent evFinalDeaths = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_DEATHS);
        PlayerStatChangeEvent evLosses = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.LOSSES);

        Instant now = Instant.now();
        Bukkit.getPluginManager().callEvent(evLast);
        if (!evLast.isCancelled()) playerStats.setLastPlay(now);
        if (playerStats.getFirstPlay() == null) {
            Bukkit.getPluginManager().callEvent(evFirst);
            if (!evFirst.isCancelled()) playerStats.setFirstPlay(now);
        }

        if (event.getArena().getStatus() == GameState.playing) {
            if (team.isBedDestroyed()) {
                if (event.getArena().isPlayer(player)) {
                    Bukkit.getPluginManager().callEvent(evFinalDeaths);
                    if (!evFinalDeaths.isCancelled()) playerStats.setFinalDeaths(playerStats.getFinalDeaths() + 1);
                    Bukkit.getPluginManager().callEvent(evLosses);
                    if (!evLosses.isCancelled()) playerStats.setLosses(playerStats.getLosses() + 1);
                }

                Player damager = event.getLastDamager();
                if (damager != null && event.getArena().isPlayer(damager) && event.getArena().getTeam(damager) != null) {
                    if (!sameParty(damager, player)) {
                        PlayerStatChangeEvent evFinalKill = new PlayerStatChangeEvent(damager, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_KILLS);
                        Bukkit.getPluginManager().callEvent(evFinalKill);
                        if (!evFinalKill.isCancelled()) {
                            IPlayerStats damagerStats = BedWars.getStatsManager().get(damager.getUniqueId());
                            damagerStats.setFinalKills(damagerStats.getFinalKills() + 1);
                            event.getArena().addPlayerKill(damager, true, player);
                        }
                    }
                }
            } else {
                Player damager = event.getLastDamager();
                if (damager != null && event.getArena().isPlayer(damager) && event.getArena().getTeam(damager) != null) {
                    if (!sameParty(damager, player)) {
                        PlayerStatChangeEvent evDeaths = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.DEATHS);
                        PlayerStatChangeEvent evKills = new PlayerStatChangeEvent(damager, event.getArena(), PlayerStatChangeEvent.StatType.KILLS);

                        Bukkit.getPluginManager().callEvent(evDeaths);
                        if (!evDeaths.isCancelled()) BedWars.getStatsManager().get(player.getUniqueId())
                                .setDeaths(playerStats.getDeaths() + 1);
                        event.getArena().addPlayerDeath(player);

                        event.getArena().addPlayerKill(damager, false, player);
                        Bukkit.getPluginManager().callEvent(evKills);
                        if (!evKills.isCancelled()) {
                            IPlayerStats damagerStats = BedWars.getStatsManager().get(damager.getUniqueId());
                            damagerStats.setKills(damagerStats.getKills() + 1);
                        }
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> BedWars.getRemoteDatabase().saveStats(playerStats));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
    }
}
