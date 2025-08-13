package com.mahghuuuls.ctsetbonus.slotaccumulator;

import java.util.LinkedHashSet;

import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SlotData;
import com.mahghuuuls.ctsetbonus.util.IdFormatter;

/**
 * Collect multiple item candidates for a single set slot and merge them into
 * one SlotData entry using OR syntax: eg. "mainhand = idA | idB | idC".
 * Preserves insertion order and de-duplicates items.v
 * 
 * Each set has its own slot key?
 * 
 */
public final class SlotAccum {
	public String setId;
	public String slotKey;
	public final LinkedHashSet<String> equipIds = new LinkedHashSet<>();
	public String setName;
	public Set set;
	public SlotData slotData;

	public SlotAccum(String setName, String slotKey) {
		this.setId = IdFormatter.getSetIdFromName(setName);
		this.setName = setName;
		this.slotKey = slotKey;
	}

	public boolean addEquip(String equipRL) {
		String equipId = IdFormatter.getEquipIdFromRL(equipRL);
		return this.equipIds.add(equipId);
	}

	public String getSetKey() {
		return IdFormatter.getSetIdFromName(this.setName);
	}

	/**
	 * Builds the exact string the Set Bonus parser expects, like slotKey = id1 |
	 * id2 | id3, using the current list of allowed items for that slot.
	 */
	public String buildSlotToken() {
		return this.slotKey + " = " + String.join(" | ", this.equipIds);
	}

}
