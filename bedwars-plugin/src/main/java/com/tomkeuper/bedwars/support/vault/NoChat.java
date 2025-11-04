package com.tomkeuper.bedwars.support.vault;

import com.tomkeuper.bedwars.api.chat.IChat;
import org.bukkit.entity.Player;

public class NoChat implements IChat {
    @Override
    public String getPrefix(Player p) {
        return "";
    }

    @Override
    public String getSuffix(Player p) {
        return "";
    }
}
