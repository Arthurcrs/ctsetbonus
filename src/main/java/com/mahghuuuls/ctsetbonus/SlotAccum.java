package com.mahghuuuls.ctsetbonus;

import java.util.LinkedHashSet;

import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.SlotData;

/**
 * Collect multiple item candidates for a single set slot and merge them into
 * one SlotData entry using OR syntax: eg. "mainhand = idA | idB | idC".
 * Preserves insertion order and de-duplicates items.v
 */
public final class SlotAccum {
	final String setId;
	final String slotKey;
	final LinkedHashSet<String> equipIds = new LinkedHashSet<>();
	String setName;
	Set setRef;
	SlotData slotRef;

	SlotAccum(String setId, String setName, String slotKey) {
		this.setId = setId;
		this.setName = setName;
		this.slotKey = slotKey;
	}
}
