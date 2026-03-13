package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.PlayerReplacementResult;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class ReplacePlayer extends SubCommand {

    public ReplacePlayer(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(10);
        showInList(true);
        setPermission(Permissions.PERMISSION_REPLACE_PLAYER);
        setDisplayInfo(Misc.msgHoverClick(
                "&b▪ &f/" + getParent().getName() + " " + getSubCommandName() + " <arena> <old> <new> &b- &freplace a team slot",
                "&fReplace an active player with another player in a running arena.",
                "/" + getParent().getName() + " " + getSubCommandName() + " ",
                ClickEvent.Action.SUGGEST_COMMAND
        ));
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        if (args.length != 3) {
            send(sender, Messages.COMMAND_REPLACE_PLAYER_USAGE);
            return true;
        }

        IArena arena = Arena.getArenaByName(args[0]);
        if (arena == null) {
            send(sender, Messages.COMMAND_REPLACE_PLAYER_ARENA_NOT_FOUND);
            return true;
        }

        Player replacedPlayer = Bukkit.getPlayerExact(args[1]);
        if (replacedPlayer == null) {
            send(sender, Messages.COMMAND_REPLACE_PLAYER_OLD_NOT_FOUND);
            return true;
        }

        Player incomingPlayer = Bukkit.getPlayerExact(args[2]);
        if (incomingPlayer == null) {
            send(sender, Messages.COMMAND_REPLACE_PLAYER_NEW_NOT_FOUND);
            return true;
        }

        PlayerReplacementResult result = BedWars.getAPI().getArenaUtil().replacePlayer(arena, replacedPlayer, incomingPlayer);
        if (!result.isSuccess()) {
            send(sender, mapFailure(result.getStatus()));
            return true;
        }

        send(sender, Messages.COMMAND_REPLACE_PLAYER_SUCCESS,
                result.getReplacedPlayer(),
                result.getIncomingPlayer(),
                result.getTeam(),
                arena
        );
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> suggestions = new ArrayList<>();
        for (IArena arena : Arena.getArenas()) {
            suggestions.add(arena.getArenaName());
        }
        return suggestions;
    }

    @Override
    public boolean canSee(CommandSender sender, com.tomkeuper.bedwars.api.BedWars api) {
        if (!(sender instanceof Player player)) return true;
        if (Arena.isInArena(player)) return false;
        if (SetupSession.isInSetupSession(player.getUniqueId())) return false;
        return hasPermission(sender);
    }

    private String mapFailure(PlayerReplacementResult.Status status) {
        return switch (status) {
            case ARENA_NOT_FOUND -> Messages.COMMAND_REPLACE_PLAYER_ARENA_NOT_FOUND;
            case ARENA_NOT_PLAYING -> Messages.COMMAND_REPLACE_PLAYER_NOT_PLAYING;
            case SAME_PLAYER -> Messages.COMMAND_REPLACE_PLAYER_SAME_PLAYER;
            case REPLACED_PLAYER_NOT_ACTIVE, REPLACED_PLAYER_TEAM_NOT_FOUND -> Messages.COMMAND_REPLACE_PLAYER_OLD_NOT_ACTIVE;
            case NEW_PLAYER_OFFLINE -> Messages.COMMAND_REPLACE_PLAYER_NEW_NOT_FOUND;
            case NEW_PLAYER_ALREADY_PLAYING, NEW_PLAYER_BUSY -> Messages.COMMAND_REPLACE_PLAYER_NEW_BUSY;
            case TEAM_SLOT_UNAVAILABLE -> Messages.COMMAND_REPLACE_PLAYER_TEAM_FULL;
            default -> Messages.COMMAND_REPLACE_PLAYER_ERROR;
        };
    }

    private void send(CommandSender sender, String messagePath) {
        if (sender instanceof Player player) {
            player.sendMessage(getMsg(player, messagePath));
            return;
        }
        sender.sendMessage(Language.getDefaultLanguage().m(messagePath));
    }

    private void send(CommandSender sender, String messagePath, Player replacedPlayer, Player incomingPlayer, com.tomkeuper.bedwars.api.arena.team.ITeam team, IArena arena) {
        String message;
        if (sender instanceof Player player) {
            message = getMsg(player, messagePath);
        } else {
            message = Language.getDefaultLanguage().m(messagePath);
        }

        sender.sendMessage(message
                .replace("%bw_old_player%", replacedPlayer.getName())
                .replace("%bw_old_player_display%", Misc.getPlayerName(replacedPlayer))
                .replace("%bw_new_player%", incomingPlayer.getName())
                .replace("%bw_new_player_display%", Misc.getPlayerName(incomingPlayer))
                .replace("%bw_team_name%", team.getDisplayName(sender instanceof Player player ? Language.getPlayerLanguage(player) : Language.getDefaultLanguage()))
                .replace("%bw_team_color%", team.getColor().chat().toString())
                .replace("%bw_arena%", arena.getDisplayName()));
    }
}
