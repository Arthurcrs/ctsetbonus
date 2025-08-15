package com.mahghuuuls.ctsetbonus.util;

public class IdFormatter {

	public static String getEquipIdFromRL(String equipRL) {
		return equipRL.replace(":", "_").replace("@", "_");
	}

	public static String getSetIdFromName(String setName) {
		return setName.replace(" ", "");
	}

	public static String getBonusIdFromName(String bonusName) {
		return bonusName.replace(" ", "");
	}

}
