package com.tomkeuper.bedwars.money.internal;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.events.player.PlayerMoneyGainEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.configuration.MoneyConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Periodic "per-minute" reward for players inside the arena.
 * Executa estritamente no thread principal (Bukkit) — rápido e seguro.
 */
public class MoneyPerMinuteTask {

    private final int money = MoneyConfig.money.getInt("money-rewards.per-minute");
    private BukkitTask task;

    public MoneyPerMinuteTask(Arena arena) {
        if (money < 1) return;

        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            if (arena == null) {
                cancel();
                return;
            }

            final List<Player> snapshot = new ArrayList<>(arena.getPlayers());
            if (snapshot.isEmpty()) return;

            for (Player p : snapshot) {
                if (p == null || !p.isOnline()) continue;

                var event = new PlayerMoneyGainEvent(p, money, PlayerMoneyGainEvent.MoneySource.PER_MINUTE);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) continue;

                if (BedWars.getEconomy() != null) {
                    BedWars.getEconomy().giveMoney(p, event.getAmount());
                }

                String msg = Language.getMsg(p, Messages.MONEY_REWARD_PER_MINUTE)
                        .replace("%bw_money%", String.valueOf(event.getAmount()));
                p.sendMessage(msg);
            }
        }, 60L * 20L, 60L * 20L);
    }

    public void cancel() {
        if (task != null) {
            try {
                task.cancel();
            } finally {
                task = null;
            }
        }
    }
}
