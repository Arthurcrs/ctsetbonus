package com.mahghuuuls.ctsetbonus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CTSetBonus.MOD_ID, name = CTSetBonus.NAME, version = CTSetBonus.VERSION, dependencies = CTSetBonus.DEPENDENCIES)
public class CTSetBonus {
	public static final String MOD_ID = "ctsetbonus";
	public static final String NAME = "CTSetBonus";
	public static final String VERSION = "0.3.0";
	public static final String DEPENDENCIES = "required-after:crafttweaker;required-after:setbonus";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Mod.EventHandler
	public void onServerStarted(FMLServerStartingEvent event) {
		ScriptLoader.applyQueuedTweaks();
	}

}
