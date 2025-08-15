package com.mahghuuuls.ctsetbonus.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class SideUtil {

	public static boolean instanceIsClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public static boolean instanceIsServer() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

}
