package com.lfghelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.PluginPanel;
import okhttp3.*;
import java.awt.*;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Slf4j
public class lfghelperpanel extends PluginPanel
{
	private final lfghelperconfig config;
	private final OkHttpClient httpClient;
	private final Gson gson;

	private JTextField bossField;
	private JTextField clanchatField;
	private JTextField teamSizeField;
	private JTextField splitsOrFFAField;
	private JTextField worldField;
	private JTextField countdownField;
	private JComboBox<String> skillLevelDropdown;

	@Inject
	private Client client;

	private long lastSubmitTime = 0;
	private static final long RATE_LIMIT_TIME = 5 * 60 * 1000;

	@Inject
	public lfghelperpanel(lfghelperconfig config, Client client, OkHttpClient httpClient, Gson gson)
	{
		this.config = config;
		this.client = client;
		this.httpClient = httpClient;
		this.gson = gson;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setPreferredSize(new Dimension(300, 450));

		Font labelFont = new Font("Arial", Font.BOLD, 13);
		Font textFieldFont = new Font("Arial", Font.PLAIN, 14);
		Dimension fieldSize = new Dimension(260, 25);

		bossField = createLabeledField("What boss is being killed?", labelFont, textFieldFont, fieldSize);
		clanchatField = createLabeledField("What clanchat is to be used?", labelFont, textFieldFont, fieldSize);
		teamSizeField = createLabeledField("<html>What is the team size?</html>", labelFont, textFieldFont, fieldSize);
		splitsOrFFAField = createLabeledField("Splits or free for all?", labelFont, textFieldFont, fieldSize);
		worldField = createLabeledField("What world?", labelFont, textFieldFont, fieldSize);
		countdownField = createLabeledField("How long until start (minutes)?", labelFont, textFieldFont, fieldSize);

		JLabel skillLabel = new JLabel("Skill Level?");
		skillLabel.setFont(labelFont);
		skillLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(skillLabel);

		skillLevelDropdown = new JComboBox<>(new String[]{"New", "Experienced", "Professional", "Anyone Welcome"});
		skillLevelDropdown.setFont(textFieldFont);
		skillLevelDropdown.setMaximumSize(fieldSize);
		skillLevelDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(skillLevelDropdown);

		add(Box.createVerticalStrut(15));

		JButton submitButton = new JButton("Submit");
		submitButton.setFont(new Font("Arial", Font.BOLD, 16));
		submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		submitButton.setMaximumSize(new Dimension(100, 30));
		add(submitButton);
		submitButton.setEnabled(false);

		submitButton.addActionListener(e -> handleSubmit());

		addDocumentListenerToField(bossField, submitButton);
		addDocumentListenerToField(clanchatField, submitButton);
		addDocumentListenerToField(teamSizeField, submitButton);
		addDocumentListenerToField(splitsOrFFAField, submitButton);
		addDocumentListenerToField(worldField, submitButton);
		addDocumentListenerToField(countdownField, submitButton);
	}

	private JTextField createLabeledField(String labelText, Font labelFont, Font textFont, Dimension fieldSize)
	{
		JLabel label = new JLabel(labelText);
		label.setFont(labelFont);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(label);

		JTextField field = new JTextField();
		field.setFont(textFont);
		field.setMaximumSize(fieldSize);
		field.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(field);
		add(Box.createVerticalStrut(10));

		return field;
	}

	private void addDocumentListenerToField(JTextField field, JButton submitButton)
	{
		field.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e) { toggleSubmitButton(submitButton); }
			@Override
			public void removeUpdate(DocumentEvent e) { toggleSubmitButton(submitButton); }
			@Override
			public void changedUpdate(DocumentEvent e) { toggleSubmitButton(submitButton); }
		});
	}

	private void toggleSubmitButton(JButton submitButton)
	{
		boolean isAnyFieldEmpty = bossField.getText().isEmpty() || clanchatField.getText().isEmpty() ||
			teamSizeField.getText().isEmpty() || splitsOrFFAField.getText().isEmpty() ||
			worldField.getText().isEmpty() || countdownField.getText().isEmpty();
		submitButton.setEnabled(!isAnyFieldEmpty);
	}

	private void handleSubmit()
	{
		if (client == null || client.getLocalPlayer() == null)
		{
			JOptionPane.showMessageDialog(this, "You need to be logged in to submit.");
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastSubmitTime < RATE_LIMIT_TIME)
		{
			JOptionPane.showMessageDialog(this, "You can submit again after 5 minutes.");
			return;
		}

		lastSubmitTime = currentTime;

		String characterName = client.getLocalPlayer().getName();
		if (characterName == null || characterName.isEmpty())
		{
			log.error("Failed to retrieve player name.");
			return;
		}

		String boss = bossField.getText();
		String clanchat = clanchatField.getText();
		String teamSize = teamSizeField.getText();
		String splitsOrFFA = splitsOrFFAField.getText();
		String world = worldField.getText();
		String skillLevel = (String) skillLevelDropdown.getSelectedItem();

		if ("99".equals(teamSize))
		{
			teamSize = "unlimited";
		}

		int countdownMinutes = 5;
		try
		{
			countdownMinutes = Integer.parseInt(countdownField.getText());
			if (countdownMinutes < 1 || countdownMinutes > 120)
			{
				JOptionPane.showMessageDialog(this, "Please enter a number between 1 and 120 for the countdown.");
				return;
			}
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Invalid countdown. Please enter a number between 1 and 120.");
			return;
		}

		long countdownTimestamp = (System.currentTimeMillis() / 1000L) + (countdownMinutes * 60);
		String discordRelativeTime = "<t:" + countdownTimestamp + ":t>";

		String roleId = config.roleId();

		JsonObject embedField1 = new JsonObject();
		embedField1.addProperty("name", "Boss");
		embedField1.addProperty("value", boss);
		embedField1.addProperty("inline", false);

		JsonObject embedField2 = new JsonObject();
		embedField2.addProperty("name", "Clanchat");
		embedField2.addProperty("value", clanchat);
		embedField2.addProperty("inline", false);

		JsonObject embedField3 = new JsonObject();
		embedField3.addProperty("name", "Team Size");
		embedField3.addProperty("value", teamSize);
		embedField3.addProperty("inline", false);

		JsonObject embedField4 = new JsonObject();
		embedField4.addProperty("name", "Splits or Free for All");
		embedField4.addProperty("value", splitsOrFFA);
		embedField4.addProperty("inline", false);

		JsonObject embedField5 = new JsonObject();
		embedField5.addProperty("name", "World");
		embedField5.addProperty("value", world);
		embedField5.addProperty("inline", false);

		JsonObject embedField6 = new JsonObject();
		embedField6.addProperty("name", "Skill Level");
		embedField6.addProperty("value", skillLevel);
		embedField6.addProperty("inline", false);

		JsonObject embedField7 = new JsonObject();
		embedField7.addProperty("name", "Starts");
		embedField7.addProperty("value", discordRelativeTime);
		embedField7.addProperty("inline", false);

		JsonObject embed = new JsonObject();
		embed.addProperty("title", "LFG Request");
		embed.addProperty("color", 16776960);
		embed.add("fields", gson.toJsonTree(new JsonObject[]{embedField1, embedField2, embedField3, embedField4, embedField5,  embedField7, embedField6}));

		JsonObject jsonPayload = new JsonObject();
		jsonPayload.addProperty("content", "<@&" + roleId + ">, " + characterName + " is starting a group for " + boss + " @ " + discordRelativeTime + "!");
		jsonPayload.add("embeds", gson.toJsonTree(new JsonObject[]{embed}));

		sendToDiscord(gson.toJson(jsonPayload));
	}

	private void sendToDiscord(String jsonPayload)
	{
		String webhookUrl = config.webhookUrl();
		if (webhookUrl.isEmpty())
		{
			log.error("Webhook URL is not set.");
			return;
		}
		sendWebhookRequest(webhookUrl, jsonPayload);
	}

	private void sendWebhookRequest(String webhookUrl, String payload)
	{
		RequestBody body = RequestBody.create(MediaType.get("application/json"), payload);
		Request request = new Request.Builder().url(webhookUrl).post(body).build();
		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.error("Failed to send webhook request", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				if (!response.isSuccessful())
				{
					log.error("Unexpected response: " + response.code() + " - " + response.message());
				}
				else
				{
					log.debug("Webhook sent successfully!");
				}
				response.close();
			}
		});
	}
}
