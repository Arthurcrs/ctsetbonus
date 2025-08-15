package com.mahghuuuls.ctsetbonus.util;

import java.util.Locale;

import crafttweaker.CraftTweakerAPI;

public class ParseUtil {

	public static String getParseableSet(String setName, String slotToken) {
		String setId = IdFormatter.getSetIdFromName(setName);
		return setId + ", " + setName + ", " + slotToken;
	}

	public static String getParseableEquip(String equipRL) {
		return IdFormatter.getEquipIdFromRL(equipRL) + ", " + equipRL;
	}

	public static String getParseableBonus(String bonusName, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		String safeDescription = cleanBonusDescription(bonusDescription);
		String requirementSpec = getParseableRequirement(setName, numberOfParts);
		return bonusId + ", " + safeDescription + ", " + discoveryMode + ", " + requirementSpec;
	}

	public static String getParseableSlotData(String equipRL, String slot) {
		ServerDataUtil.addEquip(equipRL);
		String equipId = IdFormatter.getEquipIdFromRL(equipRL);
		return slot + "=" + equipId;
	}

	public static String getParseableRequirement(String setName, int numberOfParts) {
		String setId = IdFormatter.getSetIdFromName(setName);
		return (numberOfParts > 0) ? (setId + "." + numberOfParts) : setId;
	}

	public static String getParseablePotionBonus(String bonusName, String effectRL, int level, int duration,
			int interval) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		String effectToken = effectRL + "." + level + "." + duration + "." + interval;
		return bonusId + ", " + effectToken;
	}

	public static String getParseableModifierBonus(String bonusName, String attribute, double amount,
			int operationCode) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		String spec = attribute + " = " + amount + (operationCode != 0 ? (" @ " + operationCode) : "");
		return bonusId + ", " + spec;
	}

	public static String getParseableEnchantmentBonus(String bonusName, String slot, String equipRL, String enchantRL,
			int level, int mode) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		return bonusId + ", " + getParseableSlotData(equipRL, slot) + ", " + enchantRL + "." + level + "." + mode;
	}

	/**
	 * Parses attribute modifier operation from string or number. Accepts add |
	 * mult_base | mult_total (or 0|1|2). Defaults to add with an error log on
	 * unknown inputs.
	 */
	public static int parseOperation(String operation) {

		if (operation == null) {
			return 0;
		}

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

	public static String cleanBonusDescription(String bonusDescription) {
		return bonusDescription == null ? "" : bonusDescription.replace(",", " - ");
	}

}
