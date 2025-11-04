package com.tomkeuper.bedwars.upgrades.trapaction;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.upgrades.TrapAction;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class RemoveEffectAction implements TrapAction {

    private final PotionEffectType potionEffectType;

    public RemoveEffectAction(PotionEffectType potionEffectType) {
        this.potionEffectType = potionEffectType;
    }

    @Override
    public String getName() {
        return "remove-effect";
    }

    @Override
    public void onTrigger(@NotNull Player player, ITeam playerTeam, ITeam targetTeam) {
        player.removePotionEffect(potionEffectType);
        if (potionEffectType == PotionEffectType.INVISIBILITY) {
            IArena a = Arena.getArenaByPlayer(player);

            if (a != null && a.getShowTime().containsKey(player)) {
                // Remove the player from the showTime map
                a.getShowTime().remove(player);

                // Reveal the player's armor to all relevant players
                for (Player p : a.getPlayers()) {
                    // Only reveal armor to players not on the same team
                    if (!playerTeam.equals(a.getTeam(p))) {
                        BedWars.nms.showArmor(player, p);
                    }
                }

                // Ensure spectators can see the player's armor
                for (Player p : a.getSpectators()) {
                    BedWars.nms.showArmor(player, p);
                }
            }
        }
    }
}
