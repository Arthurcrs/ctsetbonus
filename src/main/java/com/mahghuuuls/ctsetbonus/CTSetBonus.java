package com.mahghuuuls.ctsetbonus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CTSetBonus.MOD_ID, name = CTSetBonus.NAME, version = CTSetBonus.VERSION)
public class CTSetBonus {
	public static final String MOD_ID = "ctsetbonus";
	public static final String NAME = "CTSetBonus";
	public static final String VERSION = "0.1.0";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/**
	 * Server is starting: run all deferred Zen-script actions now (server thread).
	 * Why: guarantees consistent, server-only mutation of Set Bonus data.
	 */
	@Mod.EventHandler
	public void onServerAboutToStart(FMLServerStartingEvent event) {
		ScriptLoader.applyQueuedTweaks();
	}

	/**
	 * Server is stopping: drop any queued work and temp accumulators. Why: avoid
	 * state leaking across worlds/reloads.
	 */
	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		ScriptLoader.resetState();
	}

}