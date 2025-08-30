package com.mahghuuuls.ctsetbonus.cooldowntracker;

import java.util.Objects;

public class CooldownKey {
	private String playerId;
	private String cooldownId;

	CooldownKey(String playerId, String cooldownId) {
		this.playerId = playerId;
		this.cooldownId = cooldownId;
	}

	@Override
	public boolean equals(Object object) {

		if (this == object) {
			return true;
		}

		if (!(object instanceof CooldownKey)) {
			return false;
		}

		CooldownKey that = (CooldownKey) object;
		return playerId.equals(that.playerId) && cooldownId.equals(that.cooldownId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerId, cooldownId);
	}

}
