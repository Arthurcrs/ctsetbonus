package com.mahghuuuls.ctsetbonus.slotaccumulator;

import java.util.HashMap;
import java.util.Map;

import com.mahghuuuls.ctsetbonus.util.IdFormatter;
import com.mahghuuuls.ctsetbonus.util.ServerDataUtil;

public class SlotAccumulators {

	public static final Map<String, SlotAccum> SLOT_ACCUMULATORS = new HashMap<>();

	public static void clear() {
		SLOT_ACCUMULATORS.clear();
	}

	public static SlotAccum getOrCreateAccum(String setName, String slot) {
		String key = generateKey(setName, slot);
		SlotAccum slotAccumulator = SlotAccumulators.SLOT_ACCUMULATORS.get(key);

		if (slotAccumulator == null) {
			slotAccumulator = new SlotAccum(setName, slot);
			slotAccumulator.set = ServerDataUtil.getSet(setName);
			SlotAccumulators.SLOT_ACCUMULATORS.put(key, slotAccumulator);
		} else {
			slotAccumulator.setName = setName;
		}
		return slotAccumulator;
	}

	private static String generateKey(String setName, String slot) {
		String setId = IdFormatter.getSetIdFromName(setName);
		return setId + "|" + slot;
	}

}
