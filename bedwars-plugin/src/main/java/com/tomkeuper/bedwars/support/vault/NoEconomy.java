package com.tomkeuper.bedwars.support.vault;

import com.tomkeuper.bedwars.api.economy.IEconomy;
import org.bukkit.entity.Player;

public class NoEconomy implements IEconomy {
    @Override
    public boolean isEconomy() {
        return false;
    }

    @Override
    public double getMoney(Player p) {
        return 0;
    }

    @Override
    public void giveMoney(Player p, double money) {
    }

    @Override
    public void buyAction(Player p, double cost) {
    }
}
