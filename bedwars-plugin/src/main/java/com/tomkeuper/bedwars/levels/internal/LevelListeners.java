package com.tomkeuper.bedwars.levels.internal;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.*;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.configuration.LevelsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

public class LevelListeners implements Listener {

    public static LevelListeners instance;

    public LevelListeners() {
        instance = this;
    }

    //create new level data on player join
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final UUID u = e.getPlayer().getUniqueId();
        // create empty level first
        new PlayerLevel(u, 1, 0);
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            Object[] levelData = BedWars.getRemoteDatabase().getLevelData(u);
            PlayerLevel.getLevelByPlayer(u).lazyLoad((Integer) levelData[0], (Integer) levelData[1]);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        final UUID u = e.getPlayer().getUniqueId();
        PlayerLevel pl = PlayerLevel.getCachedLevelByPlayer(u);
        if (pl != null) {
            pl.destroy();
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        for (UUID p : e.getWinners()) {
            if (PlayerLevel.getLevelByPlayer(p) != null) {
                Player p1 = Bukkit.getPlayer(p);
                if (p1 == null) continue;
                int xpAmount = LevelsConfig.levels.getInt("xp-rewards.game-win");
                if (xpAmount > 0) {
                    int gained = PlayerLevel.getLevelByPlayer(p).addXp(xpAmount, PlayerXpGainEvent.XpSource.GAME_WIN);
                    if (gained > 0) {
                        p1.sendMessage(Language.getMsg(p1, Messages.XP_REWARD_WIN)
                                .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(p1));
                    }
                }
                ITeam bwt = e.getArena().getExTeam(p1.getUniqueId());
                if (bwt != null) {
                    if (bwt.getMembersCache().size() > 1) {
                        int xpAmountPerTmt = LevelsConfig.levels.getInt("xp-rewards.per-teammate");
                        if (xpAmountPerTmt > 0) {
                            int tr = xpAmountPerTmt * bwt.getMembersCache().size();
                            int gained = PlayerLevel.getLevelByPlayer(p).addXp(tr, PlayerXpGainEvent.XpSource.PER_TEAMMATE);
                            if (gained > 0) {
                                p1.sendMessage(Language.getMsg(p1, "xp-reward-per-teammate")
                                        .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(p1));
                            }
                        }
                    }
                }
            }
        }
        for (UUID p : e.getLosers()) {
            if (PlayerLevel.getLevelByPlayer(p) != null) {
                Player p1 = Bukkit.getPlayer(p);
                if (p1 == null) continue;
                ITeam bwt = e.getArena().getExTeam(p1.getUniqueId());
                if (bwt != null) {
                    if (bwt.getMembersCache().size() > 1) {
                        int xpAmountPerTmt = LevelsConfig.levels.getInt("xp-rewards.per-teammate");
                        if (xpAmountPerTmt > 0) {
                            int tr = LevelsConfig.levels.getInt("xp-rewards.per-teammate") * bwt.getMembersCache().size();
                            int gained = PlayerLevel.getLevelByPlayer(p).addXp(tr, PlayerXpGainEvent.XpSource.PER_TEAMMATE);
                            if (gained > 0) {
                                p1.sendMessage(Language.getMsg(p1, Messages.XP_REWARD_PER_TEAMMATE)
                                        .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(p1));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArenaLeave(PlayerLeaveArenaEvent e) {
        final UUID u = e.getPlayer().getUniqueId();
        PlayerLevel pl = PlayerLevel.getCachedLevelByPlayer(u);
        if (pl != null) pl.updateDatabase();
    }

    @EventHandler
    public void onBreakBed(PlayerBedBreakEvent e) {
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }
        int bedDestroy = LevelsConfig.levels.getInt("xp-rewards.bed-destroyed");
        if (bedDestroy > 0) {
            int gained = PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(bedDestroy, PlayerXpGainEvent.XpSource.BED_DESTROYED);
            if (gained > 0) {
                player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_BED_DESTROY)
                        .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(player));
            }
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        Player player = e.getKiller();
        Player victim = e.getVictim();
        if (player == null || victim.equals(player)) {
            return;
        }
        int finalKill = LevelsConfig.levels.getInt("xp-rewards.final-kill");
        int regularKill = LevelsConfig.levels.getInt("xp-rewards.regular-kill");
        if (e.getCause().isFinalKill()) {
            if (finalKill > 0) {
                int gained = PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(finalKill, PlayerXpGainEvent.XpSource.FINAL_KILL);
                if (gained > 0) {
                    player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_FINAL_KILL)
                            .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(player));
                }
            }
        } else {
            if (regularKill > 0) {
                int gained = PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(regularKill, PlayerXpGainEvent.XpSource.REGULAR_KILL);
                if (gained > 0) {
                    player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_REGULAR_KILL)
                            .replace("%bw_xp%", String.valueOf(gained)) + PlayerLevel.getXpMultiplierSuffix(player));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;

        String newLevel = PlayerLevel.getLevelByPlayer(player.getUniqueId()).getLevelName();
        if (newLevel == null) return;

        List<String> messages = Language.getList(player, Messages.PLAYER_LEVEL_UP);
        for (String message : messages) {
            player.sendMessage(message.replace("%bw_level%", newLevel));
        }
    }
}
