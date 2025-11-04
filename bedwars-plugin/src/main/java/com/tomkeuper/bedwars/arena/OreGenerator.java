package com.tomkeuper.bedwars.arena;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.GeneratorType;
import com.tomkeuper.bedwars.api.arena.generator.IGenHolo;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.generator.IGeneratorAnimation;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.entity.GeneratorHolder;
import com.tomkeuper.bedwars.api.events.gameplay.GeneratorDropEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GeneratorUpgradeEvent;
import com.tomkeuper.bedwars.api.hologram.containers.IHoloLine;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.region.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("WeakerAccess")
public class OreGenerator implements IGenerator {

    private Location location;
    private double upgradeStage = 1.0;
    private int spawnLimit = 0, amount = 1;
    final int speedMultiplier = 4;
    private double delay = 1, lastSpawn;
    @Getter
    private IArena arena;
    private ItemStack ore;
    private GeneratorType type;
    private List<IGeneratorAnimation> animations;
    private int dropID = 0;
    private ITeam bwt;
    private boolean hologram = true;
    boolean disabled = false;
    public HashMap<Player, IGenHolo> holograms = new HashMap<>();
    private GeneratorHolder item;
    public boolean stack = BedWars.getGeneratorsCfg().getBoolean(ConfigPath.GENERATOR_STACK_ITEMS);
    @Getter
    private static final ConcurrentLinkedDeque<OreGenerator> rotation = new ConcurrentLinkedDeque<>();

    public OreGenerator(Location location, IArena arena, GeneratorType type, ITeam bwt, boolean hologram) {
        if (type == GeneratorType.EMERALD || type == GeneratorType.DIAMOND) {
            this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + 1.3, location.getBlockZ() + 0.5);
        } else {
            this.location = location.add(0, 1.3, 0);
        }
        this.arena = arena;
        this.bwt = bwt;
        this.type = type;
        this.hologram = hologram;
        loadDefaults();
        BedWars.debug("Initializing new generator at: " + location + " - " + type + " - " + (bwt == null ? "NOTEAM" : bwt.getName()));
        Cuboid c = new Cuboid(location, arena.getConfig().getInt(ConfigPath.ARENA_GENERATOR_PROTECTION), true);
        c.setMaxY(c.getMaxY() + 5);
        c.setMinY(c.getMinY() - 2);
        arena.getRegionsList().add(c);
    }

    private String resolveKey(String basePath) {
        String groupKey = arena.getGroup() + "." + basePath;
        if (BedWars.getGeneratorsCfg().getYml().contains(groupKey)) return groupKey;
        return "Default." + basePath;
    }

    private double cfgDouble(String basePath) {
        return BedWars.getGeneratorsCfg().getDouble(resolveKey(basePath));
    }

    private int cfgInt(String basePath) {
        return BedWars.getGeneratorsCfg().getInt(resolveKey(basePath));
    }

    private boolean cfgBooleanOrGlobal(String basePath, String globalPath) {
        String groupKey = arena.getGroup() + "." + basePath;
        if (BedWars.getGeneratorsCfg().getYml().contains(groupKey)) {
            return BedWars.getGeneratorsCfg().getBoolean(groupKey);
        }
        return BedWars.getGeneratorsCfg().getBoolean(globalPath);
    }

    @Override
    public void upgrade() {
        switch (type) {
            case DIAMOND:
                upgradeStage++;
                if (upgradeStage == 2) {
                    delay = cfgDouble(ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY) * speedMultiplier;
                    amount = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT);
                    spawnLimit = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT);
                } else if (upgradeStage == 3) {
                    delay = cfgDouble(ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY) * speedMultiplier;
                    amount = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT);
                    spawnLimit = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_III_SPAWN_LIMIT);
                }
                ore = new ItemStack(Material.DIAMOND);
                for (IGenHolo e : holograms.values()) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("%bw_tier%", Language.getLang(e.getIso())
                            .m(upgradeStage == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)));
                }
                break;
            case EMERALD:
                upgradeStage++;
                if (upgradeStage == 2) {
                    delay = cfgDouble(ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY) * speedMultiplier;
                    amount = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT);
                    spawnLimit = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT);
                } else if (upgradeStage == 3) {
                    delay = cfgDouble(ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY) * speedMultiplier;
                    amount = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT);
                    spawnLimit = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_III_SPAWN_LIMIT);
                }
                ore = new ItemStack(Material.EMERALD);
                for (IGenHolo e : holograms.values()) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("%bw_tier%",
                            Language.getLang(e.getIso()).m(upgradeStage == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)));
                }
                break;
        }
        Bukkit.getPluginManager().callEvent(new GeneratorUpgradeEvent(this));
    }

    @Override
    public void spawn() {
        if (arena.getStatus() != GameState.playing) {
            return;
        }
        if (disabled) return;
        if (lastSpawn <= 0) {
            lastSpawn = delay - 1;
            if (spawnLimit != 0) {
                int oreCount = 0;
                for (Entity e : location.getWorld().getNearbyEntities(location, 3, 3, 3)) {
                    if (e.getType() == EntityType.DROPPED_ITEM) {
                        Item i = (Item) e;
                        if (i.getItemStack().getType() == ore.getType()) {
                            oreCount++;
                        }
                        if (oreCount >= spawnLimit) return;
                    }
                }
            }
            GeneratorDropEvent event;
            Bukkit.getPluginManager().callEvent(event = new GeneratorDropEvent(this));
            if (event.isCancelled()) {
                return;
            }
            dropItem(location);
            return;
        }
        lastSpawn--;
        if ((getType() == GeneratorType.EMERALD || getType() == GeneratorType.DIAMOND) && hologram) {
            for (Player p : arena.getWorld().getPlayers()) {
                IGenHolo e = holograms.get(p);
                if (e == null) holograms.put(p, new HoloGram(p));
                e = holograms.get(p);
                e.setTimerName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIMER).replace("%bw_seconds%", String.valueOf((int) Math.ceil(lastSpawn / speedMultiplier))));
            }
        }
    }

    private void dropItem(Location location, double amount) {
        for (double temp = amount; temp > 0; temp--) {
            ItemStack itemStack = new ItemStack(ore);
            if (!stack) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("custom" + dropID++);
                itemStack.setItemMeta(itemMeta);
            }
            Item item = location.getWorld().dropItem(location, itemStack);
            item.setVelocity(new Vector(0, 0, 0));
        }
    }

    @Override
    public void dropItem(Location location) {
        dropItem(location, amount);
    }

    @Override
    public void setOre(ItemStack ore) {
        BedWars.debug("Changing ore for generator at " + location.toString() + " from " + this.ore + " to " + ore);
        this.ore = ore;
    }

    @Override
    public List<IGeneratorAnimation> getAnimations() {
        return animations;
    }

    public void addAnimation(IGeneratorAnimation animation) {
        animations.add(animation);
    }

    @Override
    public HashMap<Player, IGenHolo> getPlayerHolograms() {
        return holograms;
    }

    @SuppressWarnings("WeakerAccess")
    public class HoloGram implements IGenHolo {
        String iso;
        IHologram hologram;
        IHoloLine tier, timer, name;
        Player p;

        public HoloGram(Player p) {
            this.p = p;
            this.iso = Language.getPlayerLanguage(p).getIso();
            if (getType() != GeneratorType.EMERALD && getType() != GeneratorType.DIAMOND) return;
            String tierText = Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIER)
                    .replace("%bw_tier%", Language.getLang(iso).m(Messages.FORMATTING_GENERATOR_TIER1));
            String timerText = Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIMER)
                    .replace("%bw_seconds%", String.valueOf(lastSpawn));
            String nameText = Language.getLang(iso).m(getOre().getType() == Material.DIAMOND ? Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND
                    : Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD);
            hologram = BedWars.getAPI().getHologramsUtil().createHologram(p, location.clone().add(0, 0.5, 0), tierText, nameText, timerText);
            hologram.setGap(0.3);
            this.timer = hologram.getLine(0);
            this.name = hologram.getLine(1);
            this.tier = hologram.getLine(2);
        }

        @Override
        public void setTierName(String name) {
            tier.setText(name);
        }

        @Override
        public void setTimerName(String name) {
            timer.setText(name);
        }

        @Override
        public String getIso() {
            return iso;
        }

        @Override
        public Player getPlayer() {
            return p;
        }

        @Override
        public IGenerator getGenerator() {
            return OreGenerator.this;
        }

        @Override
        public void update() {
            hologram.getLines().forEach(IHoloLine::reveal);
        }

        @Override
        public void destroy() {
            tier.remove();
            timer.remove();
            name.remove();
        }
    }

    @Override
    public void rotate() {
        if (item == null) return;
        for (IGeneratorAnimation a : animations) {
            a.run();
        }
    }

    @Override
    public void setDelay(double delay) {
        this.delay = delay * speedMultiplier;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public ItemStack getOre() {
        return ore;
    }

    @Override
    public void disable() {
        if (getType() == GeneratorType.DIAMOND || getType() == GeneratorType.EMERALD) {
            rotation.remove(this);
            for (IGenHolo a : holograms.values()) {
                a.destroy();
            }
            if (item != null) {
                item.destroy();
                item = null;
            }
            holograms.clear();
        }
        disabled = true;
    }

    @Override
    public void enable() {
        if (getType() == GeneratorType.DIAMOND || getType() == GeneratorType.EMERALD) {
            enableRotation();
        }
        disabled = false;
    }

    @Override
    public void updateHolograms(Player p) {
        if (!hologram) return;
        if (getType() != GeneratorType.EMERALD && getType() != GeneratorType.DIAMOND) return;
        if (!arena.getWorld().getPlayers().contains(p)) return;
        IGenHolo h = holograms.get(p);
        if (h == null && hologram) holograms.put(p, new HoloGram(p));
        h = holograms.get(p);
        h.update();
    }

    @Override
    public void enableRotation() {
        rotation.add(this);
        if (hologram) {
            for (Player p : arena.getWorld().getPlayers()) {
                IGenHolo h = holograms.get(p);
                if (h == null) holograms.put(p, new HoloGram(p));
            }
            for (IGenHolo hg : holograms.values()) hg.update();
        }
        Material displayMat;
        if (type == GeneratorType.DIAMOND) {
            displayMat = Material.DIAMOND_ORE;
        } else if (type == GeneratorType.EMERALD) {
            displayMat = Material.EMERALD_ORE;
        } else {
            displayMat = (type == GeneratorType.GOLD) ? Material.GOLD_BLOCK : Material.IRON_BLOCK;
        }
        this.item = new GeneratorHolder(location.add(0, 0.9, 0), new ItemStack(displayMat));
        if (this.animations == null) this.animations = new ArrayList<>();
        this.animations.clear();
        animations.add(BedWars.nms.createDefaultGeneratorAnimation(item.getArmorStand()));
    }

    @Override
    public void setSpawnLimit(int value) {
        this.spawnLimit = value;
    }

    private void loadDefaults() {
        switch (type) {
            case IRON:
                delay = cfgDouble(ConfigPath.GENERATOR_IRON_DELAY) * speedMultiplier;
                amount = cfgInt(ConfigPath.GENERATOR_IRON_AMOUNT);
                spawnLimit = cfgInt(ConfigPath.GENERATOR_IRON_SPAWN_LIMIT);
                ore = new ItemStack(Material.IRON_INGOT);
                break;
            case GOLD:
                delay = cfgDouble(ConfigPath.GENERATOR_GOLD_DELAY) * speedMultiplier;
                amount = cfgInt(ConfigPath.GENERATOR_GOLD_AMOUNT);
                spawnLimit = cfgInt(ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT);
                ore = new ItemStack(Material.GOLD_INGOT);
                break;
            case DIAMOND:
                delay = cfgDouble(ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY) * speedMultiplier;
                amount = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT);
                spawnLimit = cfgInt(ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT);
                ore = new ItemStack(Material.DIAMOND);
                break;
            case EMERALD:
                delay = cfgDouble(ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY) * speedMultiplier;
                amount = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT);
                spawnLimit = cfgInt(ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT);
                ore = new ItemStack(Material.EMERALD);
                break;
        }
        this.stack = cfgBooleanOrGlobal(ConfigPath.GENERATOR_STACK_ITEMS, ConfigPath.GENERATOR_STACK_ITEMS);
        lastSpawn = delay;
    }

    @Override
    public ITeam getBedWarsTeam() {
        return bwt;
    }

    @Override
    public GeneratorHolder getHologramHolder() {
        return item;
    }

    @Override
    public GeneratorType getType() {
        return type;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public double getDelay() {
        return delay;
    }

    @Override
    public double getNextSpawn() {
        return lastSpawn;
    }

    @Override
    public int getSpawnLimit() {
        return spawnLimit;
    }

    @Override
    public void setNextSpawn(double nextSpawn) {
        this.lastSpawn = nextSpawn;
    }

    @Override
    public void setStack(boolean stack) {
        this.stack = stack;
    }

    @Override
    public boolean isStack() {
        return stack;
    }

    @Override
    public boolean isHologramEnabled() {
        return hologram;
    }

    @Override
    public void setType(GeneratorType type) {
        this.type = type;
    }

    public void destroyData() {
        rotation.remove(this);
        location = null;
        arena = null;
        ore = null;
        bwt = null;
        holograms = null;
        item = null;
    }
}
