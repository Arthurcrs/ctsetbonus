package com.mahghuuuls.ctsetbonus.cooldowntracker;

import java.util.HashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CooldownTracker {

	private static HashMap<CooldownKey, CooldownData> activeCooldowns = new HashMap<>();

	public static void startCooldown(String playerId, String cooldownId, long durationTicks) {
		long currentTime = getServerTicks();
		CooldownKey key = new CooldownKey(playerId, cooldownId);
		activeCooldowns.put(key, new CooldownData(durationTicks, currentTime));
	}

	public static boolean onCooldown(String playerId, String cooldownId) {
		CooldownKey cooldownKey = new CooldownKey(playerId, cooldownId);
		CooldownData cooldownData = activeCooldowns.get(cooldownKey);

		if (cooldownData == null) {
			return false;
		}

		long currentTime = getServerTicks();
		long cooldownStartTime = cooldownData.startTime;
		long cooldownDuration = cooldownData.durationTicks;
		long elapsedTime = currentTime - cooldownStartTime;

		if (elapsedTime < cooldownDuration) {
			return true;
		}

		activeCooldowns.remove(cooldownKey);
		return false;
	}

	private static long getServerTicks() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer overworld = server.getWorld(0);
		return overworld.getTotalWorldTime();
	}

}
