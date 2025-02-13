package com.lfghelper;

import com.google.inject.Provides;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "LFG Helper",
	description = "Helps organize LFG posts for Discord",
	tags = {"lfg", "discord", "group"},
	enabledByDefault = false // This should disable it by default
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

	@Override
	protected void startUp() throws Exception {
		// Initialize the panel with the required config
		panel = new lfghelperpanel(config, client);  // Pass config object here

		// Load the image as BufferedImage
		try {
			BufferedImage originalImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/com/lfghelper/icon.png")));

			if (originalImage == null) {
				// Handle image load failure here
				return;
			}

			// Resize the image to fit the navigation button
			Image resizedImage = originalImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
			BufferedImage bufferedImage = new BufferedImage(resizedImage.getWidth(null), resizedImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(resizedImage, 0, 0, null);
			g.dispose();

			// Create the navigation button using the BufferedImage
			navButton = NavigationButton.builder()
				.tooltip("LFG Helper") // Tooltip text for the button
				.icon(bufferedImage) // Pass the BufferedImage as the icon
				.panel(panel) // Assign the panel to the button
				.priority(5) // Set the priority for button position
				.build();

			// Add the navigation button to the client toolbar
			clientToolbar.addNavigation(navButton);

			// Optionally, if you have overlays, you can add them here (if needed)
			// overlayManager.add(new SomeOverlay()); // Add overlays here if applicable

		} catch (IOException e) {
			// Handle image loading failure here
		}

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
