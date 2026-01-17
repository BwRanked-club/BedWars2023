package com.tomkeuper.bedwars.support.vault;

import com.tomkeuper.bedwars.api.economy.IEconomy;
import org.bukkit.entity.Player;

public class WithEconomy implements IEconomy {

    private static net.milkbowl.vault.economy.Economy economy;

    @Override
    public boolean isEconomy() {
        return true;
    }

    public static void setEconomy(net.milkbowl.vault.economy.Economy economy) {
        WithEconomy.economy = economy;
    }

    @Override
    public double getMoney(Player p) {
        return economy.getBalance(p);
    }

    @Override
    public void giveMoney(Player p, double money) {
        economy.depositPlayer(p, money);
    }

    @Override
    public void buyAction(Player p, double cost) {
        economy.bankWithdraw(p.getName(), cost);
    }
}
