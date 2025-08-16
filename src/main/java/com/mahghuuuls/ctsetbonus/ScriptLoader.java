package com.mahghuuuls.ctsetbonus;

import java.util.ArrayList;
import java.util.List;

import com.mahghuuuls.ctsetbonus.slotaccumulator.SlotAccumulators;

import crafttweaker.CraftTweakerAPI;

public final class ScriptLoader {
	public static final List<Runnable> QUEUE = new ArrayList<>();

	private ScriptLoader() {
	}

	public static void enqueue(Runnable runnable) {
		synchronized (QUEUE) {
			QUEUE.add(runnable);
		}
	}

	public static void applyQueuedTweaks() {
		final List<Runnable> batch;

		synchronized (QUEUE) {
			batch = new ArrayList<>(QUEUE);
		}

		int numActionsApplied = 0;

		for (Runnable runnable : batch) {
			try {
				runnable.run();
				numActionsApplied++;
			} catch (Throwable t) {
				CraftTweakerAPI.logError("CTSetBonus: deferred task failed", t);
			}
		}

		SlotAccumulators.clear();
		CraftTweakerAPI.logInfo("CTSetBonus: applied " + numActionsApplied + " queued script actions on SERVER");
	}

}
