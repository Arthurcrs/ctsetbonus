package com.mahghuuuls.ctsetbonus.util;

import java.util.HashSet;
import java.util.Set;

public class IdRegistry {
	public static final Set<String> addedEquipIds = new HashSet<>();
	public static final Set<String> addedSetIds = new HashSet<>();
	public static final Set<String> addedBonusIds = new HashSet<>();

	private IdRegistry() {

	}

	public static void addEquip(String equipRL) {
		String equipId = IdFormatter.getEquipIdFromRL(equipRL);
		addedEquipIds.add(equipId);
	}

	public static void addSet(String setName) {
		String setId = IdFormatter.getSetIdFromName(setName);
		addedSetIds.add(setId);
	}

	public static void addBonus(String bonusName) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		addedBonusIds.add(bonusId);
	}

	public static boolean equipIdWasAdded(String equipId) {
		return addedEquipIds.contains(equipId);
	}

	public static boolean setIdWasAdded(String setId) {
		return addedSetIds.contains(setId);
	}

	public static boolean bonusIdWasAdded(String bonusId) {
		return addedBonusIds.contains(bonusId);
	}

	public static void clear() {
		addedSetIds.clear();
		addedBonusIds.clear();
		addedEquipIds.clear();
	}
}
