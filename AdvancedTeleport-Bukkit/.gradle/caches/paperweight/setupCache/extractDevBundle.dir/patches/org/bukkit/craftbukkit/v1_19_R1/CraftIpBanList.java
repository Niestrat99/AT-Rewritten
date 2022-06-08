package org.bukkit.craftbukkit.v1_19_R1;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

public class CraftIpBanList implements org.bukkit.BanList {
    private final IpBanList list;

    public CraftIpBanList(IpBanList list) {
        this.list = list;
    }

    @Override
    public org.bukkit.BanEntry getBanEntry(String target) {
        Validate.notNull(target, "Target cannot be null");

        IpBanListEntry entry = (IpBanListEntry) this.list.get(target);
        if (entry == null) {
            return null;
        }

        return new CraftIpBanEntry(target, entry, this.list);
    }

    @Override
    public org.bukkit.BanEntry addBan(String target, String reason, Date expires, String source) {
        Validate.notNull(target, "Ban target cannot be null");

        IpBanListEntry entry = new IpBanListEntry(target, new Date(),
                StringUtils.isBlank(source) ? null : source, expires,
                StringUtils.isBlank(reason) ? null : reason);

        this.list.add(entry);

        try {
            this.list.save();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save banned-ips.json, {0}", ex.getMessage());
        }

        return new CraftIpBanEntry(target, entry, this.list);
    }

    @Override
    public Set<org.bukkit.BanEntry> getBanEntries() {
        ImmutableSet.Builder<org.bukkit.BanEntry> builder = ImmutableSet.builder();
        for (String target : this.list.getUserList()) {
            builder.add(new CraftIpBanEntry(target, (IpBanListEntry) this.list.get(target), this.list));
        }

        return builder.build();
    }

    @Override
    public boolean isBanned(String target) {
        Validate.notNull(target, "Target cannot be null");

        return this.list.isBanned(InetSocketAddress.createUnresolved(target, 0));
    }

    @Override
    public void pardon(String target) {
        Validate.notNull(target, "Target cannot be null");

        this.list.remove(target);
    }
}
