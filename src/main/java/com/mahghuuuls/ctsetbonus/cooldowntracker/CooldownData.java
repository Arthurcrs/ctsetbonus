package com.mahghuuuls.ctsetbonus.cooldowntracker;

public class CooldownData {
	public long durationTicks;
	public long startTime;

	CooldownData(long durationTicks, long startTime) {
		this.durationTicks = durationTicks;
		this.startTime = startTime;
	}
}
