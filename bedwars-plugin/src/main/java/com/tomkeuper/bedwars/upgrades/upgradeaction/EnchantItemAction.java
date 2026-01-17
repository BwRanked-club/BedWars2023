package com.tomkeuper.bedwars.upgrades.upgradeaction;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.upgrades.UpgradeAction;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class EnchantItemAction implements UpgradeAction {

    private final Enchantment enchantment;
    private final int amplifier;
    private final ApplyType type;

    public EnchantItemAction(Enchantment enchantment, int amplifier, ApplyType type) {
        this.enchantment = enchantment;
        this.amplifier = amplifier;
        this.type = type;
    }

    @Override
    public void onBuy(Player player, ITeam bwt) {
        if (type == ApplyType.ARMOR) {
            bwt.addArmorEnchantment(enchantment, amplifier);
        } else if (type == ApplyType.SWORD) {
            bwt.addSwordEnchantment(enchantment, amplifier);
        } else if (type == ApplyType.BOW) {
            bwt.addBowEnchantment(enchantment, amplifier);
        }
    }

    public enum ApplyType {
        SWORD,
        ARMOR,
        BOW,
    }
}
