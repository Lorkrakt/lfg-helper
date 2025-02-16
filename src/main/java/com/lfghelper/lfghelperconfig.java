package com.lfghelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lfghelperconfig")
public interface lfghelperconfig extends Config
{
	// Webhook URL configuration
	@ConfigItem(position = 1, keyName = "webhookUrl", name = "Webhook URL", description = "The webhook URL to send LFG requests to.")
	default String webhookUrl()
	{
		return "";
	}

	// Role ID configuration
	@ConfigItem(position = 2, keyName = "roleId", name = "Role ID", description = "The ID of the role to mention in the LFG request.")
	default String roleId()
	{
		return ""; // Default empty, should be filled by the user
	}
}
