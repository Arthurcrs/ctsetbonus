package com.mahghuuuls.ctsetbonus.util;

import java.util.Locale;

import com.fantasticsource.setbonus.common.bonuselements.ABonusElement;
import com.fantasticsource.setbonus.server.ServerBonus;

import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class SetTweaksUtil {

	public static String cleanBonusDescription(String bonusDescription) {
		return bonusDescription == null ? "" : bonusDescription.replace(",", " - ");
	}

	public static boolean attachElementToBonus(ServerBonus serverBonus, ABonusElement bonusElem) {
		serverBonus.bonusElements.add(bonusElem);
		return true;
	}

	/**
	 * Parses attribute modifier operation from string or number. Accepts add |
	 * mult_base | mult_total (or 0|1|2). Defaults to add with an error log on
	 * unknown inputs.
	 */
	public static int parseOperation(String operation) {
		if (operation == null)
			return 0;
		switch (operation.trim().toLowerCase(Locale.ROOT)) {
		case "add":
			return 0; // + amount
		case "mult_base":
			return 1; // + base * amount
		case "mult_total":
			return 2; // * (1 + amount)
		default:
			try {
				int operationInt = Integer.parseInt(operation);
				if (operationInt >= 0 && operationInt <= 2)
					return operationInt;
			} catch (NumberFormatException ignored) {
			}
			CraftTweakerAPI.logError("CTSetBonus: unknown attribute operation '" + operation
					+ "'. Use add | mult_base | mult_total (defaulting to add).");
			return 0;
		}
	}

	public static boolean instanceIsClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

}
