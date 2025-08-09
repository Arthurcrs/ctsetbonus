package com.mahghuuuls.ctsetbonus;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonusrequirements.ABonusRequirement;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Equip;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SetRequirement;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SlotData;
import com.fantasticsource.setbonus.server.ServerBonus;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("ctsetbonus.SetTweaks")
public class SetTweaks {

	// ADD EQUIP TO SET

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String equipRK) {
		addEquipToSetCore(setName, slotString, equipRK);
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String equipRK) {
		addEquipToSetCore(setName, Integer.toString(slotInt), equipRK);
	}

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			addEquipToSet(setName, slotString, equipRL);
		}
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			addEquipToSet(setName, slotInt, equipRL);
		}
	}

	// ADD BONUS TO SET

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName) {
		addBonusToSet(bonusID, bonusDescription, setName, -1, 1); // require full set and bonus always visible
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts) {
		addBonusToSet(bonusID, bonusDescription, setName, numberOfParts, 1); // bonus always visible
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		if (isLogicalClient()) {
			return;
		}

		String setId = setName.replace(" ", "");

		Set targetSet = null;
		for (Set s : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(s.id)) {
				targetSet = s;
				break;
			}
		}
		if (targetSet == null) {
			CraftTweakerAPI.logError("CTSetBonus: set '" + setName + "' not found. "
					+ "Add at least one equip to the set before adding bonuses.");
			return;
		}

		String reqString = (numberOfParts > 0) ? (setId + "." + numberOfParts) : setId;

		ServerBonus serverBonus = null;
		for (Bonus b : SetBonusData.SERVER_DATA.bonuses) {
			if (bonusID.equals(b.id)) {
				if (b instanceof ServerBonus)
					serverBonus = (ServerBonus) b;
				break;
			}
		}

		if (serverBonus == null) {
			String safeName = sanitizeBonusName(bonusDescription); // commas break parsing
			String parseableBonus = bonusID + ", " + safeName + ", " + discoveryMode + ", " + reqString;
			Bonus created = Bonus.getInstance(parseableBonus, SetBonusData.SERVER_DATA);
			if (!(created instanceof ServerBonus)) {
				CraftTweakerAPI
						.logError("CTSetBonus: failed to create bonus '" + bonusID + "' from '" + parseableBonus + "'");
				return;
			}
			SetBonusData.SERVER_DATA.bonuses.add(created);
			CraftTweakerAPI.logInfo("CTSetBonus: New bonus added " + bonusID + " (\"" + safeName + "\", mode="
					+ discoveryMode + ", req=" + reqString + ")");
			return;
		}

		ABonusRequirement req = ABonusRequirement.parse(reqString, SetBonusData.SERVER_DATA.sets);
		if (req == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad requirement '" + reqString + "'");
			return;
		}
		int desiredPieces = (numberOfParts > 0) ? numberOfParts : targetSet.getMaxNumber();
		if (hasSameSetRequirement(serverBonus, setId, desiredPieces)) {
			CraftTweakerAPI.logInfo("CTSetBonus: bonus '" + bonusID + "' already has requirement '" + reqString + "'");
			return;
		}
		serverBonus.requirements.add(req);
		CraftTweakerAPI.logInfo("CTSetBonus: Linked bonus '" + bonusID + "' -> set '" + setName + "' (pieces="
				+ (numberOfParts > 0 ? numberOfParts : "FULL") + ", mode=" + discoveryMode + ")");
	}

	// HELPERS

	private static Equip getOrAddEquipment(String equipRK) {
		String equipId = getEquipIdFromRK(equipRK);
		Equip targetEquip = null;
		for (Equip equip : SetBonusData.SERVER_DATA.equipment) {
			if (equipId.equals(equip.id)) {
				return equip;
			}
		}
		if (targetEquip == null) {
			String parsableEquip = equipId + ", " + equipRK;
			Equip createdEquip = Equip.getInstance(parsableEquip);
			if (createdEquip != null) {
				SetBonusData.SERVER_DATA.equipment.add(createdEquip);
				CraftTweakerAPI.logInfo("CTSetBonus: New equip added : " + parsableEquip);
			}
			targetEquip = createdEquip;
		}
		return targetEquip;
	}

	private static String getEquipIdFromRK(String equipRK) {
		return equipRK.replace(":", "_").replace("@", "_");
	}

	private static void addEquipToSetCore(String setName, String slotPart, String equipRK) {
		if (isLogicalClient()) {
			return;
		}

		String setId = setName.replace(" ", "");
		String equipId = getEquipIdFromRK(equipRK);

		Equip eq = getOrAddEquipment(equipRK);
		if (eq == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad equip registry key '" + equipRK + "'");
			return;
		}

		Set targetSet = findSetById(setId);

		String slotToken = slotPart + " = " + equipId;

		// if set is missing create the set; otherwise just add the slot
		if (targetSet == null) {
			if (!createSetWithSlot(setId, setName, slotToken)) {
				CraftTweakerAPI.logError("CTSetBonus: failed to create set '" + setId + "'");
				return;
			}
			CraftTweakerAPI.logInfo("CTSetBonus: New set added " + setName + " (" + setId + ")");
			CraftTweakerAPI.logInfo("CTSetBonus: Added " + equipRK + " to " + setName + " at slot " + slotPart);
			return;
		}

		if (!addSlotToSet(targetSet, slotToken)) {
			CraftTweakerAPI.logError("CTSetBonus: bad slot token '" + slotToken + "'");
			return;
		}
		CraftTweakerAPI.logInfo("CTSetBonus: Added " + equipRK + " at slot " + slotPart + " to set " + setName);
	}

	private static Set findSetById(String setId) {
		for (Set s : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(s.id))
				return s;
		}
		return null;
	}

	private static boolean createSetWithSlot(String setId, String setName, String slotToken) {
		String line = setId + ", " + setName + ", " + slotToken; // cfg-style: id, name, slot = equipId
		Set created = Set.getInstance(line, SetBonusData.SERVER_DATA);
		if (created == null)
			return false;
		SetBonusData.SERVER_DATA.sets.add(created);
		return true;
	}

	private static boolean addSlotToSet(Set set, String slotToken) {
		SlotData sd = SlotData.getInstance(slotToken, SetBonusData.SERVER_DATA);
		if (sd == null)
			return false;
		set.slotData.add(sd);
		return true;
	}

	private static String sanitizeBonusName(String name) {
		return name == null ? "" : name.replace(",", " - ");
	}

	private static boolean hasSameSetRequirement(ServerBonus bonus, String setId, int desiredPieces) {
		if (bonus == null || setId == null)
			return false;
		for (ABonusRequirement req : bonus.requirements) {
			if (req instanceof SetRequirement) {
				SetRequirement setReq = (SetRequirement) req;
				if (setReq.set != null && setId.equals(setReq.set.id)) {
					int required = (setReq.num == -1) ? setReq.set.getMaxNumber() : setReq.num;
					if (required == desiredPieces)
						return true;
				}
			}
		}
		return false;
	}

	private static boolean isLogicalClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	// DEBUG

	@ZenMethod
	public static void debugDump() {
		Side eff = FMLCommonHandler.instance().getEffectiveSide();
		CraftTweakerAPI.logInfo("CTSetBonus DEBUG (" + eff + ") sets=" + SetBonusData.SERVER_DATA.sets.size()
				+ ", equips=" + SetBonusData.SERVER_DATA.equipment.size() + ", bonuses="
				+ SetBonusData.SERVER_DATA.bonuses.size());
	}

}