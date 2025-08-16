package com.mahghuuuls.ctsetbonus.util;

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
		String requirementSpec = getParseableSetRequirement(setName, numberOfParts);
		return bonusId + ", " + safeDescription + ", " + discoveryMode + ", " + requirementSpec;
	}

	public static String getParseableSlotData(String equipRL, String slot) {
		ServerDataUtil.addEquip(equipRL);
		String equipId = IdFormatter.getEquipIdFromRL(equipRL);
		return slot + "=" + equipId;
	}

	public static String getParseableSetRequirement(String setName, int numberOfParts) {
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

	public static int parseOperation(String operation) {
		operation = operation.replace(" ", "");
		operation = operation.toLowerCase();

		switch (operation) {
		case "add":
			return 0; // + amount
		case "mult_base":
			return 1; // + base * amount
		case "mult_total":
			return 2; // * (1 + amount)
		}

		CraftTweakerAPI.logError("CTSetBonus: unknown attribute operation (defaulting to add) '" + operation
				+ "'. Options are: add | mult_base | mult_total");
		return 0;

	}

	public static int parseEnchantMode(String mode) {
		mode = mode.replace(" ", "");
		mode = mode.toLowerCase();

		switch (mode) {
		case "vanilla":
			return 0;
		case "vanilla_unlimited":
			return 1;
		case "override":
			return 2;
		case "additive":
			return 3;
		case "additive_unlimited":
			return 4;
		}

		CraftTweakerAPI.logError("CTSetBonus: unknown enchantment mode (defaulting to vanilla behavior) '" + mode
				+ "'. Options are: vanilla | vanilla_unlimited | override | additive | additive_unlimited");
		return 0;

	}

	public static String cleanBonusDescription(String bonusDescription) {
		return bonusDescription == null ? "" : bonusDescription.replace(",", " - ");
	}

}
