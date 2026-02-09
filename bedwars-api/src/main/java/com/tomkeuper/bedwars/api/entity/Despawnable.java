package com.tomkeuper.bedwars.api.entity;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.language.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class Despawnable {

    private LivingEntity e;
    private ITeam team;
    private int despawn = 250;
    private int despawnMax = 250;
    private String namePath;
    private PlayerKillEvent.PlayerKillCause deathRegularCause, deathFinalCause;
    private UUID uuid;

    private static BedWars api;

    public Despawnable(LivingEntity e, ITeam team, int despawn, String namePath, PlayerKillEvent.PlayerKillCause deathFinalCause, PlayerKillEvent.PlayerKillCause deathRegularCause) {
        this.e = e;
        if (e == null) return;
        this.uuid = e.getUniqueId();
        this.team = team;
        this.deathFinalCause = deathFinalCause;
        this.deathRegularCause = deathRegularCause;
        if (despawn != 0) {
            this.despawn = despawn;
        }
        this.despawnMax = this.despawn;
        this.namePath = namePath;
        if (api == null) api = Bukkit.getServer().getServicesManager().getRegistration(BedWars.class).getProvider();
        api.getVersionSupport().getDespawnablesList().put(uuid, this);
        this.setName();
    }

    public void refresh() {
        if (e.isDead() || e == null || team == null || team.getArena() == null) {
            api.getVersionSupport().getDespawnablesList().remove(uuid);
            if (team.getArena() == null){
                e.damage(e.getHealth()+100);
            }
            return;
        }
        setName();
        despawn--;
        if (despawn == 0) {
            e.damage(e.getHealth()+100);
            api.getVersionSupport().getDespawnablesList().remove(e.getUniqueId());
        }
    }

    private void setName() {
        int percentuale = (int) ((e.getHealth() * 100) / e.getMaxHealth() / 10);
        String base = api.getDefaultLang().m(namePath);
        if (Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME.equals(namePath) && !base.contains("%bw_time_bar%")) {
            base = "%bw_time_bar%";
        }
        String name = base.replace("%bw_despawn_time%", String.valueOf(despawn))
                .replace("%bw_health%", new String(new char[percentuale]).replace("\0", api.getDefaultLang()
                        .m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH)) + new String(new char[10 - percentuale]).replace("\0", "§7" + api.getDefaultLang()
                        .m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH)))
                .replace("%bw_time_bar%", buildTimeBar());
        if (team != null) {
            name = name.replace("%bw_team_color%", team.getColor().chat().toString())
                    .replace("%bw_team_name%", team.getDisplayName(api.getDefaultLang()));
        }
        e.setCustomName(name);
    }

    private String buildTimeBar() {
        int barLength = 1;
        if (api != null && api.getConfigs() != null && api.getConfigs().getMainConfig() != null) {
            barLength = api.getConfigs().getMainConfig().getInt(ConfigPath.GENERAL_CONFIGURATION_DESPAWNABLE_TIME_BAR_LENGTH);
        }
        if (barLength < 1) barLength = 1;
        int max = Math.max(1, despawnMax);
        double ratio = Math.max(0D, Math.min(1D, (double) despawn / max));
        int filled = (int) Math.ceil(ratio * barLength);

        String prefix = api.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_PREFIX);
        String suffix = api.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_SUFFIX);
        String filledToken = api.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_FILLED);
        String emptyToken = api.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_TIME_BAR_EMPTY);

        StringBuilder bar = new StringBuilder(prefix);
        if (team != null) {
            bar.append(team.getColor().chat());
        }
        for (int i = 0; i < filled; i++) {
            bar.append(filledToken);
        }
        for (int i = filled; i < barLength; i++) {
            bar.append(emptyToken);
        }
        bar.append(suffix);
        return bar.toString();
    }

    public LivingEntity getEntity() {
        return e;
    }

    public ITeam getTeam() {
        return team;
    }

    public int getDespawn() {
        return despawn;
    }

    public PlayerKillEvent.PlayerKillCause getDeathFinalCause() {
        return deathFinalCause;
    }

    public PlayerKillEvent.PlayerKillCause getDeathRegularCause() {
        return deathRegularCause;
    }

    public void destroy(){
        if (getEntity() != null){
            getEntity().damage(Integer.MAX_VALUE);
        }
        team = null;
        api.getVersionSupport().getDespawnablesList().remove(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LivingEntity) return ((LivingEntity) obj).getUniqueId().equals(e.getUniqueId());
        return false;
    }
}

