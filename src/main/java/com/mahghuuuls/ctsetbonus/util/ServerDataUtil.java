package com.mahghuuuls.ctsetbonus.util;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonusrequirements.ABonusRequirement;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Equip;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SetRequirement;
import com.fantasticsource.setbonus.server.ServerBonus;
import com.mahghuuuls.ctsetbonus.slotaccumulator.SlotAccum;

import crafttweaker.CraftTweakerAPI;

public class ServerDataUtil {

	public static Equip getEquip(String equipRL) {
		String equipId = IdFormatter.getEquipIdFromRL(equipRL);
		for (Equip equip : SetBonusData.SERVER_DATA.equipment) {
			if (equipId.equals(equip.id)) {
				return equip;
			}
		}
		return null;
	}

	public static Set getSet(String setName) {
		String setId = IdFormatter.getSetIdFromName(setName);
		for (Set set : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(set.id))
				return set;
		}
		return null;
	}

	public static ServerBonus getBonus(String bonusName) {
		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (bonusId.equals(bonus.id) && bonus instanceof ServerBonus)
				return (ServerBonus) bonus;
		}
		CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusName + "' not found. Create/link it first.");
		return null;
	}

	public static void addEquip(String equipRL) {
		String parseableEquip = ParseUtil.getParseableEquip(equipRL);
		Equip createdEquip = Equip.getInstance(parseableEquip);
		if (createdEquip != null) {
			SetBonusData.SERVER_DATA.equipment.add(createdEquip);
			CraftTweakerAPI.logInfo("CTSetBonus: New equip added : " + parseableEquip);
		}
	}

	public static void addSet(SlotAccum slotAcc) {
		String slotToken = slotAcc.buildSlotToken();
		String parseableSet = ParseUtil.getParseableSet(slotAcc.setName, slotToken);
		Set createdSet = Set.getInstance(parseableSet, SetBonusData.SERVER_DATA);
		if (createdSet == null) {
			CraftTweakerAPI
					.logError("CTSetBonus: failed to create set '" + slotAcc.setId + "' from '" + parseableSet + "'");
			return;
		}
		SetBonusData.SERVER_DATA.sets.add(createdSet);
		slotAcc.set = createdSet;

		if (createdSet.slotData.isEmpty()) {
			CraftTweakerAPI
					.logError("CTSetBonus: internal error: created set has no slot data for '" + slotToken + "'");
			return;
		}
		slotAcc.slotData = createdSet.slotData.get(createdSet.slotData.size() - 1);

		CraftTweakerAPI.logInfo("CTSetBonus: New set added " + slotAcc.setName + " (" + slotAcc.setId + ")");
	}

	public static void addBonus(String bonusName) {
		// TODO
	}

	/**
	 * Checks if a ServerBonus already has a SetRequirement for the same set and
	 * piece count (full or partial). Prevents duplicate requirements.
	 */
	public static boolean hasSameSetRequirement(ServerBonus bonus, String setName, int desiredPieces) {
		String setId = IdFormatter.getSetIdFromName(setName);
		if (bonus == null || setId == null)
			return false;
		for (ABonusRequirement bonusReq : bonus.requirements) {
			if (bonusReq instanceof SetRequirement) {
				SetRequirement setReq = (SetRequirement) bonusReq;
				if (setReq.set != null && setId.equals(setReq.set.id)) {
					int required = (setReq.num == -1) ? setReq.set.getMaxNumber() : setReq.num;
					if (required == desiredPieces)
						return true;
				}
			}
		}
		return false;
	}

}
