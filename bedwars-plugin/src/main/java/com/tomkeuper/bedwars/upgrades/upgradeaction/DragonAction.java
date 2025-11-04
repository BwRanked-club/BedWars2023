package com.tomkeuper.bedwars.upgrades.upgradeaction;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.upgrades.UpgradeAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DragonAction implements UpgradeAction {

    private final int amount;

    public DragonAction(int amount){
        this.amount = amount;
    }

    @Override
    public void onBuy(@Nullable Player player, ITeam bwt) {
        bwt.setDragonAmount(amount);
    }
}
