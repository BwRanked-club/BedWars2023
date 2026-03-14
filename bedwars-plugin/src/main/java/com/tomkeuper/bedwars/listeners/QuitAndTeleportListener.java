package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.LastHit;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import com.tomkeuper.bedwars.commands.stats.StatsCommandSupport;
import com.tomkeuper.bedwars.sidebar.BoardManager;
import com.tomkeuper.bedwars.stats.AssistTracker;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.tomkeuper.bedwars.BedWars.*;

@Slf4j
public class QuitAndTeleportListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        final IArena arena = Arena.getArenaByPlayer(p);
        if (arena != null) {
            if (arena.isPlayer(p)) {
                arena.removePlayer(p, true);
            } else if (arena.isSpectator(p)) {
                arena.removeSpectator(p, true);
            }
        }

        if (Language.getLangByPlayer().containsKey(uuid)) {
            final var lang = Language.getLangByPlayer().remove(uuid);
            if (lang != null) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    String iso = lang.getIso();
                    if (Language.isLanguageExist(iso)) {
                        if (BedWars.config.getYml()
                                .getStringList(ConfigPath.GENERAL_CONFIGURATION_DISABLED_LANGUAGES)
                                .contains(iso)) {
                            iso = Language.getDefaultLanguage().getIso();
                        }
                        BedWars.getRemoteDatabase().setLanguage(uuid, iso);
                    }
                });
            }
        }

        if (getServerType() != ServerType.SHARED) {
            e.setQuitMessage(null);
        }

        if (getPartyManager().isInternal() && getPartyManager().hasParty(p) && getPartyManager().isOwner(p)) {
            final var members = new ArrayList<>(getPartyManager().getMembers(p)); // snapshot
            for (final Player member : members) {
                if (arena == null) continue;
                if (arena.isPlayer(member)) {
                    arena.removePlayer(member, false, true);
                } else if (arena.isSpectator(member)) {
                    arena.removeSpectator(member, false, true);
                } else {
                    BedWars.debug("Cannot remove " + member.getName() + " from " + arena.getDisplayName()
                            + " because member is not a player nor a spectator.");
                }
            }
        }

        final SetupSession ss = SetupSession.getSession(uuid);
        if (ss != null) ss.cancel();

        final BoardManager bm = BoardManager.getInstance();
        if (bm != null) {
            bm.remove(p);
        }

        BedWarsTeam.reSpawnInvulnerability.remove(uuid);

        Optional.ofNullable(LastHit.getLastHit(p)).ifPresent(LastHit::remove);
        AssistTracker.clearPlayer(uuid);
        StatsCommandSupport.clearCooldown(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e) {
        final var to = e.getTo();
        if (to == null || to.getWorld() == null) return;

        final Player player = e.getPlayer();
        final IArena currentArena = Arena.getArenaByPlayer(player);
        if (currentArena == null) return;

        final IArena targetArena = Arena.getArenaByIdentifier(to.getWorld().getName());
        if (targetArena != null && !targetArena.equals(currentArena)) {
            if (currentArena.isSpectator(player)) currentArena.removeSpectator(player, false);
            if (currentArena.isPlayer(player)) currentArena.removePlayer(player, false);
            player.sendMessage("PlayerTeleportEvent: algo saiu do esperado. Você entrou em um mundo de arena diferente enquanto jogava.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(final PlayerChangedWorldEvent e) {
        final Player player = e.getPlayer();
        final String lobbyWorld = BedWars.getLobbyWorld();
        final ServerType serverType = BedWars.getServerType();

        if (BedWars.config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR)) {
            final BoardManager bm = BoardManager.getInstance();
            if (bm != null) {
                final boolean isInLobbyWorld =
                        player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNullElse(lobbyWorld, ""));
                switch (serverType) {
                    case SHARED -> {
                        if (isInLobbyWorld) {
                            bm.giveTabFeatures(player, null, true);
                        } else {
                            bm.remove(player);
                        }
                    }
                    case MULTIARENA -> {
                        if (isInLobbyWorld) {
                            bm.giveTabFeatures(player, null, true);
                        }
                    }
                    default -> {
                    }
                }
            }
        }

        final IArena arena = Arena.getArenaByPlayer(player);
        if (arena != null && arena.isPlayer(player)) {
            if (arena.getStatus() != GameState.waiting && arena.getStatus() != GameState.starting) {
                final String arenaWorld = arena.getWorld().getName();
                if (!player.getWorld().getName().equalsIgnoreCase(arenaWorld)) {
                    final boolean bungee = (serverType == ServerType.BUNGEE);
                    arena.removePlayer(player, bungee);
                    debug(player.getName() + " foi removido de " + arena.getDisplayName()
                            + " por ter sido teletransportado para fora da arena.");
                }
            }
        }

        final IArena targetArena = Arena.getArenaByIdentifier(player.getWorld().getName());
        if (targetArena != null && Arena.getArenaByPlayer(player) == null) {
            if (getPartyManager().hasParty(player)) {
                final Player owner = getPartyManager().getOwner(player);
                if (owner != null) {
                    final IArena ownerArena = Arena.getArenaByPlayer(owner);
                    if (ownerArena != null && ownerArena.equals(targetArena)) {
                        if (ownerArena.isPlayer(owner)) {
                            ownerArena.addPlayer(player, true);
                        } else if (ownerArena.isSpectator(owner)) {
                            ownerArena.addSpectator(player, false, null);
                        }
                    }
                }
            }
        }
    }
}
