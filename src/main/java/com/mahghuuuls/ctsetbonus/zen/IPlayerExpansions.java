package com.mahghuuuls.ctsetbonus.zen;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.common.bonusrequirements.setrequirement.Set;
import com.fantasticsource.setbonus.server.ServerBonus;
import com.mahghuuuls.ctsetbonus.util.IdFormatter;
import com.mahghuuuls.ctsetbonus.util.SetTweaksUtil;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenExpansion("crafttweaker.player.IPlayer")
@ZenRegister
public class IPlayerExpansions {

	@ZenMethod
	public static boolean hasSetBonus(IPlayer iPlayer, String bonusName) {
		if (SetTweaksUtil.instanceIsClient()) {
			return false;
		}
		EntityPlayer player = CraftTweakerMC.getPlayer(iPlayer);
		EntityPlayerMP playerMP = (EntityPlayerMP) player;
		if (playerMP == null)
			return false;

		String bonusId = IdFormatter.getBonusIdFromName(bonusName);
		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (!bonusId.equals(bonus.id))
				continue;
			ServerBonus serverBonus = (ServerBonus) bonus;
			return serverBonus.getBonusInstance(playerMP).active;
		}
		return false;
	}

	@ZenMethod
	public static int getSetPieceCount(IPlayer iPlayer, String setName) {
		if (SetTweaksUtil.instanceIsClient()) {
			return 0;
		}
		EntityPlayer player = CraftTweakerMC.getPlayer(iPlayer);
		EntityPlayerMP playerMP = (EntityPlayerMP) player;
		if (playerMP == null)
			return 0;

		String setId = IdFormatter.getSetIdFromName(setName);
		for (Set set : SetBonusData.SERVER_DATA.sets) {
			if (setId.equals(set.id)) {
				return set.getNumberEquipped(playerMP);
			}
		}
		return 0;
	}
}