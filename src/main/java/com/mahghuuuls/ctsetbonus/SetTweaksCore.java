package com.mahghuuuls.ctsetbonus;

import java.util.Locale;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementAttributeModifier;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementEnchantment;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementPotionEffect;
import com.fantasticsource.setbonus.common.bonusrequirements.ABonusRequirement;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Equip;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SlotData;
import com.fantasticsource.setbonus.server.ServerBonus;
import com.mahghuuuls.ctsetbonus.slotaccumulator.SlotAccum;
import com.mahghuuuls.ctsetbonus.slotaccumulator.SlotAccumulators;
import com.mahghuuuls.ctsetbonus.util.IdFormatter;
import com.mahghuuuls.ctsetbonus.util.ParseUtil;
import com.mahghuuuls.ctsetbonus.util.ServerDataUtil;
import com.mahghuuuls.ctsetbonus.util.SetTweaksUtil;

import crafttweaker.CraftTweakerAPI;

public class SetTweaksCore {

	public static void addEquipToSetCore(String setName, String slotPart, String equipRL) {

		if (SetTweaksUtil.instanceIsClient()) {
			return;
		}

		Equip equipment = ServerDataUtil.getEquip(equipRL);
		if (equipment == null) {
			ServerDataUtil.addEquip(equipRL);
		}

		SlotAccum slotAccumulator = SlotAccumulators.getOrCreateAccum(setName, slotPart);
		slotAccumulator.addEquip(equipRL);

		String slotToken = slotAccumulator.buildSlotToken();

		if (slotAccumulator.set == null) {
			ServerDataUtil.addSet(slotAccumulator);
			return;
		}

		if (slotAccumulator.slotData != null) {
			slotAccumulator.set.slotData.remove(slotAccumulator.slotData);
			slotAccumulator.slotData = null;
		}

		SlotData slotData = SlotData.getInstance(slotToken, SetBonusData.SERVER_DATA);
		if (slotData == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad slot token '" + slotToken + "'");
			return;
		}
		slotAccumulator.set.slotData.add(slotData);
		slotAccumulator.slotData = slotData;

		CraftTweakerAPI.logInfo("CTSetBonus: Added " + slotAccumulator.equipIds.size() + " option(s) to "
				+ slotAccumulator.setName + " at slot " + slotAccumulator.slotKey);
	}

	public static void addBonusToSetCore(String bonusName, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {

		if (SetTweaksUtil.instanceIsClient()) {
			return;
		}

		if (discoveryMode < 0 || discoveryMode > 2) {
			CraftTweakerAPI
					.logWarning("CTSetBonus: discovery mode " + discoveryMode + " is outside 0..2; attempting as-is.");
		}

		Set setRef = ServerDataUtil.getSet(setName);
		if (setRef == null) {
			return;
		}

		String parseableRequirement = ParseUtil.getParseableRequirement(setName, numberOfParts);

		ServerBonus serverBonus = ServerDataUtil.getBonus(bonusName);

		if (serverBonus == null) {
			String safeDescription = SetTweaksUtil.cleanBonusDescription(bonusDescription); // commas break parsing
			String parseableBonus = ParseUtil.getParseableBonus(bonusName, bonusDescription, setName, numberOfParts,
					discoveryMode);
			Bonus createdBonus = Bonus.getInstance(parseableBonus, SetBonusData.SERVER_DATA);
			if (!(createdBonus instanceof ServerBonus)) {
				CraftTweakerAPI.logError(
						"CTSetBonus: failed to create bonus '" + bonusName + "' from '" + parseableBonus + "'");
				return;
			}
			SetBonusData.SERVER_DATA.bonuses.add(createdBonus);
			CraftTweakerAPI.logInfo("CTSetBonus: New bonus added " + bonusName + " (\"" + safeDescription + "\", mode="
					+ discoveryMode + ", req=" + parseableRequirement + ")");
			return;
		}

		ABonusRequirement bonusReq = ABonusRequirement.parse(parseableRequirement, SetBonusData.SERVER_DATA.sets);
		if (bonusReq == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad requirement '" + parseableRequirement + "'");
			return;
		}
		int desiredPieces = (numberOfParts > 0) ? numberOfParts : setRef.getMaxNumber();
		if (ServerDataUtil.hasSameSetRequirement(serverBonus, setName, desiredPieces)) {
			CraftTweakerAPI.logInfo(
					"CTSetBonus: bonus '" + bonusName + "' already has requirement '" + parseableRequirement + "'");
			return;
		}
		serverBonus.requirements.add(bonusReq);
		CraftTweakerAPI.logInfo("CTSetBonus: Linked bonus '" + bonusName + "' -> set '" + setName + "' (pieces="
				+ (numberOfParts > 0 ? numberOfParts : "FULL") + ", mode=" + discoveryMode + ")");
	}

	public static void addPotionEffectToBonusCore(String bonusName, String effectRL, int level, int duration,
			int interval) {

		if (SetTweaksUtil.instanceIsClient()) {
			return;
		}

		ServerBonus serverBonus = ServerDataUtil.getBonus(bonusName);
		if (serverBonus == null) {
			return;
		}

		String parsablePotionBonus = ParseUtil.getParseablePotionBonus(bonusName, effectRL, level, duration, interval);

		BonusElementPotionEffect potionElement = BonusElementPotionEffect.getInstance(parsablePotionBonus,
				SetBonusData.SERVER_DATA);

		if (potionElement == null) {
			CraftTweakerAPI.logError("CTSetBonus: failed to parse potion effect for bonus '" + bonusName + "'");
			return;
		}

		if (!SetTweaksUtil.tryAttachElementToBonus(serverBonus, potionElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach potion element to bonus '" + bonusName + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added potion " + effectRL + " (lvl=" + level + ", dur=" + duration
				+ ", interval=" + interval + ") to bonus '" + bonusName + "'");
	}

	public static void addAttributeModToBonusCore(String bonusName, String attribute, double amount,
			int operationCode) {

		if (SetTweaksUtil.instanceIsClient()) {
			return;
		}

		ServerBonus serverBonus = ServerDataUtil.getBonus(bonusName);
		if (serverBonus == null) {
			return;
		}

		String parseableModifier = ParseUtil.getParseableModifierBonus(bonusName, attribute, amount, operationCode);

		BonusElementAttributeModifier attModElement = BonusElementAttributeModifier.getInstance(parseableModifier,
				SetBonusData.SERVER_DATA);

		if (attModElement == null) {
			CraftTweakerAPI.logError("CTSetBonus: failed to build attribute element from '" + parseableModifier + "'");
			return;
		}

		if (!SetTweaksUtil.tryAttachElementToBonus(serverBonus, attModElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach attribute element to bonus '" + bonusName + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added attribute " + attribute + " = " + amount + " @ " + operationCode
				+ " to bonus '" + bonusName + "'");
	}

	public static void addEnchantmentToBonusCore(String bonusName, String slotPart, String equipRL, String enchantRL,
			int level, int mode) {

		if (SetTweaksUtil.instanceIsClient())
			return;

		ServerBonus serverBonus = ServerDataUtil.getBonus(bonusName);
		if (serverBonus == null) {
			return;
		}

		String slotDataSpec;
		if (equipRL != null && !equipRL.isEmpty()) {

			Equip targetEquip = ServerDataUtil.getEquip(equipRL);
			if (targetEquip == null) {
				CraftTweakerAPI.logError("CTSetBonus: unknown item '" + equipRL + "' for enchant target.");
				return;
			}
			ServerDataUtil.addEquip(equipRL);
			String equipId = IdFormatter.getEquipIdFromRL(equipRL);
			String lhs = slotPart.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
			slotDataSpec = lhs + "=" + equipId;
		} else {
			int eqIdx = slotPart.indexOf('=');
			if (eqIdx >= 0) {
				CraftTweakerAPI.logWarning("CTSetBonus: '=' found in slot but no itemRL provided; "
						+ "ignoring RHS and targeting the slot only.");
				slotPart = slotPart.substring(0, eqIdx).trim();
			}
			slotDataSpec = slotPart.toLowerCase(Locale.ROOT);
		}

		SlotData slotData = SlotData.getInstance(slotDataSpec, SetBonusData.SERVER_DATA);
		if (slotData == null) {
			CraftTweakerAPI.logError("CTSetBonus: invalid slot selector '" + slotDataSpec + "'. "
					+ "Use 'head', 'chest', 'legs', 'feet', 'mainhand', 'offhand', a number, or 'slot=modid:item'.");
			return;
		}

		String enchantToken = enchantRL + "." + level + "." + mode;
		String parseableEnchantmentBonus = bonusName + ", " + slotDataSpec + ", " + enchantToken;

		BonusElementEnchantment enchantElement = BonusElementEnchantment.getInstance(parseableEnchantmentBonus,
				SetBonusData.SERVER_DATA);
		if (enchantElement == null) {
			CraftTweakerAPI.logWarning(
					"CTSetBonus: enchant parse failed for '" + parseableEnchantmentBonus + "'. Retrying with mode=0.");
			String retrySpec = bonusName + ", " + slotDataSpec + ", " + enchantRL + "." + level + ".0";
			enchantElement = BonusElementEnchantment.getInstance(retrySpec, SetBonusData.SERVER_DATA);
			if (enchantElement == null) {
				CraftTweakerAPI.logError("CTSetBonus: enchant parse still failed with fallback. Line: " + retrySpec);
				return;
			}
		}

		if (!SetTweaksUtil.tryAttachElementToBonus(serverBonus, enchantElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach enchantment element to bonus '" + bonusName + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added enchant " + enchantRL + " (lvl=" + level + ", mode=" + mode
				+ ") to bonus '" + bonusName + "' targeting '" + slotDataSpec + "'");
	}

}