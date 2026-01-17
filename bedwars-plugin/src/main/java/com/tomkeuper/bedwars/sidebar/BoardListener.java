package com.tomkeuper.bedwars.sidebar;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.arena.Arena;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class BoardListener implements Listener {
    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent event) {
        IArena arena = Arena.getArenaByPlayer(event.getPlayer());
        if (TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId()) == null) return;
        if (TabAPI.getInstance().getBossBarManager() != null && arena != null) {
            for (BossBar bossBar : arena.getDragonBossbars()) {
                bossBar.removePlayer(Objects.requireNonNull(TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId())));
            }
        }

        // Force update the prefix and suffix
        PlayerPlaceholder prefixPlaceholderTab = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_prefix_tab%");
        PlayerPlaceholder suffixPlaceholderTab = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_suffix_tab%");
        PlayerPlaceholder prefixPlaceholderHead = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_prefix_head%");
        PlayerPlaceholder suffixPlaceholderHead = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_suffix_head%");
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId());

        assert tabPlayer != null;
        prefixPlaceholderTab.updateValue(tabPlayer, BoardManager.getInstance().getPrefixTab(tabPlayer));
        suffixPlaceholderTab.updateValue(tabPlayer, BoardManager.getInstance().getSuffixTab(tabPlayer));
        prefixPlaceholderHead.updateValue(tabPlayer, BoardManager.getInstance().getPrefixHead(tabPlayer));
        suffixPlaceholderHead.updateValue(tabPlayer, BoardManager.getInstance().getSuffixHead(tabPlayer));
    }

    @EventHandler
    public void onDisconnect(PlayerLeaveArenaEvent event) {
        BoardManager.getInstance().cleanupPlayer(event.getPlayer());
    }
}
