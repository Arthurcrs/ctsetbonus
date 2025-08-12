package com.mahghuuuls.ctsetbonus.zen;

import com.fantasticsource.setbonus.SetBonusData;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("ctsetbonus.Debug")
public class Debug {

	@ZenMethod
	public static void debugData() {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		CraftTweakerAPI.logInfo("CTSetBonus DEBUG (" + side + ") sets=" + SetBonusData.SERVER_DATA.sets.size()
				+ ", equips=" + SetBonusData.SERVER_DATA.equipment.size() + ", bonuses="
				+ SetBonusData.SERVER_DATA.bonuses.size());
	}

}
