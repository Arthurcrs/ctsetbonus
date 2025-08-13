package com.mahghuuuls.ctsetbonus.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonuselements.ABonusElement;
import com.fantasticsource.setbonus.server.ServerBonus;

import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class SetTweaksUtil {

	/**
	 * Replaces commas in user-facing bonus names, avoiding CSV-style parser issues
	 * when constructing config-like lines.
	 */
	public static String cleanBonusDescription(String bonusDescription) {
		return bonusDescription == null ? "" : bonusDescription.replace(",", " - ");
	}

	/**
	 * Adds a parsed bonus element (potion/attribute/enchant) to a ServerBonus using
	 * reflection. Handles both possible field names (elements or bonusElements).
	 * Returns success/failure.
	 */
	@SuppressWarnings("unchecked")
	public static boolean tryAttachElementToBonus(ServerBonus serverBonus, ABonusElement bonusElem) {
		try {
			Field field = Bonus.class.getDeclaredField("elements");
			field.setAccessible(true);
			((List<ABonusElement>) field.get(serverBonus)).add(bonusElem);
			return true;
		} catch (NoSuchFieldException nf) {
			try {
				Field field2 = Bonus.class.getDeclaredField("bonusElements");
				field2.setAccessible(true);
				((List<ABonusElement>) field2.get(serverBonus)).add(bonusElem);
				return true;
			} catch (Throwable t2) {
				CraftTweakerAPI.logError("CTSetBonus: could not find elements list on ServerBonus", t2);
				return false;
			}
		} catch (Throwable t) {
			CraftTweakerAPI.logError("CTSetBonus: reflection error while attaching element", t);
			return false;
		}
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

	/**
	 * Returns true when running on the logical client (Side.CLIENT). Used to avoid
	 * mutating server-only data structures on the client.
	 */
	public static boolean instanceIsClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

}
