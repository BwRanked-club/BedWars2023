package com.tomkeuper.bedwars.support.party;

import com.google.gson.JsonObject;
import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.party.Party;
import com.tomkeuper.bedwars.arena.Misc;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class Internal implements Party {
    private static final List<Internal.Party> parties = new ArrayList<>();

    @NotNull
    @Contract(pure = true)
    public static List<Party> getParties() {
        return Collections.unmodifiableList(parties);
    }

    @Override
    public boolean hasParty(Player p) {
        for (Party party : getParties()) {
            if (containsPlayer(party.members, p)) return true;
        }
        return false;
    }

    @Override
    public int partySize(Player p) {
        for (Party party : getParties()) {
            if (containsPlayer(party.members, p)) {
                return party.members.size();
            }
        }
        return 0;
    }

    @Override
    public boolean isOwner(Player p) {
        for (Party party : getParties()) {
            if (containsPlayer(party.members, p)) {
                if (samePlayer(party.owner, p)) return true;
            }
        }
        return false;
    }

    @Override
    public List<Player> getMembers(Player owner) {
        for (Party party : getParties()) {
            if (containsPlayer(party.members, owner)) {
                return party.members;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void createParty(Player owner, Player... members) {
        Party p = new Party(owner);
        p.addMember(owner);
        for (Player mem : members) {
            p.addMember(mem);
        }
    }

    @Override
    public void addMember(Player owner, Player member) {
        if (owner == null || member == null) return;
        Internal.Party p = getParty(owner);
        if (p == null) return;
        p.addMember(member);
    }

    @Override
    public void removeFromParty(Player member) {
        for (Party p : new ArrayList<>(getParties())) {
            if (samePlayer(p.owner, member)) {
                disband(member);
            } else if (containsPlayer(p.members, member)) {
                for (Player mem : p.members) {
                    Language language = Language.getPlayerLanguage(mem);
                    mem.sendMessage(getMsg(language, member, Messages.COMMAND_PARTY_LEAVE_SUCCESS).replace("%bw_playername%", member.getName()).replace("%bw_player%", Misc.getPlayerName(member)));
                }
                p.members.removeIf(mem -> samePlayer(mem, member));

                if (BedWars.getRedisConnection() != null) {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "PR"); // PR = Party Remove
                    json.addProperty("owner", member.getUniqueId().toString());
                    BedWars.getRedisConnection().sendMessage(json.toString());
                }

                if (p.members.isEmpty() || p.members.size() == 1) {
                    disband(p.owner);
                    parties.remove(p);
                }
                return;
            }
        }
    }

    @Override
    public void disband(Player owner) {
        Internal.Party pa = getParty(owner);
        if (pa == null) return;
        for (Player p : pa.members) {
            p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_DISBAND_SUCCESS));
        }
        pa.members.clear();
        Internal.parties.remove(pa);

        if (BedWars.getRedisConnection() != null) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "PD"); // PD = Party Disband
            json.addProperty("owner", owner.getUniqueId().toString());
            BedWars.getRedisConnection().sendMessage(json.toString());
        }
    }

    @Override
    public boolean isMember(Player owner, Player check) {
        for (Party p : parties) {
            if (samePlayer(p.owner, owner)) {
                if (containsPlayer(p.members, check)) return true;
            }
        }
        return false;
    }

    @Override
    public void removePlayer(Player owner, Player target) {
        Party p = getParty(owner);
        if (p != null) {
            if (containsPlayer(p.members, target)) {
                for (Player mem : p.members) {
                    Language language = Language.getPlayerLanguage(mem);
                    mem.sendMessage(getMsg(language, mem, Messages.COMMAND_PARTY_REMOVE_SUCCESS).replace("%bw_playername%", mem.getName()).replace("%bw_player%", Misc.getPlayerName(mem)));
                }
                p.members.removeIf(mem -> samePlayer(mem, target));
                if (p.members.isEmpty() || p.members.size() == 1) {
                    disband(p.owner);
                    parties.remove(p);
                }
            }
        }
    }

    @Override
    public Player getOwner(Player member) {
        for (Internal.Party party : Internal.getParties()) {
            if (containsPlayer(party.members, member)) {
                return party.owner;
            }
        }
        return null;
    }

    @Override
    public void promote(@NotNull Player owner, @NotNull Player target) {
        Party p = getParty(owner);
        if (p != null) {
            p.owner = target;
        }
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Nullable
    private Party getParty(Player owner) {
        for (Party p : getParties()) {
            if (samePlayer(p.getOwner(), owner) || containsPlayer(p.members, owner)) return p;
        }
        return null;
    }

    private static boolean samePlayer(Player a, Player b) {
        if (a == null || b == null) return false;
        return a.getUniqueId().equals(b.getUniqueId());
    }

    private static boolean containsPlayer(List<Player> members, Player player) {
        if (members == null || player == null) return false;
        for (Player member : members) {
            if (samePlayer(member, player)) return true;
        }
        return false;
    }

    static class Party {

        private final List<Player> members = new ArrayList<>();
        private Player owner;

        public Party(Player p) {
            owner = p;
            Internal.parties.add(this);
        }

        public Player getOwner() {
            return owner;
        }

        void addMember(Player p) {
            if (containsPlayer(members, p)) return;
            members.add(p);
        }
    }
}
