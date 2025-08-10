package com.mahghuuuls.ctsetbonus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonuselements.ABonusElement;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementAttributeModifier;
import com.fantasticsource.setbonus.common.bonuselements.BonusElementEnchantment;
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

	private static final Map<String, SlotAccum> SLOT_ACCUMULATORS = new HashMap<>(); // Holds per-(set,slot)
																						// accumulators used to merge
																						// multiple item options into a
																						// single slot entry.

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String equipRL) {
		ScriptLoader.enqueue(() -> addEquipToSetCore(setName, slotString, equipRL));

	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String equipRL) {
		ScriptLoader.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRL));
	}

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			ScriptLoader.enqueue(() -> addEquipToSetCore(setName, slotString, equipRL));
		}
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String[] equipsRL) {
		for (String equipRL : equipsRL) {
			ScriptLoader.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRL));
		}
	}

	private static void addEquipToSetCore(String setName, String slotPart, String equipRL) {
		if (isClient())
			return;

		String equipId = requireEquipIdOrLog(equipRL, setName);
		if (equipId == null)
			return;

		SlotAccum slotAccumulator = getOrCreateAccum(setName, slotPart);
		boolean changed = slotAccumulator.equipIds.add(equipId);

		if (!changed && slotAccumulator.setRef != null && slotAccumulator.slotRef != null)
			return;

		String slotToken = buildSlotToken(slotAccumulator.slotKey, slotAccumulator.equipIds);
		if (!applySlotOptions(slotAccumulator, slotToken))
			return;

		CraftTweakerAPI.logInfo("CTSetBonus: Added " + slotAccumulator.equipIds.size() + " option(s) to "
				+ slotAccumulator.setName + " at slot " + slotAccumulator.slotKey);
	}

	// ADD BONUS TO SET

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName) {
		ScriptLoader.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, -1, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts) {
		ScriptLoader.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts, 1));
	}

	@ZenMethod
	public static void addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		ScriptLoader.enqueue(() -> addBonusToSetCore(bonusID, bonusDescription, setName, numberOfParts, discoveryMode));
	}

	private static void addBonusToSetCore(String bonusId, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		if (isClient()) {
			return;
		}

		if (discoveryMode < 0 || discoveryMode > 2) {
			CraftTweakerAPI
					.logWarning("CTSetBonus: discovery mode " + discoveryMode + " is outside 0..2; attempting as-is.");
		}
		String setId = setName.replace(" ", "");

		Set setRef = null;
		for (Set set : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(set.id)) {
				setRef = set;
				break;
			}
		}
		if (setRef == null) {
			CraftTweakerAPI.logError("CTSetBonus: set '" + setName + "' not found. "
					+ "Add at least one equip to the set before adding bonuses.");
			return;
		}

		String requirementSpec = (numberOfParts > 0) ? (setId + "." + numberOfParts) : setId;

		ServerBonus serverBonus = null;
		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (bonusId.equals(bonus.id)) {
				if (bonus instanceof ServerBonus)
					serverBonus = (ServerBonus) bonus;
				break;
			}
		}

		if (serverBonus == null) {
			String safeName = sanitizeBonusName(bonusDescription); // commas break parsing
			String bonusSpec = bonusId + ", " + safeName + ", " + discoveryMode + ", " + requirementSpec;
			Bonus createdBonus = Bonus.getInstance(bonusSpec, SetBonusData.SERVER_DATA);
			if (!(createdBonus instanceof ServerBonus)) {
				CraftTweakerAPI
						.logError("CTSetBonus: failed to create bonus '" + bonusId + "' from '" + bonusSpec + "'");
				return;
			}
			SetBonusData.SERVER_DATA.bonuses.add(createdBonus);
			CraftTweakerAPI.logInfo("CTSetBonus: New bonus added " + bonusId + " (\"" + safeName + "\", mode="
					+ discoveryMode + ", req=" + requirementSpec + ")");
			return;
		}

		ABonusRequirement bonusReq = ABonusRequirement.parse(requirementSpec, SetBonusData.SERVER_DATA.sets);
		if (bonusReq == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad requirement '" + requirementSpec + "'");
			return;
		}
		int desiredPieces = (numberOfParts > 0) ? numberOfParts : setRef.getMaxNumber();
		if (hasSameSetRequirement(serverBonus, setId, desiredPieces)) {
			CraftTweakerAPI
					.logInfo("CTSetBonus: bonus '" + bonusId + "' already has requirement '" + requirementSpec + "'");
			return;
		}
		serverBonus.requirements.add(bonusReq);
		CraftTweakerAPI.logInfo("CTSetBonus: Linked bonus '" + bonusId + "' -> set '" + setName + "' (pieces="
				+ (numberOfParts > 0 ? numberOfParts : "FULL") + ", mode=" + discoveryMode + ")");
	}

	// ADD POTION EFFECT ELEMENT TO BONUS

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRL, int level) {
		ScriptLoader.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRL, level, Integer.MAX_VALUE, 0));
	}

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRL, int level, int duration, int interval) {
		ScriptLoader.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRL, level, duration, interval));
	}

	private static void addPotionEffectToBonusCore(String bonusId, String effectRL, int level, int duration,
			int interval) {

		if (isClient()) {
			return;
		}

		ServerBonus serverBonus = findServerBonus(bonusId);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusId + "' not found. Create/link it first.");
			return;
		}

		int amp = Math.max(0, level);
		int durationTicks = Math.max(0, duration);
		int intervalTicks = Math.max(0, interval);
		String effectToken = effectRL + "." + amp + "." + durationTicks + "." + intervalTicks;

		String potionSpec = bonusId + ", " + effectToken;
		BonusElementPotionEffect potionElement = BonusElementPotionEffect.getInstance(potionSpec,
				SetBonusData.SERVER_DATA);
		if (potionElement == null) {
			CraftTweakerAPI.logError(
					"CTSetBonus: failed to parse potion effect '" + effectToken + "' for bonus '" + bonusId + "'");
			return;
		}

		if (!attachElement(serverBonus, potionElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach potion element to bonus '" + bonusId + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added potion " + effectRL + " (lvl=" + amp + ", dur=" + durationTicks
				+ ", interval=" + intervalTicks + ") to bonus '" + bonusId + "'");
	}

	// ADD ATTRIBUTE MOD ELEMENT TO BONUS

	@ZenMethod
	public static void addAttributeModToBonus(String bonusID, String attribute, double amount, String operation) {
		int operationCode = parseOperation(operation);
		ScriptLoader.enqueue(() -> addAttributeModToBonusCore(bonusID, attribute, amount, operationCode));
	}

	private static void addAttributeModToBonusCore(String bonusId, String attribute, double amount, int operationCode) {

		if (isClient()) {
			return;
		}

		ServerBonus serverBonus = findServerBonus(bonusId);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusId + "' not found. Create/link it first.");
			return;
		}

		int operation = (operationCode < 0 || operationCode > 2) ? 0 : operationCode;
		String spec = attribute + " = " + amount + (operation != 0 ? (" @ " + operation) : "");
		String attModElementSpec = bonusId + ", " + spec;

		BonusElementAttributeModifier attModElement = BonusElementAttributeModifier.getInstance(attModElementSpec,
				SetBonusData.SERVER_DATA);
		if (attModElement == null) {
			CraftTweakerAPI.logError("CTSetBonus: failed to build attribute element from '" + attModElementSpec + "'");
			return;
		}

		if (!attachElement(serverBonus, attModElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach attribute element to bonus '" + bonusId + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added attribute " + attribute + " = " + amount + " @ " + operation
				+ " to bonus '" + bonusId + "'");
	}

	// ADD ENCHANTMENT ELEMENT TO BONUS

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotString, String enchantRL, int level) {
		ScriptLoader.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotString, "", enchantRL, level, 0));
	}

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotSpec, String itemRL, String enchantRL,
			int level, int mode) {
		ScriptLoader.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotSpec, itemRL, enchantRL, level, mode));
	}

	private static void addEnchantmentToBonusCore(String bonusId, String slotSpec, String itemRL, String enchantRL,
			int level, int mode) {
		if (isClient())
			return;

		ServerBonus serverBonus = findServerBonus(bonusId);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusId + "' not found. Create/link it first.");
			return;
		}

		String raw = slotSpec == null ? "" : slotSpec.trim();

		String slotDataSpec;
		if (itemRL != null && !itemRL.isEmpty()) {
			Equip ensured = getOrAddEquipment(itemRL);
			if (ensured == null) {
				CraftTweakerAPI.logError("CTSetBonus: unknown item '" + itemRL + "' for enchant target.");
				return;
			}
			String equipId = getEquipIdFromRL(itemRL);
			String lhs = raw.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
			slotDataSpec = lhs + "=" + equipId;
		} else {
			int eqIdx = raw.indexOf('=');
			if (eqIdx >= 0) {
				CraftTweakerAPI.logWarning("CTSetBonus: '=' found in slot but no itemRL provided; "
						+ "ignoring RHS and targeting the slot only.");
				raw = raw.substring(0, eqIdx).trim();
			}
			slotDataSpec = raw.toLowerCase(Locale.ROOT);
		}

		SlotData slotData = SlotData.getInstance(slotDataSpec, SetBonusData.SERVER_DATA);
		if (slotData == null) {
			CraftTweakerAPI.logError("CTSetBonus: invalid slot selector '" + slotDataSpec + "'. "
					+ "Use 'head', 'chest', 'legs', 'feet', 'mainhand', 'offhand', a number, or 'slot=modid:item'.");
			return;
		}

		String enchantToken = enchantRL + "." + level + "." + mode;
		String enchantSpec = bonusId + ", " + slotDataSpec + ", " + enchantToken;

		BonusElementEnchantment enchantElement = BonusElementEnchantment.getInstance(enchantSpec,
				SetBonusData.SERVER_DATA);
		if (enchantElement == null) {
			CraftTweakerAPI
					.logWarning("CTSetBonus: enchant parse failed for '" + enchantSpec + "'. Retrying with mode=0.");
			String retrySpec = bonusId + ", " + slotDataSpec + ", " + enchantRL + "." + level + ".0";
			enchantElement = BonusElementEnchantment.getInstance(retrySpec, SetBonusData.SERVER_DATA);
			if (enchantElement == null) {
				CraftTweakerAPI.logError("CTSetBonus: enchant parse still failed with fallback. Line: " + retrySpec);
				return;
			}
		}

		if (!attachElement(serverBonus, enchantElement)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach enchantment element to bonus '" + bonusId + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added enchant " + enchantRL + " (lvl=" + level + ", mode=" + mode
				+ ") to bonus '" + bonusId + "' targeting '" + slotDataSpec + "'");
	}

	// DEBUG

	@ZenMethod
	public static void debugData() {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		CraftTweakerAPI.logInfo("CTSetBonus DEBUG (" + side + ") sets=" + SetBonusData.SERVER_DATA.sets.size()
				+ ", equips=" + SetBonusData.SERVER_DATA.equipment.size() + ", bonuses="
				+ SetBonusData.SERVER_DATA.bonuses.size());
	}

	// HELPERS

	/**
	 * Ensures an Equip entry exists in SERVER_DATA for a given item registry key
	 * (modid:item). Returns the existing one or creates/parses and registers a new
	 * one.
	 */
	private static Equip getOrAddEquipment(String equipRL) {
		String equipId = getEquipIdFromRL(equipRL);
		Equip targetEquip = null;
		for (Equip equip : SetBonusData.SERVER_DATA.equipment) {
			if (equipId.equals(equip.id)) {
				return equip;
			}
		}
		if (targetEquip == null) {
			String equipSpec = equipId + ", " + equipRL;
			Equip createdEquip = Equip.getInstance(equipSpec);
			if (createdEquip != null) {
				SetBonusData.SERVER_DATA.equipment.add(createdEquip);
				CraftTweakerAPI.logInfo("CTSetBonus: New equip added : " + equipSpec);
			}
			targetEquip = createdEquip;
		}
		return targetEquip;
	}

	/**
	 * Converts a registry key like modid:item (and optional @meta) into Set Bonus’s
	 * internal equip id format by replacing :/@ with _. Used to build slot =
	 * equipId or composite RHS strings.
	 */
	private static String getEquipIdFromRL(String equipRL) {
		return equipRL.replace(":", "_").replace("@", "_");
	}

	/**
	 * Replaces commas in user-facing bonus names, avoiding CSV-style parser issues
	 * when constructing config-like lines.
	 */
	private static String sanitizeBonusName(String name) {
		return name == null ? "" : name.replace(",", " - ");
	}

	/**
	 * Checks if a ServerBonus already has a SetRequirement for the same set and
	 * piece count (full or partial). Prevents duplicate requirements.
	 */
	private static boolean hasSameSetRequirement(ServerBonus bonus, String setId, int desiredPieces) {
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

	/**
	 * Adds a parsed bonus element (potion/attribute/enchant) to a ServerBonus using
	 * reflection. Handles both possible field names (elements or bonusElements).
	 * Returns success/failure.
	 */
	@SuppressWarnings("unchecked")
	private static boolean attachElement(ServerBonus serverBonus, ABonusElement bonusElem) {
		try {
			Field field = Bonus.class.getDeclaredField("elements");
			field.setAccessible(true);
			((List<ABonusElement>) field.get(serverBonus)).add(bonusElem);
			return true;
		} catch (NoSuchFieldException nf) {
			try {
				Field field2 = Bonus.class.getDeclaredField("bonusElements");
				field2.setAccessible(true);
				((List<ABonusElement>) field2.get(serverBonus)).add(bonusElem);
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

	/**
	 * Parses attribute modifier operation from string or number. Accepts add |
	 * mult_base | mult_total (or 0|1|2). Defaults to add with an error log on
	 * unknown inputs.
	 */
	private static int parseOperation(String operation) {
		if (operation == null)
			return 0;
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

	/**
	 * Finds a ServerBonus by id in SERVER_DATA.bonuses (only returns if the bonus
	 * is actually a ServerBonus).
	 */
	private static ServerBonus findServerBonus(String bonusID) {
		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (bonusID.equals(bonus.id) && bonus instanceof ServerBonus)
				return (ServerBonus) bonus;
		}
		return null;
	}

	/**
	 * Normalizes slot identifiers to canonical forms: trims, lowercases, strips
	 * spaces; maps armor indices 39/38/37/36 to head/chest/legs/feet; leaves other
	 * numbers or names (mainhand, offhand) as-is
	 */
	private static String normalizeSlotKey(String slotPart) {
		if (slotPart == null)
			return "";
		String slotPartTrim = slotPart.trim().toLowerCase(Locale.ROOT).replace(" ", "");

		try {
			int slotPartInt = Integer.parseInt(slotPartTrim);
			switch (slotPartInt) {
			case 36:
				return "feet";
			case 37:
				return "legs";
			case 38:
				return "chest";
			case 39:
				return "head";
			default:
				return slotPartTrim;
			}
		} catch (NumberFormatException ignore) {
		}

		return slotPartTrim;
	}

	/**
	 * Clears the internal per-(set,slot) accumulator map used to OR-merge multiple
	 * items into a single SlotData
	 */
	static void clearSlotAccumulators() {
		SLOT_ACCUMULATORS.clear();
	}

	/**
	 * Validates the item registry key, makes sure an Equip entry exists for it
	 * (creating one if needed), and returns the internal equipId. If anything’s
	 * off, it logs an error.
	 */
	private static String requireEquipIdOrLog(String equipRL, String setName) {
		if (equipRL == null || equipRL.trim().isEmpty()) {
			CraftTweakerAPI.logError("CTSetBonus: empty item registry key for set '" + setName + "'.");
			return null;
		}
		String equipRLTrim = equipRL.trim();
		Equip equipment = getOrAddEquipment(equipRLTrim);
		if (equipment == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad equip registry key '" + equipRLTrim + "'");
			return null;
		}
		return getEquipIdFromRL(equipRLTrim);
	}

	/**
	 * Looks up (or creates) the “SlotAccum” object that tracks everything added to
	 * a specific (set, slot) pair. It also grabs the current Set reference if the
	 * set is already in SERVER_DATA.
	 */
	private static SlotAccum getOrCreateAccum(String setName, String slotPart) {
		String setId = setName.replace(" ", "");
		String slotKey = normalizeSlotKey(slotPart);

		String key = setId + "|" + slotKey;
		SlotAccum slotAccumulator = SLOT_ACCUMULATORS.get(key);
		if (slotAccumulator == null) {
			slotAccumulator = new SlotAccum(setId, setName, slotKey);
			slotAccumulator.setRef = findSetById(setId);
			SLOT_ACCUMULATORS.put(key, slotAccumulator);
		} else {
			slotAccumulator.setName = setName;
		}
		return slotAccumulator;
	}

	/**
	 * Builds the exact string the Set Bonus parser expects, like slotKey = id1 |
	 * id2 | id3, using the current list of allowed items for that slot.
	 */
	private static String buildSlotToken(String slotKey, LinkedHashSet<String> equipIds) {
		return slotKey + " = " + String.join(" | ", equipIds);
	}

	/**
	 * Applies the slot token to the data model. If the set doesn’t exist yet, it
	 * creates the set with this slot pre-populated; if it does exist, it replaces
	 * the previous SlotData for that slot with the new merged one.
	 */
	private static boolean applySlotOptions(SlotAccum slotAcc, String slotToken) {
		if (slotAcc.setRef == null) {

			String setSpec = slotAcc.setId + ", " + slotAcc.setName + ", " + slotToken;
			Set createdSet = Set.getInstance(setSpec, SetBonusData.SERVER_DATA);
			if (createdSet == null) {
				CraftTweakerAPI
						.logError("CTSetBonus: failed to create set '" + slotAcc.setId + "' from '" + setSpec + "'");
				return false;
			}
			SetBonusData.SERVER_DATA.sets.add(createdSet);
			slotAcc.setRef = createdSet;

			if (createdSet.slotData.isEmpty()) {
				CraftTweakerAPI
						.logError("CTSetBonus: internal error: created set has no slot data for '" + slotToken + "'");
				return false;
			}
			slotAcc.slotRef = createdSet.slotData.get(createdSet.slotData.size() - 1);

			CraftTweakerAPI.logInfo("CTSetBonus: New set added " + slotAcc.setName + " (" + slotAcc.setId + ")");
			return true;
		}

		if (slotAcc.slotRef != null) {
			slotAcc.setRef.slotData.remove(slotAcc.slotRef);
			slotAcc.slotRef = null;
		}

		SlotData slotData = SlotData.getInstance(slotToken, SetBonusData.SERVER_DATA);
		if (slotData == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad slot token '" + slotToken + "'");
			return false;
		}
		slotAcc.setRef.slotData.add(slotData);
		slotAcc.slotRef = slotData;
		return true;
	}

	/**
	 * Returns true when running on the logical client (Side.CLIENT). Used to avoid
	 * mutating server-only data structures on the client.
	 */
	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	/**
	 * Linear search for a Set in SERVER_DATA.sets by id.
	 */
	public static Set findSetById(String setId) {
		for (Set set : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(set.id))
				return set;
		}
		return null;
	}

}