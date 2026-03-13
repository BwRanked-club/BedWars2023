package com.tomkeuper.bedwars.language;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class English extends Language {

    public English() {
        super(BedWars.plugin, "en");

        YamlConfiguration yml = getYml();
        yml.options().copyDefaults(true);
        yml.options().header("RGB is supported with 3 methods: <SOLID:FF0080> -> Solid, <RAINBOW1></RAINBOW> -> Rainbow (number customizable) & <GRADIENT:2C08BA></GRADIENT:028A97> -> Gradient");
        yml.addDefault(Messages.PREFIX, "");
        yml.addDefault("name", "English");

        yml.addDefault(Messages.COMMAND_MAIN, Arrays.asList("", "&a▪ &f/" + BedWars.mainCmd + " stats", "&a▪ &f/" + BedWars.mainCmd + " historico", "&a▪ &f/" + BedWars.mainCmd + " join &o<arena/group>", "&a▪ &f/" + BedWars.mainCmd + " leave", "&a▪ &f/" + BedWars.mainCmd + " lang", "&a▪ &f/" + BedWars.mainCmd + " gui", "&a▪ &f/" + BedWars.mainCmd + " start &b(vip)"));
        yml.addDefault(Messages.COMMAND_LANG_LIST_HEADER, "%bw_lang_prefix% &aAvailable languages:");
        yml.addDefault(Messages.COMMAND_LANG_LIST_FORMAT, "&a▪  &f%bw_lang_iso% - &f%bw_name%");
        yml.addDefault(Messages.COMMAND_LANG_USAGE, "%bw_lang_prefix%&fUsage: /lang &f&o<iso>");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_NOT_EXIST, "%bw_lang_prefix%&bThis language doesn't exist!");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_SUCCESSFULLY, "%bw_lang_prefix%&aLanguage changed!");
        yml.addDefault(Messages.COMMAND_LANG_USAGE_DENIED, "%bw_lang_prefix%&bYou can't change the language during the game.");
        yml.addDefault(Messages.COMMAND_JOIN_USAGE, "&a▪ &fUsage: /" + BedWars.mainCmd + " join &o<arena/group>");
        yml.addDefault(Messages.COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND, "%bw_lang_prefix%&bThere isn't any arena or arena group called: %bw_name%");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL, "%bw_lang_prefix%&bThis arena is full!\n&aPlease consider donating for more features. &f&o(click)");
        yml.addDefault(Messages.COMMAND_JOIN_NO_EMPTY_FOUND, "%bw_lang_prefix%&bThere isn't any arena available right now ;(");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS, "%bw_lang_prefix%&bWe apologise but this arena is full.\n&bWe know you're a donor but actually this arena is full of staff or/and donors.");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_PARTY_TOO_BIG, "%bw_lang_prefix%&bYour party is too big for joining this arena as a team :(");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER, "%bw_lang_prefix%&bOnly the leader can choose the arena.");
        yml.addDefault(Messages.COMMAND_JOIN_PLAYER_JOIN_MSG, "%bw_lang_prefix%&f%bw_player% &bhas joined (&b%bw_on%&b/&b%bw_max%&b)!");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_MSG, "%bw_lang_prefix%&bYou are now spectating &b%bw_arena%&b.\n%bw_lang_prefix%&bYou can leave the arena at any time doing &b/leave&b.");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_DENIED_MSG, "&bSpectators are not allowed in this arena!");
        yml.addDefault(Messages.COMMAND_TP_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bPlayer not found!");
        yml.addDefault(Messages.COMMAND_TP_NOT_IN_ARENA, "%bw_lang_prefix%&bThis player is not in a bedwars arena!");
        yml.addDefault(Messages.COMMAND_TP_NOT_STARTED, "%bw_lang_prefix%&bThe arena where the player is didn't start yet!");
        yml.addDefault(Messages.COMMAND_TP_USAGE, "%bw_lang_prefix%&bUsage: /bw tp <username>");
        yml.addDefault(Messages.REJOIN_NO_ARENA, "%bw_lang_prefix%&bThere is no arena to rejoin!");
        yml.addDefault(Messages.REJOIN_DENIED, "%bw_lang_prefix%&bYou can't rejoin the arena anymore. Game ended or bed destroyed.");
        yml.addDefault(Messages.REJOIN_ALLOWED, "%bw_lang_prefix%&bJoining arena &a%bw_arena%&b!");
        yml.addDefault(Messages.COMMAND_REJOIN_PLAYER_RECONNECTED, "%bw_lang_prefix%&f%bw_player% &bhas reconnected!");
        yml.addDefault(Messages.COMMAND_LEAVE_DENIED_NOT_IN_ARENA, "%bw_lang_prefix%&bYou're not in arena!");
        yml.addDefault(Messages.COMMAND_LEAVE_MSG, "%bw_lang_prefix%&f%bw_player% &bhas quit!");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_TITLE, "Are you sure?");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_STAY, "&bNo");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_STAY_LORE, List.of("&fStay in the arena"));
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_BRING_PARTY, "&aYes");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_BRING_PARTY_LORE, List.of("&fSummon party with you"));
        yml.addDefault(Messages.COMMAND_NOT_ALLOWED_IN_GAME, "%bw_lang_prefix%&bYou can't do this during the game.");
        yml.addDefault(Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS, "%bw_lang_prefix%&bCommand not found or you don't have permission!");
        yml.addDefault(Messages.COMMAND_PARTY_HELP, Arrays.asList("&b▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&aParty Commands:",
                "&b/party help &f- &bPrints this help message",
                "&b/party invite <player> &f- &bInvites the player to your party",
                "&b/party leave &f- &bLeaves the current party",
                "&b/party remove <player> &f- &bRemove the player from the party",
                "&b/party info &f- &bShow party members and owner",
                "&b/party promote <player> &f- &bTransfer party ownership",
                "&b/party accept <player> &f- &bAccept a party invite",
                "&b/party disband &f- &bDisbands the party")
        );
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_USAGE, "%bw_lang_prefix%&bUsage: &f/party invite <player>");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE, "%bw_lang_prefix%&f%bw_player% &bis not online!");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT, "%bw_lang_prefix%&bInvite sent to &f%bw_player%&b.");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG, "%bw_lang_prefix%&b%bw_player% &bhas invited you to a party! &o&f(Click to accept)");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF, "%bw_lang_prefix%&bYou cannot invite yourself!");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE, "%bw_lang_prefix%&bThere are no party requests to accept!");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY, "%bw_lang_prefix%&bYou're already in a party!");
        yml.addDefault(Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS, "%bw_lang_prefix%&bOnly the party owner can do this!");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_USAGE, "%bw_lang_prefix%&bUsage: &f/party accept <player>");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bhas joined the party!");
        yml.addDefault(Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY, "%bw_lang_prefix%&bYou're not in a party!");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND, "%bw_lang_prefix%&bYou can't leave your own party!\n&bTry using: &b/party disband");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bhas left the party!");
        yml.addDefault(Messages.COMMAND_PARTY_DISBAND_SUCCESS, "%bw_lang_prefix%&bParty disbanded!");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_USAGE, "%bw_lang_prefix%&fUsage: &b/party remove <player>");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bwas removed from the party,");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER, "%bw_lang_prefix%&f%bw_player% &bis not in your party!");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_SUCCESS, "%bw_lang_prefix%&bYou successfully promoted %bw_player% to owner");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_OWNER, "%bw_lang_prefix%&bYou have been promoted to party owner");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER, "%bw_lang_prefix%&f &b%bw_player% has been promoted to owner");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_OWNER, "\n%bw_lang_prefix%&bOwner of the party is: &f%bw_party_owner%");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYERS, "%bw_lang_prefix%&bParty members:");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYER, "&f%bw_player%");
        yml.addDefault(Messages.COMMAND_PARTY_CHAT_ENABLED_DISABLED, "&b&lPARTY &f&l┃ &fParty chat is now %bw_party_chat_status% &f!");
        yml.addDefault(Messages.COMMAND_FORCESTART_NOT_IN_GAME, "&b▪ &fYou're not playing!");
        yml.addDefault(Messages.COMMAND_FORCESTART_SUCCESS, "&b▪ &fCountdown shortened!");
        yml.addDefault(Messages.COMMAND_FORCESTART_NO_PERM, "%bw_lang_prefix%&fYou can't forcestart the arena.\n&fPlease consider donating for VIP features.");
        yml.addDefault(Messages.COMMAND_COOLDOWN, "&bYou can't do that yet! Wait %bw_seconds% more seconds!");
        yml.addDefault(Messages.COMMAND_SHOUT_DISABLE_SOLO, "&bShouting is disabled in Solo!");
        yml.addDefault(Messages.COMMAND_LEAVE_STARTED, "&a&lTeleporting you to the lobby in %bw_leave_delay% seconds... Right-click again to cancel the teleport!");
        yml.addDefault(Messages.COMMAND_LEAVE_CANCELED, "&b&lTeleport cancelled!");
        yml.addDefault(Messages.ARENA_JOIN_VIP_KICK, "%bw_lang_prefix%&bSorry, but you were kicked out because a donor joined the arena.\n&aPlease consider donating for more features. &f&o(click)");
        yml.addDefault(Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT, "%bw_lang_prefix%&bThere aren't enough players! Countdown stopped!");
        yml.addDefault(Messages.ARENA_RESTART_PLAYER_KICK, "%bw_lang_prefix%&bThe arena you were in is restarting.");
        yml.addDefault(Messages.ARENA_STATUS_PLAYING_NAME, "&bPlaying");
        yml.addDefault(Messages.ARENA_STATUS_RESTARTING_NAME, "&bRestarting");
        yml.addDefault(Messages.ARENA_STATUS_WAITING_NAME, "&aWaiting &b%bw_full%");
        yml.addDefault(Messages.ARENA_STATUS_STARTING_NAME, "&bStarting &b%bw_full%");
        yml.addDefault(Messages.ARENA_GUI_INV_NAME, "&fClick to join");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_NAME, "&a&l%bw_name%");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_LORE, Arrays.asList("", "&fStatus: %bw_arena_status%", "&fPlayers: &f%bw_on%&f/&f%bw_max%", "&fType: &a%bw_group%", "", "&aLeft-Click to join.", "&bRight-Click to spectate."));
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_NAME, "&r%bw_server_ip%");
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_LORE, Collections.emptyList());
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CHAT, "%bw_lang_prefix%&bThe game starts in &b%bw_time% &bseconds!");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE, "&a%bw_seconds%");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-5", "&b❺");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-4", "&b❹");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-3", "&b❸");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-2", "&b❷");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-1", "&b❶");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE, "&bWaiting for more players..");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TITLE, "&aGO");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TUTORIAL, Arrays.asList(
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &lBedWars", "",
                "&b&l    Protect your bed and destroy the enemy beds.",
                "&b&l      Upgrade yourself and your team by collecting",
                "&b&l   Iron, Gold, Emerald, and Diamond from generators",
                "&b&l             to access powerful upgrades.", "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        yml.addDefault(Messages.ARENA_JOIN_DENIED_SELECTOR, "%bw_lang_prefix%&bSorry but you can't join this arena at this moment. Use Right-Click to spectate!");
        yml.addDefault(Messages.ARENA_SPECTATE_DENIED_SELECTOR, "%bw_lang_prefix%&bSorry but you can't spectate this arena at this moment. Use Left-Click to join!");
        yml.addDefault(Messages.ARENA_JOIN_DENIED_NO_PROXY, "&bSorry but you must join an arena using BedWarsProxy. \n&bIf you want to setup an arena make sure to give yourself the bw.setup permission so you can join the server directly!");
        yml.addDefault(Messages.ARENA_JOIN_DENIED_NO_TIME, "&bSorry but you joined while the game was already started.");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_NAME, "&fTeleporter");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME, "%bw_v_prefix%%bw_player%");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE, Arrays.asList("&fHealth: &f%bw_player_health%%", "&fFood: &f%bw_player_food%", "", "&fLeft-click to spectate"));
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_NAME, "&b&lReturn to lobby");
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_LORE, Collections.singletonList("&fRight-click to leave to the lobby!"));
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE, "&aSpectating &f%bw_player%");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE, "&bSNEAK to exit");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE, "&bExiting Spectator mode");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE, "");
        yml.addDefault(Messages.ARENA_LEAVE_PARTY_DISBANDED, "%bw_lang_prefix%&bThe party owner has left and the party was disbanded!");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIER, "&bTier &b%bw_tier%");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND, "&b&lDiamond");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD, "&a&lEmerald");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIMER, "&bSpawns in &b%bw_seconds% &bseconds");
        yml.addDefault(Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT, "%bw_lang_prefix%%bw_generator_type% Generators &bhave been upgraded to Tier &b%bw_tier%");
        yml.addDefault(Messages.FORMATTING_CHAT_LOBBY, "%bw_level%%bw_v_prefix%&f%bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_WAITING, "%bw_level%%bw_v_prefix%&f%bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_SHOUT, "%bw_level%%bw_v_prefix%&b[SHOUT] %bw_team_format% &f%bw_player%&f%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_TEAM, "%bw_level%%bw_v_prefix%&f%bw_team_format%&f %bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_SPECTATOR, "%bw_level%%bw_v_prefix%&f[SPECTATOR] %bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_SPECTATOR, List.of("&f"));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_SPECTATOR, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_RESTARTING, Arrays.asList("%bw_team_color%&l%bw_team_letter% &r%bw_team_color%", "%bw_team% ", "%bw_v_prefix% %bw_team_color%"));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_RESTARTING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_PLAYING, Arrays.asList("%bw_team_color%&l%bw_team_letter% &r%bw_team_color%", "%bw_team% ", "%bw_v_prefix% %bw_team_color%&l%bw_team_letter% &r%bw_team_color%"));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_PLAYING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_STARTING, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_STARTING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_WAITING, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_WAITING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_PREFIX_LOBBY, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TAB_SUFFIX_LOBBY, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_PREFIX_RESTARTING, List.of("%bw_team_color%&l%bw_team_letter% &r%bw_team_color%"));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_SUFFIX_RESTARTING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_PREFIX_PLAYING, List.of("%bw_team_color%&l%bw_team_letter% &r%bw_team_color%"));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_SUFFIX_PLAYING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_PREFIX_STARTING, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_SUFFIX_STARTING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_PREFIX_WAITING, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_SUFFIX_WAITING, new ArrayList<>());
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_PREFIX_LOBBY, List.of("%bw_v_prefix% "));
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_HEAD_SUFFIX_LOBBY, new ArrayList<>());

        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_LOBBY, "&b%bw_server_ip%\n");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_WAITING, "&a%bw_server_ip%\n");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_STARTING, "&b%bw_server_ip%\n");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_PLAYING, "&b%bw_server_ip%\n");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_RESTARTING, "&b%bw_server_ip%\n");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_SPECTATOR, "&b%bw_server_ip%\n");

        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_LOBBY, "\n&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_WAITING, "\n&a%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_STARTING, "\n&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_PLAYING, "\n&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_RESTARTING, "\n&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_SPECTATOR, "\n&b%bw_server_ip%");

        yml.addDefault(Messages.FORMATTING_SCOREBOARD_DATE, "dd/MM/yy");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_GENERIC, "%bw_team_color%%bw_team_letter%&f %bw_team_name%: %bw_team_status%");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ELIMINATED, "&b&l✘");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_BED_DESTROYED, "&a%bw_players_remaining%");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ALIVE, "&a&l✓");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_NEXEVENT_TIMER, "mm:ss");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_YOUR_TEAM, "&f YOU");
        yml.addDefault(Messages.FORMATTING_ACTION_BAR_TRACKING, "&fTracking: %bw_team% &f- Distance: %bw_distance%m");
        yml.addDefault(Messages.FORMATTING_BOSSBAR_DRAGON, "%bw_team% &fDragon");
        yml.addDefault(Messages.FORMATTING_TEAM_WINNER_FORMAT, "      %bw_team_color%%bw_team_name% &f- %bw_winner_members%");
        yml.addDefault(Messages.FORMATTING_SOLO_WINNER_FORMAT, "                 %bw_team_color%%bw_team_name% &f- %bw_winner_members%");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER1, "I");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER2, "II");
        yml.addDefault(Messages.FORMATTING_GENERATOR_TIER3, "III");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH, "▮ ");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_PREFIX, "&f[");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_SUFFIX, "&f]");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_FILLED, "|");
        yml.addDefault(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_EMPTY, "&f|");
        yml.addDefault(Messages.FORMATTING_STATS_DATE_FORMAT, "yyyy/MM/dd HH:mm");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM, "%bw_team_color%[%bw_team_name%]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SHOUT, "&b[SHOUT]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SPECTATOR, "&f[SPECTATOR]");
        yml.addDefault(Messages.MEANING_FULL, "Full");
        yml.addDefault(Messages.MEANING_SHOUT, "shout");
        yml.addDefault(Messages.MEANING_NOBODY, "Nobody");
        yml.addDefault(Messages.MEANING_NEVER, "Never");
        yml.addDefault(Messages.MEANING_IRON_SINGULAR, "Iron");
        yml.addDefault(Messages.MEANING_IRON_PLURAL, "Iron");
        yml.addDefault(Messages.MEANING_GOLD_SINGULAR, "Gold");
        yml.addDefault(Messages.MEANING_GOLD_PLURAL, "Gold");
        yml.addDefault(Messages.MEANING_EMERALD_SINGULAR, "Emerald");
        yml.addDefault(Messages.MEANING_EMERALD_PLURAL, "Emeralds");
        yml.addDefault(Messages.MEANING_DIAMOND_SINGULAR, "Diamond");
        yml.addDefault(Messages.MEANING_DIAMOND_PLURAL, "Diamonds");
        yml.addDefault(Messages.MEANING_VAULT_SINGULAR, "$");
        yml.addDefault(Messages.MEANING_VAULT_PLURAL, "$");
        yml.addDefault(Messages.MEANING_ENABLED, "&aEnabled");
        yml.addDefault(Messages.MEANING_DISABLED, "&bDisabled");
        yml.addDefault(Messages.INTERACT_CANNOT_PLACE_BLOCK, "%bw_lang_prefix%&bYou can't place blocks here!");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_BLOCK, "%bw_lang_prefix%&bYou can only break blocks placed by a player!");
        yml.addDefault(Messages.INTERACT_FULL_CHEST, "%bw_lang_prefix%&bThe chest is full!");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_OWN_BED, "&bYou can't destroy your own bed!");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT, "\n&f&lBED DESTRUCTION > %bw_team_color%%bw_team_name% Bed &fwas deep fried by %bw_player_color%%bw_player%&f!\n");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT, "&bBED DESTROYED!");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT, "&fYou will no longer respawn!");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM, "&f&lBED DESTRUCTION > &fYour bed was iced by %bw_player_color%%bw_player%&f!");
        yml.addDefault(Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED, "&bYou can't open this chest because this team wasn't eliminated!");
        yml.addDefault(Messages.INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN, "&bYou are no longer invisible because you have taken damage!");
        yml.addDefault(Messages.INTERACT_MAGIC_MILK_REMOVED, "&bYour Magic Milk wore off!");
        yml.addDefault(Messages.ITEM_DEPOSITED_SUCCESSFULLY, "&fYou have saved &b(x{AMOUNT}) {ITEM} &fin the chest.");
        yml.addDefault(Messages.EGGBRIDGE_BUILD_LIMIT_WARNING, "&bYou are too close to the build limit!");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL, "%bw_player_color%%bw_player% &ffell into the void.");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL, "%bw_player_color%%bw_player% &ffell into the void. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL, "%bw_player_color%%bw_player% &fwas knocked into the void by %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL, "%bw_player_color%%bw_player% &fwas knocked into the void by %bw_killer_color%%bw_killer_name%&f. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_REGULAR, "%bw_player_color%%bw_player% &fdisconnected while fighting with %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL, "%bw_player_color%%bw_player% &fdisconnected while fighting with %bw_killer_color%%bw_killer_name%&f. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL, "%bw_player_color%%bw_player% &fwas pushed by %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL, "%bw_player_color%%bw_player% &fwas pushed by %bw_killer_color%%bw_killer_name%&f. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL, "%bw_player_color%%bw_player% &fwas hit off by a love bomb from %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL, "%bw_player_color%%bw_player% &fwas hit off by a love bomb from %bw_killer_color%%bw_killer_name%&f. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR, "%bw_player_color%%bw_player% &fwas hit off by a bomb.");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL, "%bw_player_color%%bw_player% &fwas hit off by a bomb. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_PVP_REGULAR_KILL, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_PVP_FINAL_KILL, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_name%&f. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_REGULAR, "%bw_player_color%%bw_player% &fdied.");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL, "%bw_player_color%%bw_player% &fdied. &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_REGULAR, "%bw_player_color%%bw_player% &fwas shot by %bw_killer_color%%bw_killer_name%&f!");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_FINAL_KILL, "%bw_player_color%%bw_player% &fwas shot by %bw_killer_color%%bw_killer_name%&f! &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_REGULAR, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_team_name%'s &fBedBug!");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_FINAL_KILL, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_team_name%'s &fBedBug! &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_REGULAR, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_team_name%'s &fIron Golem!");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_FINAL_KILL, "%bw_player_color%%bw_player% &fwas killed by %bw_killer_color%%bw_killer_team_name%'s &fIron Golem! &b&lFINAL KILL!");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_DIAMOND, "%bw_lang_prefix%&b+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_EMERALD, "%bw_lang_prefix%&a+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_IRON, "%bw_lang_prefix%&f+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_GOLD, "%bw_lang_prefix%&b+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.ARENA_MAX_BUILD_LIMIT_REACHED, "&bMax build height limit reached!");
        yml.addDefault(Messages.ARENA_MIN_BUILD_LIMIT_REACHED, "&bMin build height limit reached!");
        yml.addDefault(Messages.ARENA_FIREBALL_COOLDOWN, "&bPlease wait %bw_cooldown%s to use that again!");
        yml.addDefault(Messages.ARENA_IN_GAME_ANNOUNCEMENT, Arrays.asList("&b&lIf you get disconnected use /rejoin to join back in the game.", "&b&lCross-teaming is not allowed! Report cross-teamers using /report."));
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_TITLE, "&bYOU DIED!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_SUBTITLE, "&bYou will respawn in &b%bw_time% &bseconds!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_CHAT, "%bw_lang_prefix%&bYou will respawn in &b%bw_time% &bseconds!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWNED_TITLE, "&aRESPAWNED!");
        yml.addDefault(Messages.PLAYER_DIE_ELIMINATED_CHAT, "%bw_lang_prefix%&bYou have been eliminated!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWNED_TEXT, "%bw_lang_prefix%&bYou have respawned!");
        yml.addDefault(Messages.PLAYER_HIT_BOW, "%bw_lang_prefix%%bw_player% &fis on &b%bw_damage_amount% &fHP!");
        yml.addDefault(Messages.GAME_END_GAME_OVER_PLAYER_TITLE, "&b&lGAME OVER!");
        yml.addDefault(Messages.GAME_END_VICTORY_PLAYER_TITLE, "&b&lVICTORY!");
        yml.addDefault(Messages.GAME_END_TEAM_WON_CHAT, "%bw_lang_prefix%%bw_team_color%%bw_team_name% &ahas won the game!");
        yml.addDefault(Messages.FORMATTING_EACH_WINNER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_FIRST_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_SECOND_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_THIRD_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_TOP_PLAYER_CHAT, Arrays.asList(
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &lBedWars", "", "%bw_winner_format%", "", "",
                "&b                          &l1st Killer &f- %bw_first_format% - %bw_first_kills%",
                "&b                          &l2nd Killer &f- %bw_second_format% - %bw_second_kills%",
                "&b                          &l3rd Killer &f- %bw_third_format% - %bw_third_kills%", "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        yml.addDefault(Messages.GAME_END_MAP_RATING_QUESTION, "%bw_lang_prefix%&fHow many stars do you think this map deserves? &f(%bw_map%)");
        yml.addDefault(Messages.GAME_END_MAP_RATING_STAR, "&f✯");
        yml.addDefault(Messages.GAME_END_MAP_RATING_HOVER, "&bClick to rate &b%bw_rating% &bstars");
        yml.addDefault(Messages.GAME_END_MAP_RATING_SUCCESS, "%bw_lang_prefix%&aYou rated &f%bw_map% &awith &b%bw_rating% &astars!");
        yml.addDefault(Messages.GAME_END_MAP_RATING_NOT_AVAILABLE, "%bw_lang_prefix%&bYou can only rate the map at the end of the match.");
        yml.addDefault(Messages.GAME_END_MAP_RATING_INVALID, "%bw_lang_prefix%&bInvalid rating.");
        yml.addDefault(Messages.BED_HOLOGRAM_DEFEND, "&b&lDefend your bed!");
        yml.addDefault(Messages.BED_HOLOGRAM_DESTROYED, "&b&lYour bed was destroyed!");
        yml.addDefault(Messages.TEAM_ELIMINATED_CHAT, "\n&f&lTEAM ELIMINATED > %bw_team_color%%bw_team_name% Team &bhas been eliminated!\n");
        yml.addDefault(Messages.NEXT_EVENT_BEDS_DESTROY, "&fBed Gone");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_II, "&fDiamond II");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_III, "&fDiamond III");
        yml.addDefault(Messages.NEXT_EVENT_DRAGON_SPAWN, "&fSudden Death");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_II, "&fEmerald II");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_III, "&fEmerald III");
        yml.addDefault(Messages.NEXT_EVENT_GAME_END, "&bGame End");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED, "&bBED DESTROYED!");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED, "&fAll beds have been destroyed!");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED, "&b&lAll beds have been destroyed!");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH, "&bSudden Death");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH, "");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH, "&bSUDDEN DEATH: &b&b%bw_dragons_amount% %bw_team_color%%bw_team_name% Dragon!");
        yml.addDefault(Messages.XP_REWARD_PER_MINUTE, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Play Time).");
        yml.addDefault(Messages.XP_REWARD_WIN, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Game Win).");
        yml.addDefault(Messages.XP_REWARD_PER_TEAMMATE, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Team Support).");
        yml.addDefault(Messages.XP_REWARD_BED_DESTROY, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Bed Destroyed).");
        yml.addDefault(Messages.XP_REWARD_REGULAR_KILL, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Regular Kill).");
        yml.addDefault(Messages.XP_REWARD_FINAL_KILL, "%bw_lang_prefix%&b+%bw_xp% BedWars Experience Received (Final Kill).");
        yml.addDefault(Messages.XP_REWARD_HALLOWEEN, "%bw_lang_prefix%&b+5 BedWars Experience Received (Halloween).");
        yml.addDefault(Messages.PLAYER_LEVEL_UP, Collections.singletonList("&aCongratulations! You have leveled up to %bw_level%."));

        yml.addDefault(Messages.MONEY_REWARD_PER_MINUTE, "%bw_lang_prefix%&b+%bw_money% Coins (Play Time).");
        yml.addDefault(Messages.MONEY_REWARD_WIN, "%bw_lang_prefix%&b+%bw_money% Coins (Game Win).");
        yml.addDefault(Messages.MONEY_REWARD_PER_TEAMMATE, "%bw_lang_prefix%&b+%bw_money% Coins (Team Support).");
        yml.addDefault(Messages.MONEY_REWARD_BED_DESTROYED, "%bw_lang_prefix%&b+%bw_money% Coins (Bed Destroyed).");
        yml.addDefault(Messages.MONEY_REWARD_FINAL_KILL, "%bw_lang_prefix%&b+%bw_money% Coins (Final Kill).");
        yml.addDefault(Messages.MONEY_REWARD_REGULAR_KILL, "%bw_lang_prefix%&b+%bw_money% Coins (Regular Kill).");

        yml.addDefault(Messages.HALLOWEEN_ITEM_NAME, "&b&lHappy Halloween");
        yml.addDefault(Messages.FORMATTING_HISTORY_DATE_FORMAT, "dd/MM HH:mm");
        yml.addDefault(Messages.HISTORY_RESULT_WIN, "&aWin");
        yml.addDefault(Messages.HISTORY_RESULT_LOSE, "&bLoss");
        yml.addDefault(Messages.PLAYER_HISTORY_NO_DATA, "%bw_lang_prefix%&bNo match history found.");
        yml.addDefault(Messages.PLAYER_HISTORY_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bPlayer not found.");
        yml.addDefault(Messages.PLAYER_HISTORY_VIEW_OTHER_DENIED, "%bw_lang_prefix%&bYou don't have permission to view other players.");
        yml.addDefault(Messages.PLAYER_STATS_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bPlayer not found.");

        /* Lobby Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "stats"), "&bStats");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&fRight-click to see your stats!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "history"), "&bMatch History");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "history"), Collections.singletonList("&fRight-click to see your match history!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "arena-selector"), "&bArena Selector");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "arena-selector"), Collections.singletonList("&fRight-click to choose an arena!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "leave"), "&bBack to Hub");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fRight-click to leave BedWars!"));
        /* Pre Game Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "stats"), "&bStats");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&fRight-click to see your stats!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "leave"), "&bBack to Lobby");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fRight-click to leave the arena!"));
        /* Spectator Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "teleporter"), "&bTeleporter");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "leave"), "&bBack to Lobby");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fRight-click to leave the arena!"));

        /* save default items messages for stats gui */
        yml.addDefault(Messages.PLAYER_STATS_GUI_INV_NAME, "&f%bw_player% &aStats");
        addDefaultStatsMsg(yml, "overall", "&aOverall",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%",
                "&fWLR: &b%bw_wlr%",
                "",
                "&fFirst Play: &b%bw_play_first%",
                "&fLast Play: &b%bw_play_last%");
        addDefaultStatsMsg(yml, "solo", "&aSolo",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "doubles", "&aDoubles",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "triples", "&aTriples",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "quads", "&aQuads",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "one-vs-one", "&a1v1",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "two-vs-two", "&a2v2",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "three-vs-three", "&a3v3",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "four-vs-four", "&a4v4",
                "&fGames: &b%bw_games_played%",
                "&fWins: &b%bw_wins%",
                "&fLosses: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fDeaths: &b%bw_deaths%",
                "&fFinal Kills: &b%bw_final_kills%",
                "&fFinal Deaths: &b%bw_final_deaths%",
                "&fBeds Broken: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");

        /* history gui */
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_INV_NAME, "&fHistory: %bw_target%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_ENTRY_NAME, "&bMatch #%bw_history_index%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_ENTRY_LORE, Arrays.asList(
                "&fMap: &f%bw_history_map%",
                "&fMode: &f%bw_history_mode%",
                "&fPlacement: &f%bw_history_placement%",
                "&fResult: %bw_history_result%",
                "&fKills: &f%bw_history_kills%",
                "&fFinal Kills: &f%bw_history_final_kills%",
                "&fTotal Kills: &f%bw_history_total_kills%",
                "&fDeaths: &f%bw_history_deaths%",
                "&fFinal Deaths: &f%bw_history_final_deaths%",
                "&fBeds: &f%bw_history_beds_destroyed%",
                "&fDuration: &f%bw_history_duration%",
                "&fDate: &f%bw_history_datetime%",
                "",
                "&aClick to view match events."
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_EMPTY_NAME, "&bNo matches");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_EMPTY_LORE, Collections.singletonList("&fPlay a match to see it here."));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_NEXT_NAME, "&aNext Page");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_NEXT_LORE, Collections.singletonList("&fPage %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PREV_NAME, "&bPrevious Page");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PREV_LORE, Collections.singletonList("&fPage %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PAGE_NAME, "&bPage %bw_history_page%/%bw_history_pages%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PAGE_LORE, Collections.singletonList("&fTotal matches: &f%bw_history_total%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_FILLER_NAME, " ");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_FILLER_LORE, Collections.emptyList());

        /* history events gui */
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_NO_DATA, "%bw_lang_prefix%&bNo match events found.");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_NO_PERMISSION, "%bw_lang_prefix%&bYou don't have permission to view advanced match events.");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_INV_NAME, "&fMatch Events");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_NAME, "&bNo events");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_LORE, Collections.singletonList("&fNo events logged for this match."));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_NAME, "&aNext Page");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_LORE, Collections.singletonList("&fPage %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_NAME, "&bPrevious Page");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_LORE, Collections.singletonList("&fPage %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_NAME, "&bPage %bw_history_page%/%bw_history_pages%");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_LORE, Collections.singletonList("&fTotal events: &f%bw_history_total%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_NAME, "&aBack");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_LORE, Collections.singletonList("&fReturn to match list."));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_NAME, " ");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_LORE, Collections.emptyList());
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_DEFAULT_NAME, "&bEvent #%bw_history_event_index%");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_DEFAULT_LORE, Arrays.asList(
                "&fType: &f%bw_event_type%",
                "&fTime: &f%bw_event_time%",
                "&fActor: &f%bw_event_actor%",
                "&fTarget: &f%bw_event_target%",
                "&fTeam: %bw_event_team_color%%bw_event_team%",
                "&fMeta: &f%bw_event_meta%"
        ));

        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "game-start"), "&aGame Started");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "game-start"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fState: &f%bw_event_meta_state%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "game-end"), "&bGame Ended");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "game-end"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fWinner Team: &f%bw_event_meta_winner_team%",
                "&fWinners: &f%bw_event_meta_winners_count%",
                "&fLosers: &f%bw_event_meta_losers_count%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-join"), "&aPlayer Joined");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-join"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fSpectator: &f%bw_event_meta_spectator%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-leave"), "&bPlayer Left");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-leave"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fSpectator: &f%bw_event_meta_spectator%",
                "&fLast Damager: &f%bw_event_meta_last_damager%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-rejoin"), "&aPlayer Rejoined");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-rejoin"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fRespawn Time: &f%bw_event_meta_respawn_time%s"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-respawn"), "&bPlayer Respawned");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-respawn"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-first-spawn"), "&bPlayer First Spawn");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-first-spawn"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-kill"), "&bKill");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-kill"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fKiller: &f%bw_event_actor%",
                "&fVictim: &f%bw_event_target%",
                "&fCause: &f%bw_event_meta_cause%",
                "&fFinal Kill: &f%bw_event_meta_final_kill%",
                "&fVictim Team: &f%bw_event_meta_victim_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "bed-break"), "&bBed Destroyed");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "bed-break"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fVictim Team: &f%bw_event_meta_victim_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "team-eliminated"), "&bTeam Eliminated");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "team-eliminated"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "team-assign"), "&bTeam Assigned");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "team-assign"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "upgrade-buy"), "&bUpgrade Bought");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "upgrade-buy"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fUpgrade: &f%bw_event_meta_upgrade%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "shop-buy"), "&aShop Purchase");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "shop-buy"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fItem: &f%bw_event_meta_item%",
                "&fCategory: &f%bw_event_meta_category%",
                "&fSlot: &f%bw_event_meta_slot%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "shop-open"), "&aShop Opened");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "shop-open"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "generator-upgrade"), "&bGenerator Upgrade");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "generator-upgrade"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fGenerator: &f%bw_event_meta_generator%",
                "&fAmount: &f%bw_event_meta_amount%",
                "&fDelay: &f%bw_event_meta_delay%s"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "next-event"), "&bNext Event");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "next-event"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPrevious: &f%bw_event_meta_previous_event%",
                "&fNext: &f%bw_event_meta_next_event%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "dream-defender"), "&aDream Defender");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "dream-defender"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "bed-bug"), "&aBed Bug");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "bed-bug"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "invisibility"), "&bInvisibility");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "invisibility"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fType: &f%bw_event_meta_type%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "egg-bridge-throw"), "&bEgg Bridge Thrown");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "egg-bridge-throw"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "egg-bridge-build"), "&bEgg Bridge Build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "egg-bridge-build"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fBlock: &f%bw_event_meta_block%",
                "&fTeam Color: &f%bw_event_meta_team_color%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "popup-tower-place"), "&bPop-up Tower Placed");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "popup-tower-place"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "popup-tower-build"), "&bPop-up Tower Build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "popup-tower-build"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fBlock: &f%bw_event_meta_block%",
                "&fTeam Color: &f%bw_event_meta_team_color%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "item-deposit"), "&bItem Deposited");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "item-deposit"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fContainer: &f%bw_event_meta_container%",
                "&fItem: &f%bw_event_meta_item%",
                "&fAmount: &f%bw_event_meta_amount%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "base-enter"), "&aBase Enter");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "base-enter"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "base-leave"), "&bBase Leave");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "base-leave"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fTeam: %bw_event_team_color%%bw_event_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "resource-collect"), "&bResource Collect");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "resource-collect"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fItem: &f%bw_event_meta_item%",
                "&fAmount: &f%bw_event_meta_amount%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "resource-drop"), "&bResource Drop");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "resource-drop"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fGenerator: &f%bw_event_meta_generator%",
                "&fItem: &f%bw_event_meta_item%",
                "&fAmount: &f%bw_event_meta_amount%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "stat-change"), "&bStat Change");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "stat-change"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fStat: &f%bw_event_meta_stat%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "xp-gain"), "&bXP Gain");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "xp-gain"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fXP: &f%bw_event_meta_xp%",
                "&fSource: &f%bw_event_meta_source%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "money-gain"), "&bMoney Gain");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "money-gain"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fMoney: &f%bw_event_meta_money%",
                "&fSource: &f%bw_event_meta_source%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "level-up"), "&bLevel Up");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "level-up"), Arrays.asList(
                "&fTime: &f%bw_event_time%",
                "&fPlayer: &f%bw_event_actor%",
                "&fLevel: &f%bw_event_meta_level%",
                "&fNext XP: &f%bw_event_meta_next_xp%"
        ));

        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "game-start"), "Game Start");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "game-end"), "Game End");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-join"), "Player Join");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-leave"), "Player Leave");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-rejoin"), "Player Rejoin");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-respawn"), "Player Respawn");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-first-spawn"), "Player First Spawn");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-kill"), "Kill");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "bed-break"), "Bed Break");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "team-eliminated"), "Team Eliminated");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "team-assign"), "Team Assign");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "upgrade-buy"), "Upgrade Buy");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "shop-buy"), "Shop Buy");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "shop-open"), "Shop Open");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "generator-upgrade"), "Generator Upgrade");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "next-event"), "Next Event");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "dream-defender"), "Dream Defender");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "bed-bug"), "Bed Bug");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "invisibility"), "Invisibility");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "egg-bridge-throw"), "Egg Bridge Throw");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "egg-bridge-build"), "Egg Bridge Build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "popup-tower-place"), "Pop-up Tower Place");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "popup-tower-build"), "Pop-up Tower Build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "item-deposit"), "Item Deposit");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "base-enter"), "Base Enter");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "base-leave"), "Base Leave");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "resource-collect"), "Resource Collect");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "resource-drop"), "Resource Drop");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "stat-change"), "Stat Change");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "xp-gain"), "XP Gain");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "money-gain"), "Money Gain");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "level-up"), "Level Up");

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_WAITING, Arrays.asList("&f&lBED WARS", "&f%bw_date% &f%bw_server_id%", "", "&fMap: &a%bw_map%", "", "&fPlayers: &a%bw_on%/%bw_max%", "", "&fWaiting...", "", "&fMode: &a%bw_group%", "&fVersion: &f%bw_version%", "", "&b%bw_server_ip%"));
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_STARTING, Arrays.asList("&f&lBED WARS", "&f%bw_date% &f%bw_server_id%", "", "&fMap: &a%bw_map%", "", "&fPlayers: &a%bw_on%/%bw_max%", "", "&fStarting in &a%bw_time%s", "", "&fMode: &a%bw_group%", "&fVersion: &f%bw_version%", "", "&b%bw_server_ip%"));
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING, Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% in &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "%bw_team_5%", "%bw_team_6%", "%bw_team_7%", "%bw_team_8%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.Doubles.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% in &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "%bw_team_5%", "%bw_team_6%", "%bw_team_7%", "%bw_team_8%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.3v3v3v3.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% in &a%bw_time%}", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "", "&fKills: &a%bw_kills%", "&fFinal Kills: &a%bw_final_kills%", "&fBeds Broken: &a%bw_beds%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.4v4v4v4.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% in &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "", "&fKills: &a%bw_kills%", "&fFinal Kills: &a%bw_final_kills%", "&fBeds Broken: &a%bw_beds%", "", "&b%bw_server_ip%"));

        yml.addDefault(Messages.SCOREBOARD_LOBBY, Arrays.asList("&b&lBedWars,&b&lB&b&ledWars,&b&lB&b&le&b&ldWars,&b&lB&b&le&b&ld&b&lWars,&b&lBe&b&ld&b&lW&b&lars,&b&lBed&b&lW&b&la&b&lrs,&b&lBedW&b&la&b&lr&b&ls,&b&lBedWa&b&lr&b&ls,&b&lBedWar&b&ls,&b&lBedWars",
                "&fYour Level: %bw_level%", "", "&fProgress: &a%bw_current_xp%&f/&b%bw_required_xp%", "%bw_progress%", "", "&f%player%", "", "&fCoins: &a%bw_money%", "", "&fTotal Wins: &a%bw_wins%", "&fTotal Kills: &a%bw_kills%", "", "&b%bw_server_ip%"));

        //
        yml.addDefault(Messages.SHOP_INDEX_NAME, "&fQuick Buy");
        yml.addDefault(Messages.SHOP_QUICK_ADD_NAME, "&fAdding to Quick Buy...");
        yml.addDefault(Messages.SHOP_INSUFFICIENT_MONEY, "%bw_lang_prefix%&bYou don't have enough %bw_currency%! Need %bw_amount% more!");
        yml.addDefault(Messages.SHOP_NEW_PURCHASE, "%bw_lang_prefix%&aYou purchased &b%bw_item%");
        yml.addDefault(Messages.SHOP_ALREADY_BOUGHT, "%bw_lang_prefix%&bYou've already bought that!");
        yml.addDefault(Messages.SHOP_ALREADY_HIGHER_TIER, "%bw_lang_prefix%&bYou already have a higher tier item.");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME, "%bw_time_bar%");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME, "%bw_team_color%%bw_despawn_time%s &f[ %bw_team_color%%bw_health%&f]");
        yml.addDefault(Messages.SHOP_SEPARATOR_NAME, "&f⇧ Categories");
        yml.addDefault(Messages.SHOP_SEPARATOR_LORE, Collections.singletonList("&f⇩ Items"));
        yml.addDefault(Messages.SHOP_QUICK_BUY_NAME, "&bQuick Buy");
        yml.addDefault(Messages.SHOP_QUICK_BUY_LORE, new ArrayList<>());
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_NAME, "&bEmpty slot!");
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_LORE, Arrays.asList("&fThis is a Quick Buy Slot!", "&bSneak Click &fany item in", "&fthe shop to add it here."));
        yml.addDefault(Messages.SHOP_CAN_BUY_COLOR, "&a");
        yml.addDefault(Messages.SHOP_CANT_BUY_COLOR, "&b");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CAN_BUY, "&bClick to purchase!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CANT_AFFORD, "&bYou don't have enough %bw_currency%!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_MAXED, "&aMAXED!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_ARMOR, "&aEQUIPPED!");
        yml.addDefault(Messages.SHOP_LORE_QUICK_ADD, "&bSneak Click to add to Quick Buy");
        yml.addDefault(Messages.SHOP_LORE_QUICK_REMOVE, "&bSneak Click to remove from Quick Buy!");


        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "&fBlocks", "&aBlocks", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "wool", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Wool", Arrays.asList("&fCost: &f%bw_cost% %bw_currency%", "", "&fGreat for bridging across", "&fislands. Turns into your team's",
                "&fcolor.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "clay", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Hardened Clay", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fBasic block to defend your bed.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "glass", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Blast-Proof Glass", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fImmune to explosions.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "stone", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%End Stone", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fSolid block to defend your bed.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "ladder", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Ladder", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fUseful to save cats stuck in", "&ftrees.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "obsidian", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Obsidian", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fExtreme protection for your bed.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "wood", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Wood", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fSolid block to defend your bed", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_MELEE, "&fMelee", "&aMelee", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "stone-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Stone Sword", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "iron-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Iron Sword", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "diamond-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Diamond Sword", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "stick", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Stick (KnockBack I)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "&fArmor", "&aArmor", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "chainmail", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Permanent Chainmail Armor", Arrays.asList("&fCost: %bw_cost% %bw_currency%",
                "", "&fChainmail leggings and boots", "&fwhich you will always spawn", "&fwith.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "iron-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Permanent Iron Armor", Arrays.asList("&fCost: %bw_cost% %bw_currency%",
                "", "&fIron leggings and boots which", "&fyou will always spawn with.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "diamond-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Permanent Diamond Armor", Arrays.asList("&fCost: %bw_cost% %bw_currency%",
                "", "&fDiamond leggings and boots which", "&fyou will always crush with.", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "&fTools", "&aTools", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "shears", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Permanent Shears", Arrays.asList("&fCost: %bw_cost% %bw_currency%",
                "", "&fGreat to get rid of wool. You", "&fwill always spawn with these shears.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "pickaxe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Pickaxe %bw_tier%", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "&fTier: &b%bw_tier%",
                "", "&fThis is an upgradable item.", "&fIt will lose 1 tier upon.", "&fdeath!", "", "&fYou will permanently", "&frespawn with at least the", "&flowest tier.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "axe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Axe %bw_tier%", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "&fTier: &b%bw_tier%",
                "", "&fThis is an upgradable item.", "&fIt will lose 1 tier upon.", "&fdeath!", "", "&fYou will permanently", "&frespawn with at least the", "&flowest tier.", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_RANGED, "&fRanged", "&aRanged", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "arrow", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Arrow", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow1", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Bow", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow2", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Bow (Power I)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow3", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Bow (Power I, Punch I)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "&fPotions", "&aPotions", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "speed-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Speed II Potion (45 seconds)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "jump-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Jump V Potion (45 seconds)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "invisibility", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Invisibility Potion (30 seconds)", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "&fUtility", "&aUtility", Collections.singletonList("&bClick to view!"));

        addContentMessages(yml, "golden-apple", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Golden Apple", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fWell-rounded healing.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bedbug", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%BedBug", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fSpawns silverfish where the",
                "&fsnowball lands to distract your", "&fenemies. Lasts 15 seconds.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "dream-defender", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Dream Defender", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fIron Golem to help defend your",
                "&fbase. Lasts 4 minutes.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "fireball", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Fireball", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fRight-click to launch! Great to",
                "&fknock back enemies walking on", "&fthin bridges", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "tnt", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%TNT", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fInstantly ignites, appropriate",
                "&fto explode things!", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "ender-pearl", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Ender Pearl", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fThe quickest way to invade enemy",
                "&fbases.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "water-bucket", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Water Bucket", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fGreat to slow down approaching",
                "&fenemies. Can also protect", "&fagainst TNT.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bridge-egg", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Bridge Egg", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fThis egg creates a bridge in its",
                "&ftrial after being thrown.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "magic-milk", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Magic Milk", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fAvoid triggering traps for 60",
                "&fseconds after consuming.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "sponge", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Sponge", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fGreat for soaking up water.",
                "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "tower", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Compact Pop-up Tower", Arrays.asList("&fCost: %bw_cost% %bw_currency%", "", "&fPlace a compact pop-up", "&ftower defense!", "", "%bw_quick_buy%", "%bw_buy_status%"));

        //
        yml.addDefault(Messages.MEANING_NO_TRAP, "No trap!");
        yml.addDefault(Messages.FORMAT_UPGRADE_TRAP_COST, "&fCost: %bw_currency_color%%bw_cost% %bw_currency%");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD, "&b");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD, "&b");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_UNLOCKED, "&a");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_LOCKED, "&f");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_UNLOCKED, "&a");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY, "%bw_color%Click to purchase!");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY, "%bw_color%You don't have enough %bw_currency%");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_SPACE, "&bYou don't have enough inventory space to buy this item!");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_LOCKED, "&bLOCKED");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_UNLOCKED, "%bw_color%UNLOCKED");
        yml.addDefault(Messages.UPGRADES_UPGRADE_BOUGHT_CHAT, "&a%bw_player% purchased &b%bw_upgrade_name%");
        yml.addDefault(Messages.UPGRADES_UPGRADE_ALREADY_CHAT, "&bYou already unlocked this upgrade!");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-1"), "%bw_color%Iron Forge");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "forge"),
                Arrays.asList("&fUpgrade resource spawning on", "&fyour island.", "", "{tier_1_color}Tier 1: +50% Resources, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Tier 2: +100% Resources, &b{tier_2_cost} {tier_2_currency}",
                        "{tier_3_color}Tier 3: Spawn emeralds, &b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}Tier 4: +200% Resources, &b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-2"), "%bw_color%Golden Forge");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-3"), "%bw_color%Emerald Forge");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-4"), "%bw_color%Molten Forge");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + "traps", "&bBuy a trap");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + "traps", Arrays.asList("&fPurchased traps will be", "&fqueued on the right.", "", "&bClick to browse!"));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "swords").replace("%bw_tier%", "tier-1"), "%bw_color%Sharpened Swords");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "swords"),
                Arrays.asList("&fYour team permanently gains", "&fSharpness I on all swords and", "&faxes!", "", "{tier_1_color}Cost: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-1"), "%bw_color%Reinforced Armor I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "armor"),
                Arrays.asList("&fYour team permanently gains", "&fProtection on all armor pieces!", "", "{tier_1_color}Tier 1: Protection I, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Tier 2: Protection II, &b{tier_2_cost} {tier_2_currency}",
                        "{tier_3_color}Tier 3: Protection III, &b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}Tier 4: Protection IV, &b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-2"), "%bw_color%Reinforced Armor II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-3"), "%bw_color%Reinforced Armor III");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-4"), "%bw_color%Reinforced Armor IV");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "miner").replace("%bw_tier%", "tier-1"), "%bw_color%Maniac Miner I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "miner"),
                Arrays.asList("&fAll players on your team", "&fpermanently gain Haste.", "", "{tier_1_color}Tier 1: Haste I, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Tier 2: Haste II, &b{tier_2_cost} {tier_2_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "miner").replace("%bw_tier%", "tier-2"), "%bw_color%Maniac Miner II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "heal-pool").replace("%bw_tier%", "tier-1"), "%bw_color%Heal Pool");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "heal-pool"),
                Arrays.asList("&fCreates a Regeneration field", "&faround yor base!", "", "{tier_1_color}Cost: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "dragon").replace("%bw_tier%", "tier-1"), "%bw_color%Dragon Buff");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "dragon"),
                Arrays.asList("&fYour team will have 2 dragons", "&finstead of 1 during deathmatch!", "", "{tier_1_color}Cost: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "glass", "&f⬆&fPurchasable");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "glass", Collections.singletonList("&f⬇&fTraps Queue"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "first", "%bw_color%Trap #1: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "first", Arrays.asList("&fThe first enemy to walk", "&finto your base will trigger", "&fthis trap!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "first",
                Arrays.asList("", "&fPurchasing a trap will", "&fqueue it here. Its cost", "&fwill scale based on the", "&fnumber of traps queued.", "", "&fNext trap: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "second", "%bw_color%Trap #2: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "second", Arrays.asList("&fThe second enemy to walk", "&finto your base will trigger", "&fthis trap!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "second",
                Arrays.asList("", "&fPurchasing a trap will", "&fqueue it here. Its cost", "&fwill scale based on the", "&fnumber of traps queued.", "", "&fNext trap: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "third", "%bw_color%Trap #3: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "third", Arrays.asList("&fThe third enemy to walk", "&finto your base will trigger", "&fthis trap!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "third",
                Arrays.asList("", "&fPurchasing a trap will", "&fqueue it here. Its cost", "&fwill scale based on the", "&fnumber of traps queued.", "", "&fNext trap: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "1", "%bw_color%It's a trap!");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "1", Arrays.asList("&fInflicts Blindness and Slowness", "&ffor 5 seconds.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "2", "%bw_color%Counter-Offensive Trap");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "2", Arrays.asList("&fGrants Speed I for 15 seconds to", "&fallied players near your base.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "3", "%bw_color%Alarm Trap");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "3", Arrays.asList("&fReveales invisible players as", "&fwell as their name and team.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "4", "%bw_color%Miner Fatigue Trap");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "4", Arrays.asList("&fInflict Mining Fatigue for 10", "&fseconds.", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "back", "&aBack");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "back", Collections.singletonList("&fTo Upgrades & Traps"));
        yml.addDefault(Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + "traps", "&fQueue a trap");
        yml.addDefault(Messages.UPGRADES_TRAP_QUEUE_LIMIT, "&bTrap queue full!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_MSG, "&b&l%bw_trap% was set off!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_TITLE, "&bTRAP TRIGGERED!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_SUBTITLE, "&fYour %bw_trap% has been triggered!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_MSG + "3", "&b&lAlarm trap set off by &f&l%bw_player% &b&lfrom %bw_color%&l%bw_team% &b&lteam!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_TITLE + "3", "&b&lALARM!!!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + "3", "&fAlarm trap set off by %bw_color%%bw_team% &fteam!");
        yml.set(Messages.COMMAND_MAIN, Arrays.asList(
                "",
                "&a- &f/stats",
                "&a- &f/historico",
                "&a- &f/" + BedWars.mainCmd + " join &o<arena/group>",
                "&a- &f/" + BedWars.mainCmd + " leave",
                "&a- &f/" + BedWars.mainCmd + " lang",
                "&a- &f/" + BedWars.mainCmd + " gui",
                "&a- &f/" + BedWars.mainCmd + " start &b(vip)"
        ));
        LanguageEncodingRepair.repair(yml);
        generateNPCMessages(yml, "default");
        save();
        setPrefix(m(Messages.PREFIX));
        setPrefixStatic(m(Messages.PREFIX));
    }

    @Override
    public void generateNPCMessages(YamlConfiguration yml, String group) {
        yml.addDefault(Messages.NPC_NAME_TEAM_UPGRADES.replace("%group%", group), Arrays.asList("&bTEAM UPGRADES", "&b&lRIGHT CLICK"));
        yml.addDefault(Messages.NPC_NAME_SOLO_UPGRADES.replace("%group%", group), Arrays.asList("&bSOLO UPGRADES", "&b&lRIGHT CLICK"));
        yml.addDefault(Messages.NPC_NAME_TEAM_SHOP.replace("%group%", group), Arrays.asList("&bTEAM SHOP", "&b&lRIGHT CLICK"));
        yml.addDefault(Messages.NPC_NAME_SOLO_SHOP.replace("%group%", group), Arrays.asList("&bITEM SHOP", "&b&lRIGHT CLICK"));
    }
}

