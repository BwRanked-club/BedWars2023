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

public class Portuguese extends Language {

    public Portuguese() {
        super(BedWars.plugin, "pt");
        YamlConfiguration yml = getYml();
        yml.options().copyDefaults(true);
        yml.addDefault(Messages.PREFIX, "");
        yml.addDefault("name", "Português");

        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_LOBBY, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_WAITING, "&a%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_STARTING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_PLAYING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_RESTARTING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_HEADER_SPECTATOR, "&b%bw_server_ip%");

        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_LOBBY, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_WAITING, "&a%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_STARTING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_PLAYING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_RESTARTING, "&b%bw_server_ip%");
        yml.addDefault(Messages.FORMATTING_SIDEBAR_TAB_FOOTER_SPECTATOR, "&b%bw_server_ip%");

        yml.addDefault(Messages.COMMAND_MAIN, Arrays.asList("", "&a▪ &f/" + BedWars.mainCmd + " stats", "&a▪ &f/" + BedWars.mainCmd + " historico", "&a▪ &f/" + BedWars.mainCmd + " join &o<arena/grupo>", "&a▪ &f/" + BedWars.mainCmd + " leave", "&a▪ &f/" + BedWars.mainCmd + " lang", "&a▪ &f/" + BedWars.mainCmd + " gui", "&a▪ &f/" + BedWars.mainCmd + " start &b(vip)"));
        yml.addDefault(Messages.COMMAND_LANG_LIST_HEADER, "%bw_lang_prefix% &aIdiomas disponíveis:");
        yml.addDefault(Messages.COMMAND_LANG_LIST_FORMAT, "&a▪  &f%bw_lang_iso% - &f%bw_name%");
        yml.addDefault(Messages.COMMAND_LANG_USAGE, "%bw_lang_prefix%&fUse: /lang &f&o<linguagem>");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_NOT_EXIST, "%bw_lang_prefix%&bEsta linguagem não existe!");
        yml.addDefault(Messages.COMMAND_LANG_SELECTED_SUCCESSFULLY, "%bw_lang_prefix%&aIdioma alterado!");
        yml.addDefault(Messages.COMMAND_LANG_USAGE_DENIED, "%bw_lang_prefix%&bVocê não pode mudar o idioma durante a partida.");
        yml.addDefault(Messages.COMMAND_JOIN_USAGE, "§a▪ �&fUse: /" + BedWars.mainCmd + " join §o<arena/grupo>");
        yml.addDefault(Messages.COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND, "%bw_lang_prefix%&bNão existe nenhuma arena ou grupo de arena chamado: %bw_name%");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL, "%bw_lang_prefix%&bEsta arena está cheia!\n&aVocê pode adquirir vantagens doando. &f&o(clique)");
        yml.addDefault(Messages.COMMAND_JOIN_NO_EMPTY_FOUND, "%bw_lang_prefix%&bNão há nenhuma arena disponível no momento");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS, "%bw_lang_prefix%&bPedimos desculpas, mas esta arena está cheia.\n&bSabemos que é um doador, mas esta arena já está cheia de staffs e/ou doadores.");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_PARTY_TOO_BIG, "%bw_lang_prefix%&bA sua party é muito grande para poder entrar na arena. :(");
        yml.addDefault(Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER, "%bw_lang_prefix%&bApenas o líder da party pode escolher a arena.");
        yml.addDefault(Messages.COMMAND_JOIN_PLAYER_JOIN_MSG, "%bw_lang_prefix%&f%bw_player% &bentrou (&b%bw_on%&b/&b%bw_max%&b)!");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_MSG, "%bw_lang_prefix%�&bVocê agora está assistindo �&b%bw_arena%�&b.\n%bw_lang_prefix%�&bVocê pode sair da arena a qualquer momento com �&b/leave�&b.");
        yml.addDefault(Messages.COMMAND_JOIN_SPECTATOR_DENIED_MSG, "&bEspectadores não são permitidos nesta arena!");
        yml.addDefault(Messages.COMMAND_TP_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bJogador não encontrado!");
        yml.addDefault(Messages.COMMAND_TP_NOT_IN_ARENA, "%bw_lang_prefix%&bEste jogador não está em uma arena de bedwars!");
        yml.addDefault(Messages.COMMAND_TP_NOT_STARTED, "%bw_lang_prefix%&bA arena onde o jogador está ainda não começou!");
        yml.addDefault(Messages.COMMAND_TP_USAGE, "%bw_lang_prefix%&bUse: /bw tp <jogador>");
        yml.addDefault(Messages.REJOIN_NO_ARENA, "%bw_lang_prefix%&bNão tem partidas para você se reconectar.");
        yml.addDefault(Messages.REJOIN_DENIED, "%bw_lang_prefix%&bVocê não pode mais se juntar à arena. Jogo terminou ou cama destruída.");
        yml.addDefault(Messages.REJOIN_ALLOWED, "%bw_lang_prefix%&bEntrando na arena &a%bw_arena%&b!");
        yml.addDefault(Messages.COMMAND_REJOIN_PLAYER_RECONNECTED, "%bw_lang_prefix%&f%bw_player% &bse reconectou!");
        yml.addDefault(Messages.COMMAND_LEAVE_DENIED_NOT_IN_ARENA, "%bw_lang_prefix%&bVocê não está em uma arena!");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_TITLE, "Você tem certeza?");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_STAY, "&bNão");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_STAY_LORE, List.of("&fFicar na arena"));
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_BRING_PARTY, "&aSim");
        yml.addDefault(Messages.COMMAND_LEAVE_HAS_PARTY_POPUP_BRING_PARTY_LORE, List.of("&fPuxar a party com você"));
        yml.addDefault(Messages.COMMAND_LEAVE_MSG, "%bw_lang_prefix%&f%bw_player% &bsaiu (&b%bw_on%&b/&b%bw_max%&b)!");
        yml.addDefault(Messages.COMMAND_NOT_ALLOWED_IN_GAME, "%bw_lang_prefix%&bVocê não pode fazer isso durante o jogo.");
        yml.addDefault(Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS, "%bw_lang_prefix%&bComando não encontrado ou você não tem permissão!");
        yml.addDefault(Messages.COMMAND_PARTY_HELP, Arrays.asList("&b▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&aComandos da Party:",
                "&b/party help &f- &bIrá mostrar essa mensagem",
                "&b/party invite <player> &f- &bConvida o jogador para sua party",
                "&b/party leave &f- &bSaia da sua party",
                "&b/party info &f- &bShow party members and owner",
                "&b/party promote <jogador> &f- &bTransfer party ownership",
                "&b/party remove <jogador> &f- &bRemova o jogador da party",
                "&b/party accept <jogador> &f- &bAceite um pedido de party",
                "&b/party disband &f- &bRecuse um pedido de party")
        );
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_USAGE, "%bw_lang_prefix%&bUse: &f/party invite <jogador>");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE, "%bw_lang_prefix%&f%bw_player% &bnão está online.");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT, "%bw_lang_prefix%&bPedido enviado para &f%bw_player%&b.");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG, "%bw_lang_prefix%&b%bw_player% &bconvidou você para uma party! &o&f(Clique para aceitar)");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF, "%bw_lang_prefix%&bVocê não pode se convidar!");
        yml.addDefault(Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE, "%bw_lang_prefix%&f%bw_player% &bnão está online.");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE, "%bw_lang_prefix%&bVocê não tem solicitações de party.");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY, "%bw_lang_prefix%&bVocê já está em uma party!");
        yml.addDefault(Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS, "%bw_lang_prefix%&bSomente o dono da party pode fazer isso!");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_USAGE, "%bw_lang_prefix%&bUse: &f/party accept <jogador>");
        yml.addDefault(Messages.COMMAND_PARTY_ACCEPT_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bentrou na party.");
        yml.addDefault(Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY, "%bw_lang_prefix%&bVocê não está em uma party.");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND, "%bw_lang_prefix%&bVocê não pode sair da sua party.\n&bTente usar: &b/party disband");
        yml.addDefault(Messages.COMMAND_PARTY_LEAVE_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bsaiu da party!");
        yml.addDefault(Messages.COMMAND_PARTY_DISBAND_SUCCESS, "%bw_lang_prefix%&bA party foi desfeita");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_USAGE, "%bw_lang_prefix%&fUse: &b/party remove <player>");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_SUCCESS, "%bw_lang_prefix%&f%bw_player% &bfoi removido da party.");
        yml.addDefault(Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER, "%bw_lang_prefix%&f%bw_player% &bnão está na sua party!");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_SUCCESS, "%bw_lang_prefix%&bVocê promoveu com sucesso %bw_player% a dono");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_OWNER, "%bw_lang_prefix%&bVocê foi promovido a dono do grupo");
        yml.addDefault(Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER, "%bw_lang_prefix%&f &b%bw_player% foi promovido a dono");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_OWNER, "\n%bw_lang_prefix%&bO dono do grupo é: &f%bw_party_owner%");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYERS, "%bw_lang_prefix%&bMembros do grupo são:");
        yml.addDefault(Messages.COMMAND_PARTY_INFO_PLAYER, "&f%bw_player%");
        yml.addDefault(Messages.COMMAND_PARTY_CHAT_ENABLED_DISABLED, "&b&lPARTY &f&l┃ &fO chat da party está %bw_party_chat_status% &f!");
        yml.addDefault(Messages.COMMAND_FORCESTART_NOT_IN_GAME, "�&b▪ �&fVocê não está jogando!");
        yml.addDefault(Messages.COMMAND_FORCESTART_SUCCESS, "�&b▪ �&fContagem regressiva encurtada!");
        yml.addDefault(Messages.COMMAND_FORCESTART_NO_PERM, "%bw_lang_prefix%&fVocê não pode inicializar uma partida.\n�&fPor favor, considere doar para obter vantagens VIP.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_USAGE, "%bw_lang_prefix%&fUso: &b/bw replacePlayer <arena> <jogador-antigo> <novo-jogador>");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_ARENA_NOT_FOUND, "%bw_lang_prefix%&fArena não encontrada.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_OLD_NOT_FOUND, "%bw_lang_prefix%&fO jogador atual do time não foi encontrado.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_NEW_NOT_FOUND, "%bw_lang_prefix%&fO novo jogador não foi encontrado.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_NOT_PLAYING, "%bw_lang_prefix%&fEsta arena não está em partida.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_OLD_NOT_ACTIVE, "%bw_lang_prefix%&fEsse jogador não está ativo em um time desta arena.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_NEW_BUSY, "%bw_lang_prefix%&fO novo jogador já está ocupado em outra partida.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_TEAM_FULL, "%bw_lang_prefix%&fO time alvo não tem vaga livre para a substituição.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_SAME_PLAYER, "%bw_lang_prefix%&fVocê precisa escolher dois jogadores diferentes.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_ERROR, "%bw_lang_prefix%&fNão foi possível substituir o jogador agora.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_SUCCESS, "%bw_lang_prefix%&aSubstituiu &f%bw_old_player_display% &apor &f%bw_new_player_display% &ano time &b%bw_team_name% &ana arena &b%bw_arena%&a.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_BROADCAST, "%bw_lang_prefix%&f%bw_old_player_display% &afoi substituído por &f%bw_new_player_display% &ano time &b%bw_team_name%&a.");
        yml.addDefault(Messages.COMMAND_REPLACE_PLAYER_REMOVED, "%bw_lang_prefix%&fVocê foi substituído por &b%bw_new_player_display% &fno time &b%bw_team_name%&f. Agora você é espectador.");
        yml.addDefault(Messages.COMMAND_COOLDOWN, "&bVocê não pode fazer isso ainda! Aguarde mais %bw_seconds% segundos!");
        yml.addDefault(Messages.COMMAND_SHOUT_DISABLE_SOLO, "&bGlobal é desativado nesse modo!");
        yml.addDefault(Messages.COMMAND_LEAVE_STARTED, "&a&lTeleportando você para o lobby em %bw_leave_delay% segundos... Clique-direito novamente para cancelar!");
        yml.addDefault(Messages.COMMAND_LEAVE_CANCELED, "&b&lTeleporte cancelado!");
        yml.addDefault(Messages.ARENA_JOIN_VIP_KICK, "%bw_lang_prefix%&bDesculpe, mas você foi expulso porque um doador entrou na arena.\n&aPor favor, considere doar para mais vantagens. &f&o(clique)");
        yml.addDefault(Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT, "%bw_lang_prefix%�&bNão há jogadores suficientes! Contagem regressiva parada!");
        yml.addDefault(Messages.ARENA_RESTART_PLAYER_KICK, "%bw_lang_prefix%&bA arena em que você estava está reiniciando.");
        yml.addDefault(Messages.ARENA_STATUS_PLAYING_NAME, "&bJogando");
        yml.addDefault(Messages.ARENA_STATUS_RESTARTING_NAME, "&bReiniciando");
        yml.addDefault(Messages.ARENA_STATUS_WAITING_NAME, "&aEsperando �&b%bw_full%");
        yml.addDefault(Messages.ARENA_STATUS_STARTING_NAME, "&bIniciando �&b%bw_full%");
        yml.addDefault(Messages.ARENA_GUI_INV_NAME, "&fClique para entrar!");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_NAME, "&a&l%bw_name%");
        yml.addDefault(Messages.ARENA_GUI_ARENA_CONTENT_LORE, Arrays.asList("", "&fStatus: %bw_arena_status%", "&fJogadores: &f%bw_on%&f/&f%bw_max%", "&fTipo: &a%bw_group%", "", "&aBotão esquerdo para entrar.", "&bBotão direito para assistir."));
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_NAME, "&r%bw_server_ip%");
        yml.addDefault(Messages.ARENA_GUI_SKIPPED_ITEM_LORE, Collections.emptyList());
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CHAT, "%bw_lang_prefix%&bO jogo começa em &b%bw_time% &bsegundos!");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE, "&a%bw_seconds%");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-5", "&b❺");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-4", "&b❹");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-3", "&b❸");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-2", "&b❷");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-1", "&b❶");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE, " ");
        yml.addDefault(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE, "&bAguardando mais jogadores...");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TITLE, "&aCOMEÇOU");
        yml.addDefault(Messages.ARENA_STATUS_START_PLAYER_TUTORIAL, Arrays.asList("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&f                                   &lBedWars", "",
                "&b&l    Proteja sua cama e destrua as camas inimigas.",
                "&b&l      Melhore você e sua equipe coletando",
                "&b&l   Ferro, Ouro, Esmeralda e Diamante de geradores",
                "&b&l             para adquirir as melhorias.", "",
                "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        yml.addDefault(Messages.ARENA_JOIN_DENIED_SELECTOR, "%bw_lang_prefix%&bDesculpe, mas você não pode participar desta partida neste momento. Use o botão direito do mouse para espectar.");
        yml.addDefault(Messages.ARENA_SPECTATE_DENIED_SELECTOR, "%bw_lang_prefix%&bDesculpe, mas você não pode espectar esta partida neste momento. Utilize o botão esquerdo para entrar na partida.");
        yml.addDefault(Messages.ARENA_JOIN_DENIED_NO_PROXY, "&bDesculpe, mas precisa entrar em uma arena usando BedWarsProxy. \n&bSe você quiser configurar uma arena, certifique-se de dar a si mesmo a permissão bw.setup para que você possa entrar no servidor diretamente!");
        yml.addDefault(Messages.ARENA_JOIN_DENIED_NO_TIME, "&bEste jogo já começou!");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_NAME, "&fTeletransportador");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME, "%bw_v_prefix%%bw_player%");
        yml.addDefault(Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE, Arrays.asList("&fVida: &f%bw_player_health%%", "&fFome: &f%bw_player_food%", "", "&fClique para teletransportar."));
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_NAME, "&b&lRetornar ao lobby");
        yml.addDefault(Messages.ARENA_SPECTATOR_LEAVE_ITEM_LORE, Collections.singletonList("&fClique com o botão direito para sair."));
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE, "&aEspectando: &f%bw_player%");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE, "&bUtilize o SHIFT para sair.");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE, "&bSaindo do modo espectador!");
        yml.addDefault(Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE, "");
        yml.addDefault(Messages.ARENA_LEAVE_PARTY_DISBANDED, "%bw_lang_prefix%�&bO dono da party saiu e a party foi desfeita.");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIER, "&bNível &b%bw_tier%");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND, "&b&lDiamante");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD, "&a&lEsmeralda");
        yml.addDefault(Messages.GENERATOR_HOLOGRAM_TIMER, "&bGera em &b%bw_seconds% &bsegundos.");
        yml.addDefault(Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT, "%bw_lang_prefix%Geradores de %bw_generator_type% &bmelhorados para nível &b%bw_tier%");
        yml.addDefault(Messages.FORMATTING_CHAT_LOBBY, "%bw_level%%bw_v_prefix%&f%bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_WAITING, "%bw_level%%bw_v_prefix%&f%bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_SHOUT, "%bw_level%%bw_v_prefix%&b[G] %bw_team_format% &f%bw_player%&f%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_TEAM, "%bw_level%%bw_v_prefix%&f%bw_team_format%&f %bw_player%%bw_v_suffix%: %bw_message%");
        yml.addDefault(Messages.FORMATTING_CHAT_SPECTATOR, "%bw_level%%bw_v_prefix%&f[ESPECTADOR] %bw_player%%bw_v_suffix%: %bw_message%");
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

        yml.addDefault(Messages.FORMATTING_SCOREBOARD_DATE, "dd/MM/yy");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_GENERIC, "%bw_team_color%%bw_team_letter%&f %bw_team_name%: %bw_team_status%");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ELIMINATED, "&b&l✘");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_BED_DESTROYED, "&a%bw_players_remaining%");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_TEAM_ALIVE, "&a&l✓");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_NEXEVENT_TIMER, "mm:ss");
        yml.addDefault(Messages.FORMATTING_SCOREBOARD_YOUR_TEAM, "&f VOCÊ");
        yml.addDefault(Messages.FORMATTING_ACTION_BAR_TRACKING, "&fRastreando: %bw_team% &f- Distância: %bw_distance%m");
        yml.addDefault(Messages.FORMATTING_BOSSBAR_DRAGON, "&fDragão de %bw_team%");
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
        yml.addDefault(Messages.FORMATTING_STATS_DATE_FORMAT, "dd/MM/yyyy HH:mm");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM, "%bw_team_color%[%bw_team_name%]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SHOUT, "&b[GLOBAL]");
        yml.addDefault(Messages.FORMAT_PAPI_PLAYER_TEAM_SPECTATOR, "&f[ESPECTADOR]");
        yml.addDefault(Messages.MEANING_FULL, "Lotado");
        yml.addDefault(Messages.MEANING_SHOUT, "global");
        yml.addDefault(Messages.MEANING_NOBODY, "Ninguém");
        yml.addDefault(Messages.MEANING_NEVER, "Nunca");
        yml.addDefault(Messages.MEANING_IRON_SINGULAR, "Ferro");
        yml.addDefault(Messages.MEANING_IRON_PLURAL, "Ferros");
        yml.addDefault(Messages.MEANING_GOLD_SINGULAR, "Ouro");
        yml.addDefault(Messages.MEANING_GOLD_PLURAL, "Ouros");
        yml.addDefault(Messages.MEANING_EMERALD_SINGULAR, "Esmeralda");
        yml.addDefault(Messages.MEANING_EMERALD_PLURAL, "Esmeraldas");
        yml.addDefault(Messages.MEANING_DIAMOND_SINGULAR, "Diamante");
        yml.addDefault(Messages.MEANING_DIAMOND_PLURAL, "Diamantes");
        yml.addDefault(Messages.MEANING_VAULT_SINGULAR, "$");
        yml.addDefault(Messages.MEANING_VAULT_PLURAL, "$");
        yml.addDefault(Messages.MEANING_ENABLED, "&aAtivado");
        yml.addDefault(Messages.MEANING_DISABLED, "&bDesativado");
        yml.addDefault(Messages.INTERACT_CANNOT_PLACE_BLOCK, "%bw_lang_prefix%&bVocê não pode colocar blocos aqui!");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_BLOCK, "%bw_lang_prefix%&bVocê só pode quebrar blocos colocados por um jogador!");
        yml.addDefault(Messages.INTERACT_FULL_CHEST, "%bw_lang_prefix%&bO baú está cheio!");
        yml.addDefault(Messages.INTERACT_CANNOT_BREAK_OWN_BED, "&bVocê não pode destruir sua própria cama!");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT, "\n&f&lCAMA DESTRUIDA > %bw_team_color%Cama do time %bw_team_name% &ffoi destruida por %bw_player_color%%bw_player%&f!\n");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT, "&bCAMA DESTRUIDA !");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT, "&fVocê não vai mais renascer!");
        yml.addDefault(Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM, "&f&lCAMA DESTRUIDA > &fSua cama foi destruida por %bw_player_color%%bw_player%&f!");
        yml.addDefault(Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED, "&bVocê não pode abrir este baú porque esse time não foi eliminado!");
        yml.addDefault(Messages.INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN, "&bSua invisibilidade foi removida pois você tomou dano!");
        yml.addDefault(Messages.INTERACT_MAGIC_MILK_REMOVED, "&bOs efeitos do seu leite acabaram!");
        yml.addDefault(Messages.ITEM_DEPOSITED_SUCCESSFULLY, "&fVocê guardou &b(x{AMOUNT}) {ITEM} &fno baú.");
        yml.addDefault(Messages.EGGBRIDGE_BUILD_LIMIT_WARNING, "&bYou are too close to the build limit!");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL, "%bw_player_color%%bw_player% &fcaiu no void.");
        yml.addDefault(Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL, "%bw_player_color%%bw_player% &fcaiu no void. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL, "%bw_player_color%%bw_player% &ffoi jogado no void por %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi jogado no void por %bw_killer_color%%bw_killer_name%&f. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_REGULAR, "%bw_player_color%%bw_player% &fdesconectou em combate com %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL, "%bw_player_color%%bw_player% &fdesconectou em combate com %bw_killer_color%%bw_killer_name%&f. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL, "%bw_player_color%%bw_player% &ffoi empurrado por %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi empurrado por %bw_killer_color%%bw_killer_name%&f. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL, "%bw_player_color%%bw_player% &ffoi atingido por uma TNT de %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi atingido por uma TNT de %bw_killer_color%%bw_killer_name%&f. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR, "%bw_player_color%%bw_player% &ffoi atingido por uma TNT.");
        yml.addDefault(Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi atingido por uma TNT. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_PVP_REGULAR_KILL, "%bw_player_color%%bw_player% &ffoi morto por %bw_killer_color%%bw_killer_name%&f.");
        yml.addDefault(Messages.PLAYER_DIE_PVP_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi morto por %bw_killer_color%%bw_killer_name%&f. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_REGULAR, "%bw_player_color%%bw_player% &fmorreu.");
        yml.addDefault(Messages.PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL, "%bw_player_color%%bw_player% &fmorreu. &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_REGULAR, "%bw_player_color%%bw_player% &ffoi flechado por %bw_killer_color%%bw_killer_name%&f!");
        yml.addDefault(Messages.PLAYER_DIE_SHOOT_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi flechado por %bw_killer_color%%bw_killer_name%&f! &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_REGULAR, "%bw_player_color%%bw_player% &ffoi morto por pela Traça de %bw_killer_color%%bw_killer_team_name%&f!");
        yml.addDefault(Messages.PLAYER_DIE_DEBUG_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi morto por pela Traça de %bw_killer_color%%bw_killer_team_name%&f! &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_REGULAR, "%bw_player_color%%bw_player% &ffoi morto pelo Golem do %bw_killer_color%%bw_killer_team_name%&f!");
        yml.addDefault(Messages.PLAYER_DIE_IRON_GOLEM_FINAL_KILL, "%bw_player_color%%bw_player% &ffoi morto pelo Golem do %bw_killer_color%%bw_killer_team_name%&f! &b&lKILL FINAL!");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_DIAMOND, "%bw_lang_prefix%&b+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_EMERALD, "%bw_lang_prefix%&a+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_IRON, "%bw_lang_prefix%&f+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.PLAYER_DIE_REWARD_GOLD, "%bw_lang_prefix%&b+%bw_amount% %bw_meaning%");
        yml.addDefault(Messages.ARENA_MAX_BUILD_LIMIT_REACHED, "&bLimite máximo de construção atingido!");
        yml.addDefault(Messages.ARENA_MIN_BUILD_LIMIT_REACHED, "&bLimite mínimo de construção atingido!");
        yml.addDefault(Messages.ARENA_FIREBALL_COOLDOWN, "&bAguarde mais %bw_cooldown%s para fazer isso novamente!");
        yml.addDefault(Messages.ARENA_IN_GAME_ANNOUNCEMENT, Arrays.asList("&b&lSe você for desconectado, use /reconectar para voltar ao jogo.", "&b&lNão é permitido formar alianças com o time adversário! Denuncie usando /report."));
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_TITLE, "&bVOCÊ MORREU!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_SUBTITLE, "&bRenascendo em &b%bw_time% &bsegundos.");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWN_CHAT, "%bw_lang_prefix%&bVocê irá renascer em &b%bw_time% &bsegundos.");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWNED_TITLE, "&aRENASCIDO!");
        yml.addDefault(Messages.PLAYER_DIE_ELIMINATED_CHAT, "%bw_lang_prefix%&bVocê foi eliminado!");
        yml.addDefault(Messages.PLAYER_DIE_RESPAWNED_TEXT, "%bw_lang_prefix%&bVocê renasceu!");
        yml.addDefault(Messages.PLAYER_HIT_BOW, "%bw_lang_prefix%%bw_player% &festá com &b%bw_damage_amount% &fde vida!");
        yml.addDefault(Messages.GAME_END_GAME_OVER_PLAYER_TITLE, "&b&lFIM DE JOGO!");
        yml.addDefault(Messages.GAME_END_VICTORY_PLAYER_TITLE, "&b&lVITÓRIA!");
        yml.addDefault(Messages.GAME_END_TEAM_WON_CHAT, "%bw_lang_prefix%%bw_team_color%%bw_team_name% &aganhou o jogo!");
        yml.addDefault(Messages.FORMATTING_EACH_WINNER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_FIRST_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_SECOND_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_THIRD_KILLER, "%bw_player%");
        yml.addDefault(Messages.GAME_END_TOP_PLAYER_CHAT, Arrays.asList(
                "&f&m----------------------------------------",
                "&b&lBED WARS",
                "&fVencedor: %bw_team_color%%bw_team_name%",
                "&fTempo de partida: &a%bw_game_time%",
                "",
                "&b&lTOP KILLS FINAIS:",
                "&b1º %bw_first_format% &f- &f%bw_first_kills%",
                "&b2º %bw_second_format% &f- &f%bw_second_kills%",
                "",
                "&b&lTOP CAMAS QUEBRADAS:",
                "&b1º %bw_bedbreaker_format% &f- &f%bw_beds%",
                "&f&m----------------------------------------"
        ));
        yml.addDefault(Messages.GAME_END_MAP_RATING_QUESTION, "%bw_lang_prefix%&fPor favor, classifique esse mapa de acordo com sua experiência. &b(%bw_map%)");
        yml.addDefault(Messages.GAME_END_MAP_RATING_STAR, "&f✯");
        yml.addDefault(Messages.GAME_END_MAP_RATING_HOVER, "&bClique para avaliar com &b%bw_rating% &bestrela(s)");
        yml.addDefault(Messages.GAME_END_MAP_RATING_SUCCESS, "%bw_lang_prefix%&aVocê avaliou o mapa &f%bw_map% &acom &b%bw_rating% &aestrela(s)!");
        yml.addDefault(Messages.GAME_END_MAP_RATING_NOT_AVAILABLE, "%bw_lang_prefix%&bVocê só pode avaliar o mapa no fim da partida.");
        yml.addDefault(Messages.GAME_END_MAP_RATING_INVALID, "%bw_lang_prefix%&bAvaliação inválida.");
        yml.addDefault(Messages.BED_HOLOGRAM_DEFEND, "&b&lDefenda sua cama!");
        yml.addDefault(Messages.BED_HOLOGRAM_DESTROYED, "&b&lSua cama foi destruída!");
        yml.addDefault(Messages.TEAM_ELIMINATED_CHAT, "\n&f&lEQUIPE ELIMINADA > &bO time %bw_team_color%%bw_team_name% &bfoi eliminado!\n");
        yml.addDefault(Messages.NEXT_EVENT_BEDS_DESTROY, "&fDestruição de Camas");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_II, "&fDiamante II");
        yml.addDefault(Messages.NEXT_EVENT_DIAMOND_UPGRADE_III, "&fDiamante III");
        yml.addDefault(Messages.NEXT_EVENT_DRAGON_SPAWN, "&fMorte Súbita");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_II, "&fEsmeraldas II");
        yml.addDefault(Messages.NEXT_EVENT_EMERALD_UPGRADE_III, "&fEsmeraldas III");
        yml.addDefault(Messages.NEXT_EVENT_GAME_END, "&bFim do jogo");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED, "&bCAMA DESTRUÍDA!");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED, "&fTodas as camas foram destruídas!");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED, "&b&lTodas as camas foram destruídas!");
        yml.addDefault(Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH, "&bMorte Súbita");
        yml.addDefault(Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH, "");
        yml.addDefault(Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH, "&bMORTE SÚBITA: &b&b%bw_dragons_amount% dragões do %bw_team_color%%bw_team_name%");
        yml.addDefault(Messages.XP_REWARD_PER_MINUTE, "%bw_lang_prefix%&b+%bw_xp% XP (Tempo de jogo).");
        yml.addDefault(Messages.XP_REWARD_WIN, "%bw_lang_prefix%&b+%bw_xp% XP (Vencer o jogo).");
        yml.addDefault(Messages.XP_REWARD_PER_TEAMMATE, "%bw_lang_prefix%&b+%bw_xp% XP (Suporte da equipe).");
        yml.addDefault(Messages.XP_REWARD_BED_DESTROY, "%bw_lang_prefix%&b+%bw_xp% XP (Bed Destroyed).");
        yml.addDefault(Messages.XP_REWARD_REGULAR_KILL, "%bw_lang_prefix%&b+%bw_xp% XP (Regular Kill).");
        yml.addDefault(Messages.XP_REWARD_FINAL_KILL, "%bw_lang_prefix%&b+%bw_xp% XP (Final Kill).");
        yml.addDefault(Messages.XP_REWARD_HALLOWEEN, "%bw_lang_prefix%&b+5 XP (Halloween).");
        yml.addDefault(Messages.PLAYER_LEVEL_UP, Collections.singletonList("&aParabéns! Você evoluiu seu nível para %bw_level%."));

        yml.addDefault(Messages.MONEY_REWARD_PER_MINUTE, "%bw_lang_prefix%&b+%bw_money% Moedas (Tempo de jogo).");
        yml.addDefault(Messages.MONEY_REWARD_WIN, "%bw_lang_prefix%&b+%bw_money% Moedas (Vencer o jogo).");
        yml.addDefault(Messages.MONEY_REWARD_PER_TEAMMATE, "%bw_lang_prefix%&b+%bw_money% Moedas (Suporte da equipe).");
        yml.addDefault(Messages.MONEY_REWARD_BED_DESTROYED, "%bw_lang_prefix%&b+%bw_money% Moedas (Bed Destroyed).");
        yml.addDefault(Messages.MONEY_REWARD_FINAL_KILL, "%bw_lang_prefix%&b+%bw_money% Moedas (Final Kill).");
        yml.addDefault(Messages.MONEY_REWARD_REGULAR_KILL, "%bw_lang_prefix%&b+%bw_money% Moedas (Regular Kill).");

        yml.addDefault(Messages.HALLOWEEN_ITEM_NAME, "&b&lFeliz Halloween");
        yml.addDefault(Messages.FORMATTING_HISTORY_DATE_FORMAT, "dd/MM HH:mm");
        yml.addDefault(Messages.HISTORY_RESULT_WIN, "&aVenceu");
        yml.addDefault(Messages.HISTORY_RESULT_LOSE, "&bPerdeu");
        yml.addDefault(Messages.PLAYER_HISTORY_NO_DATA, "%bw_lang_prefix%&bNao ha historico ainda.");
        yml.addDefault(Messages.PLAYER_HISTORY_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bJogador nao encontrado.");
        yml.addDefault(Messages.PLAYER_HISTORY_VIEW_OTHER_DENIED, "%bw_lang_prefix%&bVoce nao tem permissao para ver outros jogadores.");
        yml.addDefault(Messages.PLAYER_STATS_PLAYER_NOT_FOUND, "%bw_lang_prefix%&bJogador nao encontrado.");

        /* Lobby Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "history"), "&bHistorico de Partidas");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "history"), Collections.singletonList("&fBotao direito para ver o historico!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "stats"), "&bEstatísticas");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&fBotão direito para ver estatísticas! "));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "arena-selector"), "&bSeletor de Arena");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "arena-selector"), Collections.singletonList("&fBotão direito para escolher arena."));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", "leave"), "&bSair do BedWars");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fBotão direito para sair do BedWars!"));
        /* Pre Game Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "stats"), "&bEstatísticas");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "stats"), Collections.singletonList("&fBotão direito para ver estatísticas!"));
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", "leave"), "&bSair da Partida");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fBotão direito para sair da arena!"));
        /* Spectator Command Items */
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "teleporter"), "&bTeletransportador");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", "leave"), "&bSair da Partida");
        yml.addDefault(Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", "leave"), Collections.singletonList("&fBotão direito para sair da arena!"));

        /* save default items messages for stats gui */
        yml.addDefault(Messages.PLAYER_STATS_GUI_INV_NAME, "&fEstatisticas de %bw_player%");
        addDefaultStatsMsg(yml, "overall", "&aGeral",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%",
                "&fWLR: &b%bw_wlr%",
                "",
                "&fPrimeira Partida: &b%bw_play_first%",
                "&fUltima Partida: &b%bw_play_last%");
        addDefaultStatsMsg(yml, "solo", "&aSolo",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "doubles", "&aDuplas",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "triples", "&aTrios",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "quads", "&aQuartetos",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "one-vs-one", "&a1v1",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "two-vs-two", "&a2v2",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "three-vs-three", "&a3v3",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");
        addDefaultStatsMsg(yml, "four-vs-four", "&a4v4",
                "&fPartidas: &b%bw_games_played%",
                "&fVitorias: &b%bw_wins%",
                "&fDerrotas: &b%bw_losses%",
                "&fKills: &b%bw_kills%",
                "&fMortes: &b%bw_deaths%",
                "&fKills Finais: &b%bw_final_kills%",
                "&fMortes Finais: &b%bw_final_deaths%",
                "&fCamas Quebradas: &b%bw_beds%",
                "&fFKDR: &b%bw_fkdr%");

        /* history gui */
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_INV_NAME, "&fHistorico: %bw_target%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_ENTRY_NAME, "&bPartida #%bw_history_index%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_ENTRY_LORE, Arrays.asList(
                "&fMapa: &f%bw_history_map%",
                "&fModo: &f%bw_history_mode%",
                "&fColocacao: &f%bw_history_placement%",
                "&fResultado: %bw_history_result%",
                "&fKills: &f%bw_history_kills%",
                "&fFinal Kills: &f%bw_history_final_kills%",
                "&fTotal Kills: &f%bw_history_total_kills%",
                "&fMortes: &f%bw_history_deaths%",
                "&fMortes Finais: &f%bw_history_final_deaths%",
                "&fCamas: &f%bw_history_beds_destroyed%",
                "&fDuracao: &f%bw_history_duration%",
                "&fData: &f%bw_history_datetime%",
                "",
                "&aClique para ver eventos."
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_EMPTY_NAME, "&bSem partidas");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_EMPTY_LORE, Collections.singletonList("&fJogue uma partida para ver aqui."));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_NEXT_NAME, "&aProxima pagina");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_NEXT_LORE, Collections.singletonList("&fPagina %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PREV_NAME, "&bPagina anterior");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PREV_LORE, Collections.singletonList("&fPagina %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PAGE_NAME, "&bPagina %bw_history_page%/%bw_history_pages%");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_PAGE_LORE, Collections.singletonList("&fTotal de partidas: &f%bw_history_total%"));
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_FILLER_NAME, " ");
        yml.addDefault(Messages.PLAYER_HISTORY_GUI_FILLER_LORE, Collections.emptyList());

        /* history events gui */
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_NO_DATA, "%bw_lang_prefix%&bNenhum evento encontrado.");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_NO_PERMISSION, "%bw_lang_prefix%&bVoce nao tem permissao para ver os eventos avancados da partida.");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_INV_NAME, "&fEventos da Partida");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_NAME, "&bSem eventos");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_LORE, Collections.singletonList("&fNenhum evento registrado."));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_NAME, "&aProxima pagina");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_LORE, Collections.singletonList("&fPagina %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_NAME, "&bPagina anterior");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_LORE, Collections.singletonList("&fPagina %bw_history_page%/%bw_history_pages%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_NAME, "&bPagina %bw_history_page%/%bw_history_pages%");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_LORE, Collections.singletonList("&fTotal de eventos: &f%bw_history_total%"));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_NAME, "&aVoltar");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_LORE, Collections.singletonList("&fVoltar para a lista."));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_NAME, " ");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_LORE, Collections.emptyList());
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_DEFAULT_NAME, "&bEvento #%bw_history_event_index%");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_DEFAULT_LORE, Arrays.asList(
                "&fTipo: &f%bw_event_type%",
                "&fHora: &f%bw_event_time%",
                "&fAtor: &f%bw_event_actor%",
                "&fAlvo: &f%bw_event_target%",
                "&fTime: %bw_event_team_color%%bw_event_team%",
                "&fMeta: &f%bw_event_meta%"
        ));

        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "player-kill"), "&bAbate");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "player-kill"), Arrays.asList(
                "&fHora: &f%bw_event_time%",
                "&fKiller: &f%bw_event_actor%",
                "&fVitima: &f%bw_event_target%",
                "&fCausa: &f%bw_event_meta_cause%",
                "&fFinal: &f%bw_event_meta_final_kill%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "bed-break"), "&bCama destruida");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "bed-break"), Arrays.asList(
                "&fHora: &f%bw_event_time%",
                "&fJogador: &f%bw_event_actor%",
                "&fTime alvo: &f%bw_event_meta_victim_team%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "shop-buy"), "&aCompra");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "shop-buy"), Arrays.asList(
                "&fHora: &f%bw_event_time%",
                "&fJogador: &f%bw_event_actor%",
                "&fItem: &f%bw_event_meta_item%",
                "&fCategoria: &f%bw_event_meta_category%"
        ));
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", "upgrade-buy"), "&bUpgrade");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", "upgrade-buy"), Arrays.asList(
                "&fHora: &f%bw_event_time%",
                "&fJogador: &f%bw_event_actor%",
                "&fUpgrade: &f%bw_event_meta_upgrade%"
        ));

        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "game-start"), "Inicio");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "game-end"), "Fim");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-join"), "Entrada");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-leave"), "Saida");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-rejoin"), "Reconectar");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-respawn"), "Respawn");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-first-spawn"), "Primeiro Spawn");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "player-kill"), "Abate");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "bed-break"), "Cama destruida");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "team-eliminated"), "Time eliminado");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "team-assign"), "Time definido");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "upgrade-buy"), "Upgrade");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "shop-buy"), "Compra");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "shop-open"), "Abrir loja");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "generator-upgrade"), "Upgrade gerador");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "next-event"), "Proximo evento");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "dream-defender"), "Golem");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "bed-bug"), "Inseto");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "invisibility"), "Invisibilidade");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "egg-bridge-throw"), "Egg Bridge");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "egg-bridge-build"), "Egg Bridge build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "popup-tower-place"), "Torre");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "popup-tower-build"), "Torre build");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "item-deposit"), "Deposito");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "base-enter"), "Base entrar");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "base-leave"), "Base sair");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "resource-collect"), "Coleta");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "resource-drop"), "Drop");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "stat-change"), "Status");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "xp-gain"), "XP");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "money-gain"), "Moedas");
        yml.addDefault(Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", "level-up"), "Level");

        yml.addDefault(Messages.SCOREBOARD_DEFAULT_WAITING, Arrays.asList("&f&lBED WARS", "&f%bw_date% &f%bw_server_id%", "", "&fMapa: &a%bw_map%", "", "&fJogadores: &a%bw_on%/%bw_max%", "", "&fEsperando...", "", "§fModo: &a%bw_group%", "&fVersão: &f%bw_version%", "", "&b%bw_server_ip%"));
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_STARTING, Arrays.asList("&f&lBED WARS", "&f%bw_date% &f%bw_server_id%", "", "&fMapa: &a%bw_map%", "", "&fJogadores: &a%bw_on%/%bw_max%", "", "&fInicio em &a%bw_time%s", "", "§fModo: &a%bw_group%", "&fVersão: &f%bw_version%", "", "&b%bw_server_ip%"));
        yml.addDefault(Messages.SCOREBOARD_DEFAULT_PLAYING, Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% em &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "%bw_team_5%", "%bw_team_6%", "%bw_team_7%", "%bw_team_8%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.Doubles.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% em &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "%bw_team_5%", "%bw_team_6%", "%bw_team_7%", "%bw_team_8%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.3v3v3v3.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% em &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "", "&fKills: &a%bw_kills%", "&fFinal Kills: &a%bw_final_kills%", "&fBeds Broken: &a%bw_beds%", "", "&b%bw_server_ip%"));

        yml.addDefault("scoreboard.4v4v4v4.playing", Arrays.asList("&b&lBED WARS", "&f%bw_date%", "", "&f%bw_next_event% em &a%bw_time%", "",
                "%bw_team_1%", "%bw_team_2%", "%bw_team_3%", "%bw_team_4%", "", "&fKills: &a%bw_kills%", "&fFinal Kills: &a%bw_final_kills%", "&fBeds Broken: &a%bw_beds%", "", "&b%bw_server_ip%"));

        yml.addDefault(Messages.SCOREBOARD_LOBBY, Arrays.asList("&b&lBedWars,&b&lB&b&ledWars,&b&lB&b&le&b&ldWars,&b&lB&b&le&b&ld&b&lWars,&b&lBe&b&ld&b&lW&b&lars,&b&lBed&b&lW&b&la&b&lrs,&b&lBedW&b&la&b&lr&b&ls,&b&lBedWa&b&lr&b&ls,&b&lBedWar&b&ls,&b&lBedWars",
                "&fSeu nivel: %bw_level%", "", "&fProgresso: &a%bw_current_xp%&f/&b%bw_required_xp%", "%bw_progress%", "", "&f%bw_player%", "", "&fMoedas: &a%bw_money%", "", "&fTotal de vitórias: &a%bw_wins%", "&fTotal de abates: &a%bw_kills%", "", "&b%bw_server_ip%"));

        yml.addDefault(Messages.SHOP_INDEX_NAME, "&fCompra rápida");
        yml.addDefault(Messages.SHOP_QUICK_ADD_NAME, "&fAdicionando à compra rápida...");
        yml.addDefault(Messages.SHOP_INSUFFICIENT_MONEY, "%bw_lang_prefix%&bVocê não tem %bw_currency% suficiente! Precisa de mais %bw_amount%!");
        yml.addDefault(Messages.SHOP_NEW_PURCHASE, "%bw_lang_prefix%&aVocê comprou &b%bw_item%");
        yml.addDefault(Messages.SHOP_ALREADY_BOUGHT, "%bw_lang_prefix%&bVocê já comprou isso!");
        yml.addDefault(Messages.SHOP_ALREADY_HIGHER_TIER, "%bw_lang_prefix%&bYou already have a higher tier item.");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME, "%bw_time_bar%");
        yml.addDefault(Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME, "%bw_team_color%%bw_despawn_time%s &f[ %bw_team_color%%bw_health%&f]");
        yml.addDefault(Messages.SHOP_SEPARATOR_NAME, "&f⇧ Categorias");
        yml.addDefault(Messages.SHOP_SEPARATOR_LORE, Collections.singletonList("&f⇩ Itens"));
        yml.addDefault(Messages.SHOP_QUICK_BUY_NAME, "&bCompra rápida");
        yml.addDefault(Messages.SHOP_QUICK_BUY_LORE, new ArrayList<>());
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_NAME, "&bEspaço vazio!");
        yml.addDefault(Messages.SHOP_QUICK_EMPTY_LORE, Arrays.asList("&fEste é um slot de compra rápida!", "&fClique em um item segurando o", "&fshift para adicionar neste slot."));
        yml.addDefault(Messages.SHOP_CAN_BUY_COLOR, "&a");
        yml.addDefault(Messages.SHOP_CANT_BUY_COLOR, "&b");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CAN_BUY, "&bClique para comprar!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_CANT_AFFORD, "&bVocê não tem o suficiente de %bw_currency%!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_MAXED, "&aMÁXIMO!");
        yml.addDefault(Messages.SHOP_LORE_STATUS_ARMOR, "&aEQUIPADO!");
        yml.addDefault(Messages.SHOP_LORE_QUICK_ADD, "&bClique + shift para por na Compra Rápida");
        yml.addDefault(Messages.SHOP_LORE_QUICK_REMOVE, "&bClique + shift para tirar da Compra Rápida");

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "&fBlocos", "&aBlocos", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "wool", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Lã", Arrays.asList("&fPreço: &f%bw_cost% %bw_currency%", "", "&fÓtimo para atravessar ilhas.", "&fTransforma-se na cor do seu time.",
                "&fcolor.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "clay", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Argila Endurecida", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fBloco básico para defender sua cama.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "glass", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Vidro à prova de explosão", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fImune a explosões.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "stone", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Pedra do Fim", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fBloco sólido para defender sua cama.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "ladder", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Escada", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fÚtil para salvar gatos presos", "&fde arvores.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "obsidian", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Obsidiana", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fProteção extrema para sua cama.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "wood", ConfigPath.SHOP_PATH_CATEGORY_BLOCKS, "%bw_color%Madeira", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fBloco sólido para defender sua cama.", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_MELEE, "&fCorpo a corpo", "&aCorpo a corpo", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "stone-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Espada de Pedra", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "iron-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Espada de Ferro", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "diamond-sword", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Espada de Diamante", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "stick", ConfigPath.SHOP_PATH_CATEGORY_MELEE, "%bw_color%Graveto (Repulsão I)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "&aArmadura", "&aArmadura", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "chainmail", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Armadura de Malha Permanente", Arrays.asList("&fPreço: %bw_cost% %bw_currency%",
                "", "&fCalças e botas de malha", "&fque você sempre irá renascer", "&fcom elas.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "iron-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Armadura de Ferro Permanente", Arrays.asList("&fPreço: %bw_cost% %bw_currency%",
                "", "&fCalças e botas de ferro", "&fque você sempre irá renascer", "&fcom elas.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "diamond-armor", ConfigPath.SHOP_PATH_CATEGORY_ARMOR, "%bw_color%Armadura de Diamante Permanente", Arrays.asList("&fPreço: %bw_cost% %bw_currency%",
                "", "&fCalças e botas de diamante", "&fque você sempre irá renascer", "&fcom elas.", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "&fFerramentas", "&aFerramentas", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "shears", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Tesouras Permanentes", Arrays.asList("&fPreço: %bw_cost% %bw_currency%",
                "", "&fÓtimo para se livrar da lã. Você", "&fsempre irá nascer com as tesouras.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "pickaxe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Picareta %bw_tier%", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "&fNível: &b%bw_tier%",
                "", "&fEste é um item melhorável.", "&fEle perderá 1 nível após.", "&fvocê morer!", "", "&fVocê vai permanentemente", "&frenascer com pelo menos o", "&fnível mais baixo.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "axe", ConfigPath.SHOP_PATH_CATEGORY_TOOLS, "%bw_color%Machado %bw_tier%", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "&fNível: &b%bw_tier%",
                "", "&fEste é um item melhorável.", "&fEle perderá 1 nível após.", "&fvocê morer!", "", "&fVocê vai permanentemente", "&frenascer com pelo menos o", "&fnível mais baixo.", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_RANGED, "&fArcos", "&aArcos", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "arrow", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Flecha", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow1", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Arco", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow2", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Arco (Força I)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bow3", ConfigPath.SHOP_PATH_CATEGORY_RANGED, "%bw_color%Arco (Força I, Impacto I)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "&fPoções", "&aPoções", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "speed-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Poção de Agilidade II (45 segundos)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "jump-potion", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Poção de Super Pulo V (45 segundos)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "invisibility", ConfigPath.SHOP_PATH_CATEGORY_POTIONS, "%bw_color%Poção de Invisibilidade (30 segundos)", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "%bw_quick_buy%", "%bw_buy_status%"));

        addCategoryMessages(yml, ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "&fUtilidades", "&aUtilidades", Collections.singletonList("&bClique para ver!"));

        addContentMessages(yml, "golden-apple", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Maça Dourada", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fCura completa.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bedbug", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Traça", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fGera traças onde a bola",
                "&fde neve cai para distrair seus", "&finimigos. Dura 15 segundos.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "dream-defender", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Defesa dos Sonhos", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fGolem de Ferro para defender sua",
                "&fbase. Dura 4 minutos.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "fireball", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Bola de Fogo", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fBotão direito para atirar! Ótimo para",
                "&frepelir os inimigos andando", "&fpor pontes.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "tnt", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%TNT", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fExplode instantaneamente, apropriado",
                "&fpara explodir coisas!", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "ender-pearl", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Pérola do Fim", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fA maneira mais rápida de invadir",
                "&fa base do inimigo.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "water-bucket", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Balde de Água", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fÓtimo para atrasar inimigos. Também",
                "&fprotege contra TNT.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "bridge-egg", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Ovo das Pontes", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fEste ovo cria uma ponte na",
                "&fdireção que for jogada.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "magic-milk", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Leite Mágico", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fEvite acionar armadilhas por 60",
                "&fsegundos após o consumo.", "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "sponge", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Esponja", Arrays.asList("&fPreço: %bw_cost% %bw_currency%", "", "&fÓtimo para absorver a água.",
                "", "%bw_quick_buy%", "%bw_buy_status%"));
        addContentMessages(yml, "tower", ConfigPath.SHOP_PATH_CATEGORY_UTILITY, "%bw_color%Torre Pop-up Compacta", Arrays.asList("&fCusto: %bw_cost% %bw_currency%", "", "&fColoque uma torre pop-up compacta", "&fde defesa de torre!", "", "%bw_quick_buy%", "%bw_buy_status%"));

        yml.addDefault(Messages.MEANING_NO_TRAP, "Sem armadilha!");
        yml.addDefault(Messages.FORMAT_UPGRADE_TRAP_COST, "&fPreço: %bw_currency_color%%bw_cost% %bw_currency%");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD, "&b");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD, "&b");
        yml.addDefault(Messages.FORMAT_UPGRADE_COLOR_UNLOCKED, "&a");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_LOCKED, "&f");
        yml.addDefault(Messages.FORMAT_UPGRADE_TIER_UNLOCKED, "&a");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY, "%bw_color%Clique para comprar!");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY, "%bw_color%Você não tem o suficiente de %bw_currency%.");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_SPACE, "&bVocê não tem espaço livre no inventário!");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_LOCKED, "&bBLOQUEADO");
        yml.addDefault(Messages.UPGRADES_LORE_REPLACEMENT_UNLOCKED, "%bw_color%DESBLOQUEADO");
        yml.addDefault(Messages.UPGRADES_UPGRADE_BOUGHT_CHAT, "&a%bw_player% compro &b%bw_item%");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-1"), "%bw_color%Forja de Ferro");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "forge"),
                Arrays.asList("&fMelhore a geração de recursos", "&fna sua base.", "", "{tier_1_color}Nível 1: +50% de recursos, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Nível 2: +100% de recursos, &b{tier_2_cost} {tier_2_currency}",
                        "{tier_3_color}Nível 3: Gerar esmeraldas, &b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}Nível 4: +200% de recursos, &b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-2"), "%bw_color%Forja de Ouro");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-3"), "%bw_color%Forja de Esmeralda");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "forge").replace("%bw_tier%", "tier-4"), "%bw_color%Forja Melhorada");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + "traps", "&bCompre uma armadilha");
        yml.addDefault(Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + "traps", Arrays.asList("&fAs armadilhas compradas serão", "&fenfileiradas abaixo.", "", "&bClique para navegar!"));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "swords").replace("%bw_tier%", "tier-1"), "%bw_color%Espadas Afiadas");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "swords"),
                Arrays.asList("&fTodas as espadas do seu time irão ter o", "&fencantamento Afiação I permanentemente.", "", "{tier_1_color}Preço: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-1"), "%bw_color%Armadura Reforçada I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "armor"),
                Arrays.asList("&fSua equipe ganha permanentemente", "&fproteção em toda a armadura!", "", "{tier_1_color}Nível 1: Proteção I, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Nível 2: Proteção II, &b{tier_2_cost} {tier_2_currency}",
                        "{tier_3_color}Nível 3: Proteção III, &b{tier_3_cost} {tier_3_currency}",
                        "{tier_4_color}Nível 4: Proteção IV, &b{tier_4_cost} {tier_4_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-2"), "%bw_color%Armadura Reforçada II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-3"), "%bw_color%Armadura Reforçada III");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "armor").replace("%bw_tier%", "tier-4"), "%bw_color%Armadura Reforçada IV");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "miner").replace("%bw_tier%", "tier-1"), "%bw_color%Minerador Maníaco I");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "miner"),
                Arrays.asList("&fTodos os jogadores do seu time irão", "&fganhar permanentemente Pressa.", "", "{tier_1_color}Nível 1: Pressa I, &b{tier_1_cost} {tier_1_currency}",
                        "{tier_2_color}Nível 2: Pressa II, &b{tier_2_cost} {tier_2_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "miner").replace("%bw_tier%", "tier-2"), "%bw_color%Minerador Maníaco II");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "heal-pool").replace("%bw_tier%", "tier-1"), "%bw_color%Regeneração na Ilha");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "heal-pool"),
                Arrays.asList("&fCria um campo de regeneração", "&fem torno de sua base!", "", "{tier_1_color}Preço: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("%bw_name%", "dragon").replace("%bw_tier%", "tier-1"), "%bw_color%Buffar Dragões");
        yml.addDefault(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("%bw_name%", "dragon"),
                Arrays.asList("&fSua equipe terá 2 dragões", "&fem vez de 1 durante o deathmatch!", "", "{tier_1_color}Preço: &b{tier_1_cost} {tier_1_currency}", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "glass", "&f⬆&fAdquirível");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "glass", Collections.singletonList("&f⬇&fFila de armadilhas"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "first", "%bw_color%Armadilha #1: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "first", Arrays.asList("&fO primeiro inimigo a andar", "&fem sua base irá acionar", "&festa armadilha!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "first",
                Arrays.asList("", "&fA compra de uma armadilha", "&ffica nesta fila. Seu custo", "&fserá com base no número", "&fde armadilhas enfileiradas.", "", "&fPróxima armadilha: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "second", "%bw_color%Armadilha #2: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "second", Arrays.asList("&fO segundo inimigo a andar", "&fem sua base irá acionar", "&festa armadilha!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "second",
                Arrays.asList("", "&fA compra de uma armadilha", "&ffica nesta fila. Seu custo", "&fserá com base no número", "&fde armadilhas enfileiradas.", "", "&fPróxima armadilha: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_NAME_PATH + "third", "%bw_color%Armadilha #3: %bw_name%");
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + "third", Arrays.asList("&fO terceiro inimigo a andar", "&fem sua base irá acionar", "&festa armadilha!"));
        yml.addDefault(Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + "third",
                Arrays.asList("", "&fA compra de uma armadilha", "&ffica nesta fila. Seu custo", "&fserá com base no número", "&fde armadilhas enfileiradas.", "", "&fPróxima armadilha: &b%bw_cost% %bw_currency%"));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "1", "%bw_color%É uma armadilha!");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "1", Arrays.asList("&fIsso irá causar cegueira e lentidão", "&fdurante 5 segundos nos invasores.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "2", "%bw_color%Armadilha Contra-ofensiva");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "2", Arrays.asList("&fConcede Velocidade I por 15 segundos a", "&fjogadores aliados próximos à sua base.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "3", "%bw_color%Alarme");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "3", Arrays.asList("&fIrá revelar jogadores invisíveis,", "&fbem como seu nome e time.", ""));
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + "4", "%bw_color%Cansaço");
        yml.addDefault(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + "4", Arrays.asList("&fOs inimigos irão receber fadiga I ao entrar", "&fna sua base com duração de 10 segundos.", ""));
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + "back", "&aVoltar");
        yml.addDefault(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + "back", Collections.singletonList("&fPara Melhorias & Traps"));
        yml.addDefault(Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + "traps", "&fEnfileirar uma armadilha");
        yml.addDefault(Messages.UPGRADES_TRAP_QUEUE_LIMIT, "&bFila de armadilhas cheia!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_MSG, "&b&lA armadilha %bw_trap% foi acionada!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_TITLE, "&bARMADILHA ACIONADA!");
        yml.addDefault(Messages.UPGRADES_TRAP_DEFAULT_SUBTITLE, "&fSua %bw_trap% foi acionada!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_MSG + "3", "&b&lArmadilha de alarme acionada por &f&l%bw_player% &b&ldo time %bw_color%&l%bw_team%&b&l!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_TITLE + "3", "&b&lALARME!!!");
        yml.addDefault(Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + "3", "&fAlarme acionado pelo time %bw_color%%bw_team%&f!");
        yml.addDefault(Messages.UPGRADES_UPGRADE_ALREADY_CHAT, "&bVocê já comprou essa melhoria!");
        yml.addDefault(Messages.DRAW_TITLE, "&b&lO JOGO ACABOU!");
        yml.addDefault(Messages.DRAW_SUBTITLE, "&fNinguém venceu.");
        yml.set(Messages.COMMAND_MAIN, Arrays.asList(
                "",
                "&a- &f/stats",
                "&a- &f/historico",
                "&a- &f/" + BedWars.mainCmd + " join &o<arena/grupo>",
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
        yml.addDefault(Messages.NPC_NAME_TEAM_UPGRADES.replace("%group%", group), Arrays.asList("&bMELHORIAS DA EQUIPE", "&b&lCLIQUE DIREITO"));
        yml.addDefault(Messages.NPC_NAME_SOLO_UPGRADES.replace("%group%", group), Arrays.asList("&bMELHORIAS", "&b&lCLIQUE DIREITO"));
        yml.addDefault(Messages.NPC_NAME_TEAM_SHOP.replace("%group%", group), Arrays.asList("&bLOJA DA EQUIPE", "&b&lCLIQUE DIREITO"));
        yml.addDefault(Messages.NPC_NAME_SOLO_SHOP.replace("%group%", group), Arrays.asList("&bLOJA", "&b&lCLIQUE DIREITO"));
    }
}

