package com.lfghelper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "LFG Helper",
	description = "Helps organize LFG posts for Discord",
	tags = {"lfg", "discord", "group"}
)
public class lfghelperplugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar; // Inject the client toolbar

	@Inject
	private lfghelperconfig config;

	private lfghelperpanel panel;
	private NavigationButton navButton; // Sidebar button

	@Inject
	private OverlayManager overlayManager; // Inject OverlayManager if you need overlays

	@Inject
	private okhttp3.OkHttpClient httpClient; // Inject OkHttpClient

	@Inject
	private Injector injector; // Inject the Injector to get instances

	@Override
	protected void startUp() throws Exception {
		// Use injector to get the instance of lfghelperpanel
		panel = injector.getInstance(lfghelperpanel.class);

		// Load the image using ImageUtil
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/com/lfghelper/icon.png");

		// Create the navigation button
		navButton = NavigationButton.builder()
			.tooltip("LFG Helper")
			.icon(icon) // Use the loaded image directly
			.panel(panel)
			.priority(5)
			.build();

		// Add the button to the client toolbar
		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() {
		// Remove the sidebar button on shutdown
		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	lfghelperconfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(lfghelperconfig.class);
	}
}
