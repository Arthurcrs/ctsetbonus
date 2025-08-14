package com.mahghuuuls.ctsetbonus.util;

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
		String requirementSpec = getParseableRequirement(setName, numberOfParts);
		String safeDescription = SetTweaksUtil.cleanBonusDescription(bonusDescription);
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
		return bonusName + ", " + getParseableSlotData(equipRL, slot) + ", " + enchantRL + "." + level + "." + mode;
	}

}
