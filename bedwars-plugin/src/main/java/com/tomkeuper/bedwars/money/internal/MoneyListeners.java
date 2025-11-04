package com.tomkeuper.bedwars.money.internal;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerMoneyGainEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.configuration.MoneyConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MoneyListeners implements Listener {

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        final IArena arena = e.getArena();
        if (arena == null) return;

        final int gameWin = MoneyConfig.money.getInt("money-rewards.game-win");
        final int perTeammate = MoneyConfig.money.getInt("money-rewards.per-teammate");
        final boolean teamMode = arena.getMaxInTeam() > 1;

        for (UUID uuid : e.getWinners()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if (gameWin > 0) {
                grant(player, gameWin, PlayerMoneyGainEvent.MoneySource.GAME_WIN, Messages.MONEY_REWARD_WIN);
            }

            if (teamMode) {
                final ITeam exTeam = arena.getExTeam(player.getUniqueId());
                if (exTeam != null && perTeammate > 0) {
                    grant(player, perTeammate, PlayerMoneyGainEvent.MoneySource.PER_TEAMMATE, Messages.MONEY_REWARD_PER_TEAMMATE);
                }
            }
        }

        if (teamMode && perTeammate > 0) {
            for (UUID uuid : e.getLosers()) {
                final Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                final ITeam exTeam = arena.getExTeam(player.getUniqueId());
                if (exTeam != null) {
                    grant(player, perTeammate, PlayerMoneyGainEvent.MoneySource.PER_TEAMMATE, Messages.MONEY_REWARD_PER_TEAMMATE);
                }
            }
        }
    }

    @EventHandler
    public void onBreakBed(PlayerBedBreakEvent e) {
        final Player player = e.getPlayer();
        if (player == null) return;

        final int bedDestroy = MoneyConfig.money.getInt("money-rewards.bed-destroyed");
        if (bedDestroy <= 0) return;

        grant(player, bedDestroy, PlayerMoneyGainEvent.MoneySource.BED_DESTROYED, Messages.MONEY_REWARD_BED_DESTROYED);
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        final Player killer = e.getKiller();
        final Player victim = e.getVictim();

        if (killer == null || victim == null) return;
        if (killer.equals(victim)) return;

        final boolean isFinal = e.getCause().isFinalKill();
        final int finalKill = MoneyConfig.money.getInt("money-rewards.final-kill");
        final int regularKill = MoneyConfig.money.getInt("money-rewards.regular-kill");

        if (isFinal) {
            if (finalKill > 0) {
                grant(killer, finalKill, PlayerMoneyGainEvent.MoneySource.FINAL_KILL, Messages.MONEY_REWARD_FINAL_KILL);
            }
        } else {
            if (regularKill > 0) {
                grant(killer, regularKill, PlayerMoneyGainEvent.MoneySource.REGULAR_KILL, Messages.MONEY_REWARD_REGULAR_KILL);
            }
        }
    }

    private static void grant(Player player, int amount, PlayerMoneyGainEvent.MoneySource source, String messageKey) {
        if (amount <= 0) return;

        var event = new PlayerMoneyGainEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (BedWars.getEconomy() != null) {
            BedWars.getEconomy().giveMoney(player, event.getAmount());
        }

        String msg = Language.getMsg(player, messageKey).replace("%bw_money%", String.valueOf(event.getAmount()));
        player.sendMessage(msg);
    }
}
