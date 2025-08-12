package com.mahghuuuls.ctsetbonus.zen;

import com.mahghuuuls.ctsetbonus.ScriptLoader;
import com.mahghuuuls.ctsetbonus.SetTweaksCore;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("ctsetbonus.SetTweaks")
public class SetTweaks {

	// ADD EQUIP TO SET

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String equipRL) {
		ScriptLoader.enqueue(() -> SetTweaksCore.addEquipToSetCore(setName, slotString, equipRL));

	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String equipRL) {
		ScriptLoader.enqueue(() -> SetTweaksCore.addEquipToSetCore(setName, Integer.toString(slotInt), equipRL));
	}

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			ScriptLoader.enqueue(() -> SetTweaksCore.addEquipToSetCore(setName, slotString, equipRL));
		}
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			ScriptLoader.enqueue(() -> SetTweaksCore.addEquipToSetCore(setName, Integer.toString(slotInt), equipRL));
		}
	}

	// ADD BONUS TO SET

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName) {
		ScriptLoader.enqueue(() -> SetTweaksCore.addBonusToSetCore(bonusID, bonusDescription, setName, -1, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts) {
		ScriptLoader
				.enqueue(() -> SetTweaksCore.addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		ScriptLoader.enqueue(() -> SetTweaksCore.addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts,
				discoveryMode));
	}

	// ADD POTION EFFECT ELEMENT TO BONUS

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRL, int level) {
		ScriptLoader.enqueue(
				() -> SetTweaksCore.addPotionEffectToBonusCore(bonusID, effectRL, level, Integer.MAX_VALUE, 0));
	}

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRL, int level, int duration, int interval) {
		ScriptLoader
				.enqueue(() -> SetTweaksCore.addPotionEffectToBonusCore(bonusID, effectRL, level, duration, interval));
	}

	// ADD ATTRIBUTE MOD ELEMENT TO BONUS

	@ZenMethod
	public static void addAttributeModToBonus(String bonusID, String attribute, double amount, String operation) {
		int operationCode = SetTweaksCore.parseOperation(operation);
		ScriptLoader.enqueue(() -> SetTweaksCore.addAttributeModToBonusCore(bonusID, attribute, amount, operationCode));
	}

	// ADD ENCHANTMENT ELEMENT TO BONUS

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotString, String enchantRL, int level) {
		ScriptLoader
				.enqueue(() -> SetTweaksCore.addEnchantmentToBonusCore(bonusID, slotString, "", enchantRL, level, 0));
	}

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotSpec, String itemRL, String enchantRL,
			int level, int mode) {
		ScriptLoader.enqueue(
				() -> SetTweaksCore.addEnchantmentToBonusCore(bonusID, slotSpec, itemRL, enchantRL, level, mode));
	}

}
