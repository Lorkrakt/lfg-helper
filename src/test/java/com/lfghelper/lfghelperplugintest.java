package com.lfghelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class lfghelperplugintest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(lfghelperplugin.class);
		RuneLite.main(args);
	}
}