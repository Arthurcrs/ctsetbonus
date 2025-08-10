package com.mahghuuuls.ctsetbonus;

import java.lang.reflect.Field;
import java.util.HashMap;
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

	private static final Map<String, SlotAccum> SLOT_ACCUM = new HashMap<>();

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String equipRK) {
		ScriptLoader.enqueue(() -> addEquipToSetCore(setName, slotString, equipRK));

	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String equipRK) {
		ScriptLoader.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRK));
	}

	@ZenMethod
	public static void addEquipToSet(String setName, String slotString, String[] equipsRK) {
		for (String equipRK : equipsRK) {
			ScriptLoader.enqueue(() -> addEquipToSetCore(setName, slotString, equipRK));
		}
	}

	@ZenMethod
	public static void addEquipToSet(String setName, int slotInt, String[] equipsRK) {
		for (String equipRK : equipsRK) {
			ScriptLoader.enqueue(() -> addEquipToSetCore(setName, Integer.toString(slotInt), equipRK));
		}
	}

	private static void addEquipToSetCore(String setName, String slotPart, String equipRK) {
		if (isLogicalClient())
			return;

		if (equipRK == null || equipRK.trim().isEmpty()) {
			CraftTweakerAPI.logError("CTSetBonus: empty item registry key for set '" + setName + "'.");
			return;
		}

		final String setId = setName.replace(" ", "");
		final String slotKey = normalizeSlotKey(slotPart);

		// ensure the Equip exists and get its internal id
		Equip eq = getOrAddEquipment(equipRK.trim());
		if (eq == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad equip registry key '" + equipRK + "'");
			return;
		}
		String equipId = getEquipIdFromRK(equipRK.trim());

		// fetch/create accumulator for this (set,slot)
		final String accKey = setId + "|" + slotKey;
		SlotAccum acc = SLOT_ACCUM.get(accKey);
		if (acc == null) {
			acc = new SlotAccum(setId, setName, slotKey);
			acc.setRef = findSetById(setId);
			SLOT_ACCUM.put(accKey, acc);
		} else {
			acc.setName = setName; // keep latest display name
		}

		// add this item to the OR set (deduped)
		boolean changed = acc.equipIds.add(equipId);
		if (!changed && acc.setRef != null && acc.slotRef != null) {
			// nothing new to apply
			return;
		}

		// build RHS: "id1 | id2 | ..."
		String rhs = String.join(" | ", acc.equipIds);
		String slotToken = slotKey + " = " + rhs;

		if (acc.setRef == null) {
			// create the set WITH this slot already present
			String setLine = setId + ", " + setName + ", " + slotToken;
			Set created = Set.getInstance(setLine, SetBonusData.SERVER_DATA);
			if (created == null) {
				CraftTweakerAPI.logError("CTSetBonus: failed to create set '" + setId + "' from '" + setLine + "'");
				return;
			}
			SetBonusData.SERVER_DATA.sets.add(created);
			acc.setRef = created;

			// IMPORTANT: do NOT add another SlotData here.
			// Reuse the one that Set.getInstance(...) just created.
			if (created.slotData.isEmpty()) {
				CraftTweakerAPI
						.logError("CTSetBonus: internal error: created set has no slot data for '" + slotToken + "'");
				return;
			}
			// The last entry corresponds to the slot we just parsed in setLine
			acc.slotRef = created.slotData.get(created.slotData.size() - 1);

			CraftTweakerAPI.logInfo("CTSetBonus: New set added " + setName + " (" + setId + ")");
			CraftTweakerAPI.logInfo(
					"CTSetBonus: Added " + acc.equipIds.size() + " option(s) to " + setName + " at slot " + slotKey);
			return;
		}

		// update existing set: remove our previous SlotData (if any), then add the
		// merged one
		if (acc.slotRef != null) {
			acc.setRef.slotData.remove(acc.slotRef);
			acc.slotRef = null;
		}

		SlotData sd = SlotData.getInstance(slotToken, SetBonusData.SERVER_DATA);
		if (sd == null) {
			CraftTweakerAPI.logError("CTSetBonus: bad slot token '" + slotToken + "'");
			return;
		}
		acc.setRef.slotData.add(sd);
		acc.slotRef = sd;

		CraftTweakerAPI.logInfo(
				"CTSetBonus: Added " + acc.equipIds.size() + " option(s) to " + acc.setName + " at slot " + slotKey);
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

	private static void addBonusToSetCore(String bonusID, String bonusDescription, String setName, int numberOfParts,
			int discoveryMode) {
		if (isLogicalClient()) {
			return;
		}

		if (discoveryMode < 0 || discoveryMode > 2) {
			CraftTweakerAPI
					.logWarning("CTSetBonus: discovery mode " + discoveryMode + " is outside 0..2; attempting as-is.");
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

	// ADD POTION EFFECT ELEMENT TO BONUS

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRK, int level) {
		ScriptLoader.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRK, level, Integer.MAX_VALUE, 0));
	}

	@ZenMethod
	public static void addPotionEffectToBonus(String bonusID, String effectRK, int level, int duration, int interval) {
		ScriptLoader.enqueue(() -> addPotionEffectToBonusCore(bonusID, effectRK, level, duration, interval));
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
		ScriptLoader.enqueue(() -> addAttributeModToBonusCore(bonusID, attribute, amount, operationParsed));
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
	public static void addEnchantmentToBonus(String bonusID, String slotString, String enchantRK, int level) {
		ScriptLoader.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotString, "", enchantRK, level, 0));
	}

	@ZenMethod
	public static void addEnchantmentToBonus(String bonusID, String slotSpec, String itemRK, String enchantRK,
			int level, int mode) {
		ScriptLoader.enqueue(() -> addEnchantmentToBonusCore(bonusID, slotSpec, itemRK, enchantRK, level, mode));
	}

	private static void addEnchantmentToBonusCore(String bonusID, String slotSpec, String itemRK, String enchantRK,
			int level, int mode) {
		if (isLogicalClient())
			return;

		ServerBonus serverBonus = findServerBonus(bonusID);
		if (serverBonus == null) {
			CraftTweakerAPI.logError("CTSetBonus: bonus '" + bonusID + "' not found. Create/link it first.");
			return;
		}

		String raw = slotSpec == null ? "" : slotSpec.trim();
		if (raw.contains("|")) {
			CraftTweakerAPI.logError(
					"CTSetBonus: multi-slot specs are not supported. " + "Call addEnchantmentToBonus once per slot.");
			return;
		}

		String selector;
		if (itemRK != null && !itemRK.isEmpty()) {
			Equip ensured = getOrAddEquipment(itemRK);
			if (ensured == null) {
				CraftTweakerAPI.logError("CTSetBonus: unknown item '" + itemRK + "' for enchant target.");
				return;
			}
			String equipId = getEquipIdFromRK(itemRK);
			String lhs = raw.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
			selector = lhs + "=" + equipId;
		} else {
			int eqIdx = raw.indexOf('=');
			if (eqIdx >= 0) {
				CraftTweakerAPI.logWarning("CTSetBonus: '=' found in slot but no itemRK provided; "
						+ "ignoring RHS and targeting the slot only.");
				raw = raw.substring(0, eqIdx).trim();
			}
			selector = raw.toLowerCase(Locale.ROOT);
		}

		SlotData sd = SlotData.getInstance(selector, SetBonusData.SERVER_DATA);
		if (sd == null) {
			CraftTweakerAPI.logError("CTSetBonus: invalid slot selector '" + selector + "'. "
					+ "Use 'head', 'chest', 'legs', 'feet', 'mainhand', 'offhand', a number, or 'slot=modid:item'.");
			return;
		}

		String token = enchantRK + "." + level + "." + mode;
		String line = bonusID + ", " + selector + ", " + token;

		BonusElementEnchantment elem = BonusElementEnchantment.getInstance(line, SetBonusData.SERVER_DATA);
		if (elem == null) {
			CraftTweakerAPI.logWarning("CTSetBonus: enchant parse failed for '" + line + "'. Retrying with mode=0.");
			String retry = bonusID + ", " + selector + ", " + enchantRK + "." + level + ".0";
			elem = BonusElementEnchantment.getInstance(retry, SetBonusData.SERVER_DATA);
			if (elem == null) {
				CraftTweakerAPI.logError("CTSetBonus: enchant parse still failed with fallback. Line: " + retry);
				return;
			}
		}

		if (!attachElement(serverBonus, elem)) {
			CraftTweakerAPI.logError("CTSetBonus: could not attach enchantment element to bonus '" + bonusID + "'");
			return;
		}

		CraftTweakerAPI.logInfo("CTSetBonus: Added enchant " + enchantRK + " (lvl=" + level + ", mode=" + mode
				+ ") to bonus '" + bonusID + "' targeting '" + selector + "'");
	}

	// DEBUG

	@ZenMethod
	public static void debugDATA() {
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

	/**
	 * Converts a registry key like modid:item (and optional @meta) into Set Bonus’s
	 * internal equip id format by replacing :/@ with _. Used to build slot =
	 * equipId or composite RHS strings.
	 */
	private static String getEquipIdFromRK(String equipRK) {
		return equipRK.replace(":", "_").replace("@", "_");
	}

	/**
	 * Linear search for a Set in SERVER_DATA.sets by id.
	 */
	private static Set findSetById(String setId) {
		for (Set s : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(s.id))
				return s;
		}
		return null;
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

	/**
	 * Returns true when running on the logical client (Side.CLIENT). Used to avoid
	 * mutating server-only data structures on the client.
	 */
	private static boolean isLogicalClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	/**
	 * Adds a parsed bonus element (potion/attribute/enchant) to a ServerBonus using
	 * reflection. Handles both possible field names (elements or bonusElements).
	 * Returns success/failure.
	 */
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

	/**
	 * Parses attribute modifier operation from string or number. Accepts add |
	 * mult_base | mult_total (or 0|1|2). Defaults to add with an error log on
	 * unknown inputs.
	 */
	private static int parseOperation(String op) {
		if (op == null)
			return 0;
		switch (op.trim().toLowerCase(Locale.ROOT)) {
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
		String s = slotPart.trim().toLowerCase(Locale.ROOT).replace(" ", "");

		try {
			int n = Integer.parseInt(s);
			switch (n) {
			case 36:
				return "feet";
			case 37:
				return "legs";
			case 38:
				return "chest";
			case 39:
				return "head";
			default:
				return s;
			}
		} catch (NumberFormatException ignore) {
		}

		return s;
	}

	/**
	 * Clears the internal per-(set,slot) accumulator map used to OR-merge multiple
	 * items into a single SlotData
	 */
	static void clearSlotAccum() {
		SLOT_ACCUM.clear();
	}

}