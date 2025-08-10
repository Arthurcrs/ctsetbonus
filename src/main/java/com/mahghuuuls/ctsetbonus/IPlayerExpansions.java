package com.mahghuuuls.ctsetbonus;

import com.fantasticsource.setbonus.SetBonusData;
import com.fantasticsource.setbonus.common.Bonus;
import com.fantasticsource.setbonus.server.ServerBonus;

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
	public static boolean hasSetBonus(IPlayer iPlayer, String bonusId) {
		EntityPlayer player = CraftTweakerMC.getPlayer(iPlayer);
		if (player.world.isRemote)
			return false;
		EntityPlayerMP playerMP = (EntityPlayerMP) player;

		for (Bonus bonus : SetBonusData.SERVER_DATA.bonuses) {
			if (!bonusId.equals(bonus.id))
				continue;
			ServerBonus serverBonus = (ServerBonus) bonus;
			return serverBonus.getBonusInstance(playerMP).active;
		}
		return false;
	}
}