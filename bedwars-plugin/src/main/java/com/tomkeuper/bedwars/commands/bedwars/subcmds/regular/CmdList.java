package com.tomkeuper.bedwars.commands.bedwars.subcmds.regular;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.server.SetupType;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

import static com.tomkeuper.bedwars.BedWars.*;
import static com.tomkeuper.bedwars.api.language.Language.getList;

public class CmdList extends SubCommand {

    public CmdList(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(11);
        showInList(true);
        setDisplayInfo(
                Misc.msgHoverClick(
                        "§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName() + "         §8 - §e ver comandos de jogador",
                        "§fVer comandos de jogador.",
                        "/" + getParent().getName() + " " + getSubCommandName(),
                        ClickEvent.Action.RUN_COMMAND
                )
        );
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return false;

        final Player player = (Player) sender;

        if (SetupSession.isInSetupSession(player.getUniqueId())) {
            final SetupSession session = SetupSession.getSession(player.getUniqueId());
            Objects.requireNonNull(session).getConfig().reload();

            final ConfigurationSection yml = session.getConfig().getYml();
            final SetupType setupType = session.getSetupType();
            final String mainCmdName = getParent().getName();

            // Waiting lobby
            final boolean hasWaitingSpawn = yml.contains("waiting.Loc");
            final boolean hasPos1 = yml.contains("waiting.Pos1");
            final boolean hasPos2 = yml.contains("waiting.Pos2");
            final boolean hasBothPos = hasPos1 && hasPos2;

            // Status por equipe
            final StringBuilder spawnNotSetBars = new StringBuilder();
            final StringBuilder spawnNotSetNames = new StringBuilder();
            final StringBuilder bedNotSetBars = new StringBuilder();
            final StringBuilder shopNotSetBars = new StringBuilder();
            final StringBuilder killDropsNotSetBars = new StringBuilder();
            final StringBuilder upgradeNotSetBars = new StringBuilder();
            final StringBuilder generatorNotSetBars = new StringBuilder();

            int teamsCount = 0;

            if (yml.isConfigurationSection("Team")) {
                final ConfigurationSection teamSec = yml.getConfigurationSection("Team");
                for (String team : teamSec.getKeys(false)) {
                    final String colorPath = "Team." + team + ".Color";
                    if (!yml.contains(colorPath)) continue;

                    final ChatColor teamColor = TeamColor.getChatColor(yml.getString(colorPath));

                    if (!yml.contains("Team." + team + ".Spawn")) {
                        spawnNotSetBars.append(teamColor).append("▋");
                        spawnNotSetNames.append(teamColor).append(team).append(" ");
                    }
                    if (!yml.contains("Team." + team + ".Bed")) {
                        bedNotSetBars.append(teamColor).append("▋");
                    }
                    if (!yml.contains("Team." + team + ".Shop")) {
                        shopNotSetBars.append(teamColor).append("▋");
                    }
                    if (!yml.contains("Team." + team + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC)) {
                        killDropsNotSetBars.append(teamColor).append("▋");
                    }
                    if (!yml.contains("Team." + team + ".Upgrade")) {
                        upgradeNotSetBars.append(teamColor).append("▋");
                    }
                    final boolean hasIron = yml.contains("Team." + team + ".Iron");
                    final boolean hasGold = yml.contains("Team." + team + ".Gold");
                    if (!hasIron || !hasGold) {
                        generatorNotSetBars.append(teamColor).append("▋");
                    }
                    teamsCount++;
                }
            }

            // Geradores globais
            final int emeraldGenCount = yml.getStringList("generator.Emerald").size();
            final int diamondGenCount = yml.getStringList("generator.Diamond").size();

            // Mensagens auxiliares
            final String waitingPosMsg = resolveWaitingPosMessage(hasPos1, hasPos2);

            // Grupo/tipo
            String groupLabel = ChatColor.RED + "(NÃO DEFINIDO)";
            final String groupValue = yml.getString("group");
            if (groupValue != null && !groupValue.equalsIgnoreCase("default")) {
                groupLabel = ChatColor.GREEN + "(" + groupValue + ")";
            }

            final int maxInTeam = yml.getInt("maxInTeam");

            // Linhas com status
            final String lineSetWaitingSpawn = session.dot()
                    + (hasWaitingSpawn ? ChatColor.STRIKETHROUGH : "")
                    + "setWaitingSpawn" + ChatColor.RESET + " "
                    + (hasWaitingSpawn ? ChatColor.GREEN + "(DEFINIDO)" : ChatColor.RED + "(NÃO DEFINIDO)");

            final String lineWaitingPos = session.dot()
                    + (hasBothPos ? ChatColor.STRIKETHROUGH : "")
                    + "waitingPos 1/2" + ChatColor.RESET + " "
                    + waitingPosMsg;

            final String lineSetSpawn = session.dot()
                    + (spawnNotSetBars.isEmpty() ? ChatColor.STRIKETHROUGH : "")
                    + "setSpawn <teamName>" + ChatColor.RESET + " "
                    + (spawnNotSetBars.isEmpty()
                    ? ChatColor.GREEN + "(TODOS DEFINIDOS)"
                    : ChatColor.RED + "(Restantes: " + spawnNotSetBars + ChatColor.RED + ")");

            final String lineSetBed = session.dot()
                    + (bedNotSetBars.isEmpty() ? ChatColor.STRIKETHROUGH : "")
                    + "setBed" + ChatColor.RESET + " "
                    + (bedNotSetBars.isEmpty()
                    ? ChatColor.GREEN + "(TODOS DEFINIDOS)"
                    : ChatColor.RED + "(Restantes: " + bedNotSetBars + ChatColor.RED + ")");

            final String lineSetShop = session.dot()
                    + (shopNotSetBars.isEmpty() ? ChatColor.STRIKETHROUGH : "")
                    + "setShop" + ChatColor.RESET + " "
                    + (shopNotSetBars.isEmpty()
                    ? ChatColor.GREEN + "(TODOS DEFINIDOS)"
                    : ChatColor.RED + "(Restantes: " + shopNotSetBars + ChatColor.RED + ")");

            final boolean killDropsOptionalConcluded = shopNotSetBars.isEmpty();

            final String lineSetKillDrops = session.dot()
                    + (killDropsOptionalConcluded ? ChatColor.STRIKETHROUGH : "")
                    + "setKillDrops" + ChatColor.RESET + " "
                    + (killDropsOptionalConcluded
                    ? ChatColor.GREEN + "(OPCIONAL - CONCLUÍDO)"
                    : (killDropsNotSetBars.isEmpty()
                    ? ChatColor.GRAY + "(OPCIONAL)"
                    : ChatColor.RED + "(Restantes: " + killDropsNotSetBars + ChatColor.RED + ")"));

            final String lineSetUpgrade = session.dot()
                    + (upgradeNotSetBars.isEmpty() ? ChatColor.STRIKETHROUGH : "")
                    + "setUpgrade" + ChatColor.RESET + " "
                    + (upgradeNotSetBars.isEmpty()
                    ? ChatColor.GREEN + "(TODOS DEFINIDOS)"
                    : ChatColor.RED + "(Restantes: " + upgradeNotSetBars + ChatColor.RED + ")");

            final String lineAddGenerator = session.dot()
                    + "addGenerator "
                    + (generatorNotSetBars.isEmpty() ? "" : ChatColor.RED + "(Restantes: " + generatorNotSetBars + ChatColor.RED + ") ")
                    + ChatColor.YELLOW + "(" + ChatColor.DARK_GREEN + "E" + emeraldGenCount + " " + ChatColor.AQUA + "D" + diamondGenCount + ChatColor.YELLOW + ")";

            final String lineSetSpectatorSpawn = session.dot()
                    + (yml.contains(ConfigPath.ARENA_SPEC_LOC) ? ChatColor.STRIKETHROUGH : "")
                    + "setSpectSpawn" + ChatColor.RESET + " "
                    + (yml.contains(ConfigPath.ARENA_SPEC_LOC) ? ChatColor.GRAY + "(DEFINIDO)" : ChatColor.RED + "(NÃO DEFINIDO)");

            // Cabeçalho
            sender.sendMessage("");
            sender.sendMessage(
                    ChatColor.GRAY + "" + ChatColor.BOLD + MainCommand.getDot()
                            + ChatColor.GOLD + plugin.getDescription().getName()
                            + " v" + plugin.getDescription().getVersion()
                            + ChatColor.GRAY + '-' + " "
                            + ChatColor.GREEN + session.getWorldName() + " comandos"
            );

            // Corpo (UI com hover/click)
            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineSetWaitingSpawn,
                            ChatColor.WHITE + "Defina o local onde os jogadores\n" + ChatColor.WHITE + "irão esperar antes da partida começar.",
                            "/" + mainCmdName + " setWaitingSpawn",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineWaitingPos,
                            ChatColor.WHITE + "Faça o lobby de espera desaparecer ao iniciar.\n"
                                    + ChatColor.WHITE + "Selecione como uma região do WorldEdit.",
                            "/" + mainCmdName + " waitingPos ",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

            if (setupType == SetupType.ADVANCED) {
                player.spigot().sendMessage(
                        msgWithHoverAndClick(
                                lineSetSpectatorSpawn,
                                ChatColor.WHITE + "Defina onde os espectadores irão nascer.",
                                "/" + mainCmdName + " setSpectSpawn",
                                ClickEvent.Action.RUN_COMMAND
                        )
                );
            }

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            session.dot() + "autoCreateTeams " + ChatColor.YELLOW + "(detecção automática)",
                            ChatColor.WHITE + "Crie equipes com base nas cores das ilhas.",
                            "/" + mainCmdName + " autoCreateTeams",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            session.dot() + "createTeam <name> <color> " + ChatColor.YELLOW + "(" + teamsCount + " CRIADAS)",
                            ChatColor.WHITE + "Crie uma equipe.",
                            "/" + mainCmdName + " createTeam ",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            session.dot() + "removeTeam <name>",
                            ChatColor.WHITE + "Remova uma equipe pelo nome.",
                            "/" + mainCmd + " removeTeam ",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineSetSpawn,
                            ChatColor.WHITE + "Defina o spawn de uma equipe.\n"
                                    + ChatColor.WHITE + "Equipes sem spawn definido:\n"
                                    + spawnNotSetNames,
                            "/" + mainCmdName + " setSpawn ",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineSetBed,
                            ChatColor.WHITE + "Defina a localização da cama de uma equipe.\n"
                                    + ChatColor.WHITE + "Você não precisa especificar o nome da equipe.",
                            "/" + mainCmdName + " setBed",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineSetShop,
                            ChatColor.WHITE + "Defina o NPC da equipe.\n"
                                    + ChatColor.WHITE + "Você não precisa especificar o nome da equipe.\n"
                                    + ChatColor.WHITE + "Ele será spawnado apenas quando o jogo começar.",
                            "/" + mainCmdName + " setShop",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineSetUpgrade,
                            ChatColor.WHITE + "Defina o NPC de melhorias da equipe.\n"
                                    + ChatColor.WHITE + "Você não precisa especificar o nome da equipe.\n"
                                    + ChatColor.WHITE + "Ele será spawnado apenas quando o jogo começar.",
                            "/" + mainCmdName + " setUpgrade",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            if (setupType == SetupType.ADVANCED) {
                player.spigot().sendMessage(
                        msgWithHoverAndClick(
                                lineSetKillDrops,
                                ChatColor.WHITE + "(Opcional) Defina o local onde os itens do inimigo\n"
                                        + ChatColor.WHITE + "serão dropados após eliminá-lo.",
                                "/" + mainCmdName + " setKillDrops ",
                                ClickEvent.Action.SUGGEST_COMMAND
                        )
                );
            }

            final String genHover =
                    (setupType == SetupType.ADVANCED
                            ? ChatColor.WHITE + "Adicione um ponto de spawn de gerador.\n" + ChatColor.YELLOW + "/" + mainCmdName + " addGenerator <Iron/ Gold/ Emerald, Diamond>"
                            : ChatColor.WHITE + "Adicione um ponto de spawn de gerador.\n" + ChatColor.YELLOW + "Fique em uma ilha de equipe para definir o gerador da equipe")
                            + "\n" + ChatColor.WHITE + "Fique em um bloco de diamante para definir o gerador de diamante.\n"
                            + ChatColor.WHITE + "Fique em um bloco de esmeralda para definir o gerador de esmeralda.";

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            lineAddGenerator,
                            genHover,
                            "/" + mainCmdName + " addGenerator ",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            session.dot() + "removeGenerator",
                            genHover,
                            "/" + mainCmdName + " removeGenerator",
                            (setupType == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND)
                    )
            );

            if (setupType == SetupType.ADVANCED) {
                player.spigot().sendMessage(
                        msgWithHoverAndClick(
                                session.dot() + "setMaxInTeam <int> (ATUALMENTE " + maxInTeam + ")",
                                ChatColor.WHITE + "Defina o tamanho máximo do time.",
                                "/" + mainCmd + " setMaxInTeam ",
                                ClickEvent.Action.SUGGEST_COMMAND
                        )
                );
                player.spigot().sendMessage(
                        msgWithHoverAndClick(
                                session.dot() + "arenaGroup " + groupLabel,
                                ChatColor.WHITE + "Defina o grupo da arena.",
                                "/" + mainCmd + " arenaGroup ",
                                ClickEvent.Action.SUGGEST_COMMAND
                        )
                );
            } else {
                player.spigot().sendMessage(
                        msgWithHoverAndClick(
                                session.dot() + "setType <type> " + groupLabel,
                                ChatColor.WHITE + "Adicione a arena a um grupo.",
                                "/" + mainCmdName + " setType",
                                ClickEvent.Action.RUN_COMMAND
                        )
                );
            }

            player.spigot().sendMessage(
                    msgWithHoverAndClick(
                            session.dot() + "save",
                            ChatColor.WHITE + "Salvar arena e voltar ao lobby",
                            "/" + mainCmdName + " save",
                            ClickEvent.Action.SUGGEST_COMMAND
                    )
            );

        } else {
            final TextComponent credits = new TextComponent(
                    ChatColor.BLUE + "" + ChatColor.BOLD + MainCommand.getDot()
                            + " " + ChatColor.GOLD + plugin.getName()
                            + " " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion()
                            + " por MrCeasar"
            );
            credits.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
            credits.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            ChatColor.GRAY + "Arenas: "
                                    + (Arena.getArenas().isEmpty() ? ChatColor.RED + "0" : ChatColor.GREEN + "" + Arena.getArenas().size())
                    ).create()
            ));
            ((Player) sender).spigot().sendMessage(credits);

            for (String line : getList((Player) sender, Messages.COMMAND_MAIN)) {
                sender.sendMessage(line);
            }
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender sender, BedWars api) {
        if (sender instanceof ConsoleCommandSender) return false;
        if (sender instanceof Player p) {
            if (Arena.isInArena(p)) return false;
            if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        }
        return hasPermission(sender);
    }

    private static TextComponent msgWithHoverAndClick(String text, String hover, String command, ClickEvent.Action action) {
        return Misc.msgHoverClick(text, hover, command, action);
    }

    private static String resolveWaitingPosMessage(boolean hasPos1, boolean hasPos2) {
        if (hasPos1 && !hasPos2) return ChatColor.RED + "(POS 2 NÃO DEFINIDA)";
        if (!hasPos1 && hasPos2) return ChatColor.RED + "(POS 1 NÃO DEFINIDA)";
        if (hasPos1) return ChatColor.GREEN + "(DEFINIDO)";
        return ChatColor.GRAY + "(NÃO DEFINIDO) " + ChatColor.ITALIC + "OPCIONAL";
    }
}
