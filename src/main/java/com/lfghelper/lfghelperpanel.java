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

	@Inject
	private Client client;

	private long lastSubmitTime = 0;
	private static final long RATE_LIMIT_TIME = 5 * 60 * 1000; // 5 minutes

	@Inject
	public lfghelperpanel(lfghelperconfig config, Client client, OkHttpClient httpClient, Gson gson)
	{
		this.config = config;
		this.client = client;
		this.httpClient = httpClient;
		this.gson = gson; // Injected instead of creating new Gson

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(300, 400));

		Font labelFont = new Font("Arial", Font.PLAIN, 14);
		Font textFieldFont = new Font("Arial", Font.PLAIN, 16);

		add(new JLabel("What boss is being killed?"));
		bossField = new JTextField(10);
		bossField.setFont(textFieldFont);
		add(bossField);
		add(Box.createVerticalStrut(10));

		add(new JLabel("What clanchat is to be used?"));
		clanchatField = new JTextField(10);
		clanchatField.setFont(textFieldFont);
		add(clanchatField);
		add(Box.createVerticalStrut(10));

		add(new JLabel("<html>What is the team size?<br>Enter 99 if unlimited</html>"));
		teamSizeField = new JTextField(5);
		teamSizeField.setFont(textFieldFont);
		add(teamSizeField);
		add(Box.createVerticalStrut(10));

		add(new JLabel("Splits or free for all?"));
		splitsOrFFAField = new JTextField(10);
		splitsOrFFAField.setFont(textFieldFont);
		add(splitsOrFFAField);
		add(Box.createVerticalStrut(10));

		add(new JLabel("What world?"));
		worldField = new JTextField(5);
		worldField.setFont(textFieldFont);
		add(worldField);
		add(Box.createVerticalStrut(10));

		JButton submitButton = new JButton("Submit");
		submitButton.setFont(new Font("Arial", Font.BOLD, 16));
		add(submitButton);

		submitButton.addActionListener(e -> handleSubmit());

		submitButton.setEnabled(false);

		addDocumentListenerToField(bossField, submitButton);
		addDocumentListenerToField(clanchatField, submitButton);
		addDocumentListenerToField(teamSizeField, submitButton);
		addDocumentListenerToField(splitsOrFFAField, submitButton);
		addDocumentListenerToField(worldField, submitButton);
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
			worldField.getText().isEmpty();

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

		// Collect form input data
		String boss = bossField.getText();
		String clanchat = clanchatField.getText();
		String teamSize = teamSizeField.getText();
		String splitsOrFFA = splitsOrFFAField.getText();
		String world = worldField.getText();

		if ("99".equals(teamSize))
		{
			teamSize = "unlimited";
		}

		String roleId = config.roleId();

		// Build JSON using injected Gson
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

		JsonObject embed = new JsonObject();
		embed.addProperty("title", "LFG Request");
		embed.addProperty("color", 16776960);
		embed.add("fields", gson.toJsonTree(new JsonObject[]{embedField1, embedField2, embedField3, embedField4, embedField5}));

		JsonObject jsonPayload = new JsonObject();
		jsonPayload.addProperty("content", "<@&" + roleId + ">, " + characterName + " is starting a group for " + boss + "!");
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

		Request request = new Request.Builder()
			.url(webhookUrl)
			.post(body)
			.build();

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
