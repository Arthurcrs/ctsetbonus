package com.mahghuuuls.ctsetbonus;

import java.lang.reflect.Field;
import java.util.List;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonuselements.ABonusElement;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementAttributeModifier;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementPotionEffect;
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
		SetBonusScriptQueue.enqueue(() -> addEquipToSetCore(setName, slotString, equipRK));

	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String equipRK) {
		SetBonusScriptQueue.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRK));
	}

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String[] equipsRK) {
		for (String equipRK : equipsRK) {
			SetBonusScriptQueue.enqueue(() -> addEquipToSetCore(setName, slotString, equipRK));
		}
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String[] equipsRK) {
		for (String equipRK : equipsRK) {
			SetBonusScriptQueue.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRK));
		}
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

	// ADD BONUS TO SET

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName) {
		SetBonusScriptQueue.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, -1, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts) {
		SetBonusScriptQueue.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		SetBonusScriptQueue
				.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts, discoveryMode));
	}

	private static void addBonusToSetCore(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		if (isLogicalClient()) {
			return;
		}
		discoveryMode = clampDiscovery(discoveryMode);
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

	// ADD POTION EFFECT ELEMENT TO BONUS

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRK, int level) {
		SetBonusScriptQueue.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRK, level, Integer.MAX_VALUE, 0));
	}

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRK, int level, int duration, int interval) {
		SetBonusScriptQueue.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRK, level, duration, interval));
	}

	private static void addPotionEffectToBonusCore(String bonusID, String effectRK, int level, int duration,
			int interval) {

		if (isLogicalClient()) {
			return;
		}

		ServerBonus serverBonus = findServerBonus(bonusID);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusID + "' not found. Create/link it first.");
			return;
		}

		int amp = Math.max(0, level);
		int durTicks = Math.max(0, duration);
		int intTicks = Math.max(0, interval);
		String effectToken = effectRK + "." + amp + "." + durTicks + "." + intTicks;

		String parseableBonusElement = bonusID + ", " + effectToken;
		BonusElementPotionEffect bonusElement = BonusElementPotionEffect.getInstance(parseableBonusElement,
				SetBonusData.SERVER_DATA);
		if (bonusElement == null) {
			CraftTweakerAPI.logError(
					"CTSetBonus: failed to parse potion effect '" + effectToken + "' for bonus '" + bonusID + "'");
			return;
		}

		if (!attachElement(serverBonus, bonusElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach potion element to bonus '" + bonusID + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added potion " + effectRK + " (lvl=" + amp + ", dur=" + durTicks
				+ ", interval=" + intTicks + ") to bonus '" + bonusID + "'");
	}

	// ADD ATTRIBUTE MOD ELEMENT TO BONUS

	@ZenMethod
	public static void addAttributeModToBonus(String bonusID, String attribute, double amount, String operation) {
		int operationParsed = parseOperation(operation);
		SetBonusScriptQueue.enqueue(() -> addAttributeModToBonusCore(bonusID, attribute, amount, operationParsed));
	}

	private static void addAttributeModToBonusCore(String bonusID, String attribute, double amount,
			int operationParsed) {

		if (isLogicalClient()) {
			return;
		}

		ServerBonus serverBonus = findServerBonus(bonusID);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusID + "' not found. Create/link it first.");
			return;
		}

		int op = (operationParsed < 0 || operationParsed > 2) ? 0 : operationParsed;
		String spec = attribute + " = " + amount + (op != 0 ? (" @ " + op) : "");
		String line = bonusID + ", " + spec;

		BonusElementAttributeModifier elem = BonusElementAttributeModifier.getInstance(line, SetBonusData.SERVER_DATA);
		if (elem == null) {
			CraftTweakerAPI.logError("CTSetBonus: failed to build attribute element from '" + line + "'");
			return;
		}

		if (!attachElement(serverBonus, elem)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach attribute element to bonus '" + bonusID + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added attribute " + attribute + " = " + amount + " @ " + op + " to bonus '"
				+ bonusID + "'");
	}

	// ADD ENCHANTMENT ELEMENT TO BONUS

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotString, int level) {
		SetBonusScriptQueue.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotString, level, 0));
	}

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotString, int level, int mode) {
		SetBonusScriptQueue.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotString, level, mode));
	}

	private static void addEnchantmentToBonusCore(String bonusID, String slotString, int level, int mode) {
		// TODO
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

	private static Set findSetById(String setId) {
		for (Set s : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(s.id))
				return s;
		}
		return null;
	}

	private static boolean createSetWithSlot(String setId, String setName, String slotToken) {
		String line = setId + ", " + setName + ", " + slotToken;
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

	@SuppressWarnings("unchecked")
	private static boolean attachElement(ServerBonus serverBonus, ABonusElement elem) {
		try {
			Field f = Bonus.class.getDeclaredField("elements");
			f.setAccessible(true);
			((List<ABonusElement>) f.get(serverBonus)).add(elem);
			return true;
		} catch (NoSuchFieldException nf) {
			try {
				Field f2 = Bonus.class.getDeclaredField("bonusElements");
				f2.setAccessible(true);
				((List<ABonusElement>) f2.get(serverBonus)).add(elem);
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

	private static int parseOperation(String op) {
		if (op == null)
			return 0;
		switch (op.trim().toLowerCase(java.util.Locale.ROOT)) {
		case "add":
			return 0; // + amount
		case "mult_base":
			return 1; // + base * amount
		case "mult_total":
			return 2; // * (1 + amount)
		default:
			try {
				int n = Integer.parseInt(op);
				if (n >= 0 && n <= 2)
					return n;
			} catch (NumberFormatException ignored) {
			}
			CraftTweakerAPI.logError("CTSetBonus: unknown attribute operation '" + op
					+ "'. Use add | mult_base | mult_total (defaulting to add).");
			return 0;
		}
	}

	private static ServerBonus findServerBonus(String bonusID) {
		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (bonusID.equals(bonus.id) && bonus instanceof ServerBonus)
				return (ServerBonus) bonus;
		}
		return null;
	}

	private static int clampDiscovery(int m) {
		return m < 0 ? 0 : m > 2 ? 2 : m;
	}

	// DEBUG

	@ZenMethod
	public static void debugDATA() {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		CraftTweakerAPI.logInfo("CTSetBonus DEBUG (" + side + ") sets=" + SetBonusData.SERVER_DATA.sets.size()
				+ ", equips=" + SetBonusData.SERVER_DATA.equipment.size() + ", bonuses="
				+ SetBonusData.SERVER_DATA.bonuses.size());
	}

}