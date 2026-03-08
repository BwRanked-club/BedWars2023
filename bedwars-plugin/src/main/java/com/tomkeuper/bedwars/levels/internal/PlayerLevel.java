package com.tomkeuper.bedwars.levels.internal;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.events.player.PlayerLevelUpEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerXpGainEvent;
import com.tomkeuper.bedwars.configuration.LevelsConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class PlayerLevel {

    private static final Map<UUID, PlayerLevel> levelByPlayer =
            new HashMap<>(256, 0.75f);
    private static final ThreadLocal<NumberFormat> NF = ThreadLocal.withInitial(() -> {
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(2);
        f.setMinimumFractionDigits(0);
        return f;
    });
    private static final String[] PROGRESS_BARS = new String[11];

    static {
        String format = LevelsConfig.levels.getString("progress-bar.format");
        String symbol = LevelsConfig.levels.getString("progress-bar.symbol");
        String unlockedColor = LevelsConfig.levels.getString("progress-bar.unlocked-color");
        String lockedColor = LevelsConfig.levels.getString("progress-bar.locked-color");
        for (int unlocked = 0; unlocked <= 10; unlocked++) {
            int locked = 10 - unlocked;
            String bar = format.replace("{progress}",
                    unlockedColor
                            + String.valueOf(new char[unlocked]).replace("\0", symbol)
                            + lockedColor
                            + String.valueOf(new char[locked]).replace("\0", symbol)
            );
            PROGRESS_BARS[unlocked] = ChatColor.translateAlternateColorCodes('&', bar);
        }
    }

    private final UUID uuid;
    private int level;
    private int nextLevelCost;
    private String levelName;
    private int currentXp;
    private String progressBar;
    private String requiredXp;
    private String formattedCurrentXp;
    // keep trace if current level is different than the one in database
    private boolean modified = false;

    /**
     * Cache a player level.
     */
    public PlayerLevel(UUID player, int level, int currentXp) {
        this.uuid = player;
        setLevelName(level);
        setNextLevelCost(level, true);

        //fix levels broken in the past by an issue
        if (level < 1) level = 1;
        if (currentXp < 0) currentXp = 0;

        this.level = level;
        this.currentXp = currentXp;
        updateProgressBar();
        if (!levelByPlayer.containsKey(player)) levelByPlayer.put(player, this);
    }

    /**
     * Get PlayerLevel by player.
     */
    public static PlayerLevel getLevelByPlayer(UUID player) {
        PlayerLevel existing = levelByPlayer.get(player);
        if (existing != null) return existing;
        return new PlayerLevel(player, 1, 0);
    }

    /**
     * Get cached PlayerLevel by player without creating a new entry.
     */
    public static PlayerLevel getCachedLevelByPlayer(UUID player) {
        return levelByPlayer.get(player);
    }

    public static void saveAll() {
        for (PlayerLevel pl : levelByPlayer.values()) {
            if (pl != null) pl.updateDatabase();
        }
    }

    public void setNextLevelCost(int level, boolean initialize) {
        if (!initialize) modified = true;
        this.nextLevelCost = LevelsConfig.getNextCost(level);
    }

    public void lazyLoad(int level, int currentXp) {
        modified = false;
        if (level < 1) level = 1;
        if (currentXp < 0) currentXp = 0;
        setLevelName(level);
        setNextLevelCost(level, true);
        this.level = level;
        this.currentXp = currentXp;
        updateProgressBar();
        modified = false;
    }

    /**
     * Update the player progress bar.
     */
    private void updateProgressBar() {
        double ratio = (nextLevelCost - currentXp) / (double) nextLevelCost * 10;
        int unlocked = 10 - (int) ratio;
        if (unlocked < 0) unlocked = 0;
        if (unlocked > 10) unlocked = 10;

        progressBar = PROGRESS_BARS[unlocked];
        requiredXp = formatNumber(nextLevelCost);
        formattedCurrentXp = formatNumber(currentXp);
    }

    /**
     * Get player current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set player level.
     */
    public void setLevel(int level) {
        this.level = level;
        nextLevelCost = LevelsConfig.getNextCost(level);
        this.levelName = ChatColor.translateAlternateColorCodes('&',
                        LevelsConfig.getLevelName(level))
                .replace("{number}", String.valueOf(level));
        requiredXp = nextLevelCost >= 1000
                ? nextLevelCost % 1000 == 0
                ? nextLevelCost / 1000 + "k"
                : (double) nextLevelCost / 1000 + "k"
                : String.valueOf(nextLevelCost);
        updateProgressBar();
        modified = true;
    }

    /**
     * Get the amount of xp required to level up.
     */
    public int getNextLevelCost() {
        return nextLevelCost;
    }

    /**
     * Get player uuid.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get player current level display name.
     */
    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(int level) {
        this.levelName = ChatColor.translateAlternateColorCodes('&', LevelsConfig.getLevelName(level)).replace("{number}", String.valueOf(level));
    }

    /**
     * Get player xp.
     */
    public int getCurrentXp() {
        return currentXp;
    }

    /**
     * Get progress bar for player.
     */
    public String getProgress() {
        return progressBar;
    }

    /**
     * Get target xp already formatted.
     * Like: 2000 is 2k
     */
    public String getFormattedRequiredXp() {
        return requiredXp;
    }

    /**
     * Add xp to player with source.
     */
    public int addXp(int xp, PlayerXpGainEvent.XpSource source) {
        if (xp < 0) return 0;
        Player player = Bukkit.getPlayer(uuid);
        int finalXp = XpMultiplierUtil.applyMultiplier(player, xp);
        PlayerXpGainEvent event = new PlayerXpGainEvent(player, finalXp, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return 0;
        finalXp = Math.max(0, event.getAmount());
        if (finalXp < 1) return 0;
        this.currentXp += finalXp;
        upgradeLevel();
        updateProgressBar();
        modified = true;
        return finalXp;
    }

    public static String getXpMultiplierSuffix(Player player) {
        return XpMultiplierUtil.getMultiplierSuffix(player);
    }

    /**
     * Set player xp.
     */
    public void setXp(int currentXp) {
        if (currentXp <= 0) currentXp = 0;
        this.currentXp = currentXp;
        upgradeLevel();
        updateProgressBar();
        modified = true;
    }

    /**
     * Get player xp already formatted.
     * Like: 1000 is 1k
     */
    public String getFormattedCurrentXp() {
        return formattedCurrentXp;
    }

    /**
     * Used to upgrade player level.
     */
    public void upgradeLevel() {
        if (nextLevelCost <= 0) {
            nextLevelCost = LevelsConfig.getNextCost(level);
            if (nextLevelCost <= 0) return;
        }

        boolean leveledUp = false;
        while (currentXp >= nextLevelCost) {
            currentXp -= nextLevelCost;
            level++;
            nextLevelCost = LevelsConfig.getNextCost(level);
            setLevelName(level);

            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(
                    Bukkit.getPlayer(getUuid()), level, nextLevelCost
            ));

            leveledUp = true;
        }

        if (leveledUp) {
            requiredXp = formatNumber(nextLevelCost);
            formattedCurrentXp = formatNumber(currentXp);
            modified = true;
        }
    }

    private String formatNumber(int score) {
        NumberFormat f = NF.get();
        if (score >= 1000) return f.format(score / 1000.0) + "k";

        return f.format(score);
    }

    /**
     * Destroy data.
     */
    public void destroy() {
        levelByPlayer.remove(uuid);
        updateDatabase();
    }

    public void updateDatabase() {
        if (!modified) return;

        Runnable saveTask = () -> BedWars.getRemoteDatabase().setLevelData(
                uuid, level, currentXp,
                LevelsConfig.getLevelName(level),
                nextLevelCost
        );

        boolean useSchedulerAsync = BedWars.plugin != null
                && BedWars.plugin.isEnabled()
                && !BedWars.isShuttingDown()
                && Bukkit.isPrimaryThread();

        if (useSchedulerAsync) {
            try {
                Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, saveTask);
                modified = false;
                return;
            } catch (Exception ignored) {
                // fallback to direct save if scheduler rejects tasks during shutdown
            }
        }

        try {
            saveTask.run();
            modified = false;
        } catch (Exception ex) {
            if (BedWars.plugin != null) {
                BedWars.plugin.getLogger().warning("Failed to save level data for " + uuid + ": " + ex.getMessage());
            }
        }
    }
}
