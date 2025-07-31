package com.saunhardy.afkstatus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AFKManager {
    private static final Set<UUID> afkPlayers = new HashSet<>();

    public static void setAFK(UUID uuid, boolean afk) {
        if (afk) {
            afkPlayers.add(uuid);
        } else {
            afkPlayers.remove(uuid);
        }
    }

    public static boolean isAFK(UUID uuid) {
        return afkPlayers.contains(uuid);
    }
}
