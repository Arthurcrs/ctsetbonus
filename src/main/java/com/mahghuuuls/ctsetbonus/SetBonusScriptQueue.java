package com.mahghuuuls.ctsetbonus;

import java.util.ArrayList;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public final class SetBonusScriptQueue {
	private static final List<Runnable> QUEUE = new ArrayList<>();

	public static void enqueue(Runnable r) {
		synchronized (QUEUE) {
			QUEUE.add(r);
		}
	}

	public static void flushOnServerStart(MinecraftServer srv) {
		final List<Runnable> todo;
		synchronized (QUEUE) {
			todo = new ArrayList<>(QUEUE);
		}

		int applied = 0;
		for (Runnable r : todo) {
			try {
				r.run();
				applied++;
			} catch (Throwable t) {
				CraftTweakerAPI.logError("CTSetBonus: deferred task failed", t);
			}
		}

		CraftTweakerAPI.logInfo("CTSetBonus: applied " + applied + " queued script actions on SERVER");
	}
}