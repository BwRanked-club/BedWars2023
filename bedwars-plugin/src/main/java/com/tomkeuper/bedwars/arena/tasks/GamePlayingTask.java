package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.player.PlayerInvisibilityPotionEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.tasks.PlayingTask;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

import static com.tomkeuper.bedwars.BedWars.config;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class GamePlayingTask implements Runnable, PlayingTask {

    private final Arena arena;
    private final BukkitTask task;
    private final OreGenTask oreGenTask;
    private int beds_destroy_countdown, dragon_spawn_countdown, game_end_countdown;

    public GamePlayingTask(Arena arena) {
        this.arena = arena;
        this.beds_destroy_countdown = config.getInt(ConfigPath.GENERAL_CONFIGURATION_BEDS_DESTROY_COUNTDOWN);
        this.dragon_spawn_countdown = config.getInt(ConfigPath.GENERAL_CONFIGURATION_DRAGON_SPAWN_COUNTDOWN);
        this.game_end_countdown = config.getInt(ConfigPath.GENERAL_CONFIGURATION_GAME_END_COUNTDOWN);
        this.task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L);
        this.oreGenTask = new OreGenTask(arena);
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public BukkitTask getBukkitTask() {
        return task;
    }

    /**
     * Get task ID
     */
    public int getTask() {
        return task.getTaskId();
    }

    public int getBedsDestroyCountdown() {
        return beds_destroy_countdown;
    }

    @Override
    public void setBedsDestroyCountdown(int seconds) {
        this.beds_destroy_countdown = Math.max(0, seconds);
    }

    public int getDragonSpawnCountdown() {
        return dragon_spawn_countdown;
    }

    @Override
    public void setDragonSpawnCountdown(int seconds) {
        this.dragon_spawn_countdown = Math.max(0, seconds);
    }

    public int getGameEndCountdown() {
        return game_end_countdown;
    }

    @Override
    public void setGameEndCountdown(int seconds) {
        this.game_end_countdown = Math.max(0, seconds);
    }

    @Override
    public void run() {
        switch (getArena().getNextEvent()) {
            case EMERALD_GENERATOR_TIER_II:
            case EMERALD_GENERATOR_TIER_III:
            case DIAMOND_GENERATOR_TIER_II:
            case DIAMOND_GENERATOR_TIER_III:
                if (getArena().upgradeDiamondsCount > 0) {
                    getArena().upgradeDiamondsCount--;
                    if (getArena().upgradeDiamondsCount == 0) {
                        getArena().updateNextEvent();
                    }
                }
                if (getArena().upgradeEmeraldsCount > 0) {
                    getArena().upgradeEmeraldsCount--;
                    if (getArena().upgradeEmeraldsCount == 0) {
                        getArena().updateNextEvent();
                    }
                }
                break;
            case BEDS_DESTROY:
                beds_destroy_countdown--;
                if (getBedsDestroyCountdown() == 0) {
                    for (Player p : getArena().getPlayers()) {
                        BedWars.nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED), 0, 40, 10);
                        p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED));
                    }
                    for (Player p : getArena().getSpectators()) {
                        BedWars.nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED), 0, 40, 10);
                        p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED));
                    }
                    for (ITeam t : getArena().getTeams()) {
                        t.setBedDestroyed(true);
                    }
                    getArena().updateNextEvent();
                }
                break;
            case ENDER_DRAGON:
                dragon_spawn_countdown--;
                if (getDragonSpawnCountdown() == 0) {
                    for (Player p : getArena().getPlayers()) {
                        BedWars.nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH), 0, 40, 10);
                        for (ITeam t : getArena().getTeams()) {
                            if (t.getMembers().isEmpty()) continue;
                            p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH).replace("%bw_dragons_amount%", String.valueOf(t.getDragonAmount()))
                                    .replace("%bw_team_color%", t.getColor().chat().toString()).replace("%bw_team_name%", t.getDisplayName(Language.getPlayerLanguage(p))));
                        }
                    }
                    for (Player p : getArena().getSpectators()) {
                        BedWars.nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH), 0, 40, 10);
                        for (ITeam t : getArena().getTeams()) {
                            if (t.getMembers().isEmpty()) continue;
                            p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH).replace("%bw_dragons_amount%", String.valueOf(t.getDragonAmount()))
                                    .replace("%bw_team_color%", t.getColor().chat().toString()).replace("%bw_team_name%", t.getDisplayName(Language.getPlayerLanguage(p))));
                        }
                    }
                    getArena().updateNextEvent();
                    for (ITeam t : getArena().getTeams()) {
                        if (t.getMembers().isEmpty()) continue;
                        for (int x = 0; x < t.getDragonAmount(); x++) {
                            EnderDragon dragon = BedWars.nms.spawnDragon(getArena().getConfig().getArenaLoc("waiting.Loc").add(0, 10, 0), t);
                            if (dragon != null) {
                                t.addDragon(dragon);
                                if (BedWars.nms.getVersion() == 0) {
                                    arena.set1_8BossBarName(t, dragon);
                                } else {
                                    arena.createTABTeamDragonBossBar(t, x);
                                }
                            }
                        }
                    }
                }
                break;
            case GAME_END:
                game_end_countdown--;
                if (getGameEndCountdown() == 0) {
                    getArena().checkWinner();
                    getArena().changeStatus(GameState.restarting);
                }
                break;
        }
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_TEAMMATE_TRACKING_ACTION_BAR)) {
            int distance = 0;
            for (ITeam t : getArena().getTeams()) {
                if (t.getSize() > 1) {
                    for (Player p : t.getMembers()) {
                        for (Player p2 : t.getMembers()) {
                            if (p2 == p || !p.getLocation().getWorld().equals(p2.getLocation().getWorld())) continue;
                            if (distance == 0) {
                                distance = (int) p.getLocation().distance(p2.getLocation());
                            } else if ((int) p.getLocation().distance(p2.getLocation()) < distance) {
                                distance = (int) p.getLocation().distance(p2.getLocation());
                            }
                        }
                        BedWars.nms.playAction(p, getMsg(p, Messages.FORMATTING_ACTION_BAR_TRACKING).replace("%bw_team%", t.getColor().chat() + t.getDisplayName(Language.getPlayerLanguage(p)))
                                .replace("%bw_distance%", t.getColor().chat().toString() + distance).replace("&", "§"));
                    }
                }
            }
        }

        /* AFK SYSTEM FOR PLAYERS */
        int current = 0;
        for (Player p : getArena().getPlayers()) {
            if (Arena.afkCheck.get(p.getUniqueId()) == null) {
                Arena.afkCheck.put(p.getUniqueId(), current);
            } else {
                current = Arena.afkCheck.get(p.getUniqueId());
                current++;
                Arena.afkCheck.replace(p.getUniqueId(), current);
                if (current == 45) {
                    BedWars.getAPI().getAFKUtil().setPlayerAFK(p, true);
                }
            }
        }

        /* RESPAWN SESSION */
        if (!getArena().getRespawnSessions().isEmpty()) {
            for (Map.Entry<Player, Integer> e : getArena().getRespawnSessions().entrySet()) {
                if (e.getValue() <= 0) {
                    IArena a = Arena.getArenaByPlayer(e.getKey());
                    if (a == null) {
                        getArena().getRespawnSessions().remove(e.getKey());
                        continue;
                    }
                    ITeam t = a.getTeam(e.getKey());
                    if (t == null) {
                        a.addSpectator(e.getKey(), true, null);
                    } else {
                        t.respawnMember(e.getKey());
                        e.getKey().setAllowFlight(false);
                        e.getKey().setFlying(false);
                    }
                } else {
                    BedWars.nms.sendTitle(e.getKey(), getMsg(e.getKey(), Messages.PLAYER_DIE_RESPAWN_TITLE).replace("%bw_time%",
                            String.valueOf(e.getValue())), getMsg(e.getKey(), Messages.PLAYER_DIE_RESPAWN_SUBTITLE).replace("%bw_time%",
                            String.valueOf(e.getValue())), 0, 30, 10);
                    e.getKey().sendMessage(getMsg(e.getKey(), Messages.PLAYER_DIE_RESPAWN_CHAT).replace("%bw_time%", String.valueOf(e.getValue())));
                    getArena().getRespawnSessions().replace(e.getKey(), e.getValue() - 1);
                }
            }
        }

        /* INVISIBILITY FOR ARMOR */
        if (!getArena().getShowTime().isEmpty()) {
            for (Map.Entry<Player, Integer> e : getArena().getShowTime().entrySet()) {
                if (e.getValue() <= 0) {
                    for (Player p : e.getKey().getWorld().getPlayers()) {
                        BedWars.nms.showArmor(e.getKey(), p);
                        //nms.showPlayer(e.getKey(), p);
                    }
                    e.getKey().removePotionEffect(PotionEffectType.INVISIBILITY);
                    getArena().getShowTime().remove(e.getKey());
                    Bukkit.getPluginManager().callEvent(new PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.REMOVED, getArena().getTeam(e.getKey()), e.getKey(), getArena()));
                } else {
                    getArena().getShowTime().replace(e.getKey(), e.getValue() - 1);
                }
            }
        }
    }

    public void setCountdownForEvent(Object nextEventEnum, int seconds) {
        String name = String.valueOf(nextEventEnum);
        if ("BEDS_DESTROY".equals(name)) setBedsDestroyCountdown(seconds);
        if ("ENDER_DRAGON".equals(name)) setDragonSpawnCountdown(seconds);
        if ("GAME_END".equals(name)) setGameEndCountdown(seconds);
    }

    public void cancel() {
        task.cancel();
        oreGenTask.cancel();
    }
}