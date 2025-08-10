package com.mahghuuuls.ctsetbonus;

import java.util.ArrayList;
import java.util.List;

import crafttweaker.CraftTweakerAPI;

/**
 * Collects CraftTweaker-driven actions during script parse and applies them
 * once on the server. Prevents client-side mutation and state leaks between
 * loads.
 */
public final class ScriptLoader {
	private static final List<Runnable> QUEUE = new ArrayList<>();

	private ScriptLoader() {
	}

	/**
	 * Queues a deferred script action. Thread-safe. Zen methods can run on the
	 * client; we defer to apply on the server only.
	 */
	public static void enqueue(Runnable r) {
		synchronized (QUEUE) {
			QUEUE.add(r);
		}
	}

	/**
	 * Runs all queued actions (server-side), then clears queue and per-slot
	 * accumulators. Ensures changes apply exactly once, in order, with a clean
	 * state afterward.
	 */
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

		SetTweaks.clearSlotAccumulators();
		CraftTweakerAPI.logInfo("CTSetBonus: applied " + applied + " queued script actions on SERVER");
	}

	/**
	 * Clears slot accumulators.
	 */
	public static void resetState() {
		SetTweaks.clearSlotAccumulators();
	}
}
