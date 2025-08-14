package com.mahghuuuls.ctsetbonus;

import java.util.ArrayList;
import java.util.List;

import com.mahghuuuls.ctsetbonus.slotaccumulator.SlotAccumulators;

import crafttweaker.CraftTweakerAPI;

/**
 * Collects CraftTweaker-driven actions during script parse and applies them
 * once on the server. Prevents client-side mutation and state leaks between
 * loads.
 */
public final class ScriptLoader {
	public static final List<Runnable> QUEUE = new ArrayList<>();
	public static boolean hasServerLoadedQueue;

	private ScriptLoader() {
	}

	public static void enqueue(Runnable r) {
		synchronized (QUEUE) {
			QUEUE.add(r);
		}
	}

	public static void applyQueuedTweaks() {

		final List<Runnable> batch;

		synchronized (QUEUE) {
			batch = new ArrayList<>(QUEUE);
		}

		int applied = 0;

		for (Runnable runnable : batch) {
			try {
				runnable.run();
				applied++;
			} catch (Throwable t) {
				CraftTweakerAPI.logError("CTSetBonus: deferred task failed", t);
			}
		}
		SlotAccumulators.clear();
		CraftTweakerAPI.logInfo("CTSetBonus: applied " + applied + " queued script actions on SERVER");
	}

}
