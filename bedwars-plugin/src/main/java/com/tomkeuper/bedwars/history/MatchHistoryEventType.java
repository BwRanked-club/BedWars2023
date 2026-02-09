package com.tomkeuper.bedwars.history;

public enum MatchHistoryEventType {
    GAME_START("game-start"),
    GAME_END("game-end"),
    PLAYER_JOIN("player-join"),
    PLAYER_LEAVE("player-leave"),
    PLAYER_REJOIN("player-rejoin"),
    PLAYER_RESPAWN("player-respawn"),
    PLAYER_FIRST_SPAWN("player-first-spawn"),
    PLAYER_KILL("player-kill"),
    BED_BREAK("bed-break"),
    TEAM_ELIMINATED("team-eliminated"),
    TEAM_ASSIGN("team-assign"),
    UPGRADE_BUY("upgrade-buy"),
    SHOP_BUY("shop-buy"),
    SHOP_OPEN("shop-open"),
    GENERATOR_UPGRADE("generator-upgrade"),
    NEXT_EVENT("next-event"),
    DREAM_DEFENDER("dream-defender"),
    BED_BUG("bed-bug"),
    INVISIBILITY("invisibility"),
    EGG_BRIDGE_THROW("egg-bridge-throw"),
    EGG_BRIDGE_BUILD("egg-bridge-build"),
    POPUP_TOWER_PLACE("popup-tower-place"),
    POPUP_TOWER_BUILD("popup-tower-build"),
    ITEM_DEPOSIT("item-deposit"),
    BASE_ENTER("base-enter"),
    BASE_LEAVE("base-leave"),
    RESOURCE_COLLECT("resource-collect"),
    RESOURCE_DROP("resource-drop"),
    STAT_CHANGE("stat-change"),
    XP_GAIN("xp-gain"),
    MONEY_GAIN("money-gain"),
    LEVEL_UP("level-up");

    private final String key;

    MatchHistoryEventType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
