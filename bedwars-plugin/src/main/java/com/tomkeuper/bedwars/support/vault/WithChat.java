package com.tomkeuper.bedwars.support.vault;

import com.tomkeuper.bedwars.api.chat.IChat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WithChat implements IChat {

    private static net.milkbowl.vault.chat.Chat chat;

    public static void setChat(net.milkbowl.vault.chat.Chat chat) {
        WithChat.chat = chat;
    }

    @Override
    public String getPrefix(Player p) {
        return ChatColor.translateAlternateColorCodes('&', chat.getPlayerPrefix(p));
    }

    @Override
    public String getSuffix(Player p) {
        return ChatColor.translateAlternateColorCodes('&', chat.getPlayerSuffix(p));
    }
}
