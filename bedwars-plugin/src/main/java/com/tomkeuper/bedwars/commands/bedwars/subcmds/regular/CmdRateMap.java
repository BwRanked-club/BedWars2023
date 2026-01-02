package com.tomkeuper.bedwars.commands.bedwars.subcmds.regular;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.ratings.MapRatingService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class CmdRateMap extends SubCommand {

    public CmdRateMap(ParentCommand parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof Player player)) return false;

        if (args.length < 2) {
            player.sendMessage(getMsg(player, Messages.GAME_END_MAP_RATING_INVALID));
            return true;
        }

        int rating;
        try {
            rating = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage(getMsg(player, Messages.GAME_END_MAP_RATING_INVALID));
            return true;
        }

        if (rating < 1 || rating > 5) {
            player.sendMessage(getMsg(player, Messages.GAME_END_MAP_RATING_INVALID));
            return true;
        }

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.getArenaName().equalsIgnoreCase(args[0]) || arena.getStatus() != GameState.restarting) {
            player.sendMessage(getMsg(player, Messages.GAME_END_MAP_RATING_NOT_AVAILABLE));
            return true;
        }

        MapRatingService.saveRating(player.getUniqueId(), arena, rating);

        String success = getMsg(player, Messages.GAME_END_MAP_RATING_SUCCESS)
                .replace("%bw_map%", arena.getDisplayName())
                .replace("%bw_arena%", arena.getArenaName())
                .replace("%bw_group%", arena.getGroup())
                .replace("%bw_rating%", String.valueOf(rating));
        player.sendMessage(success);
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return Collections.emptyList();
    }
}
