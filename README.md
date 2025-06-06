# LFG Helper - RuneLite Plugin

LFG Helper is a RuneLite plugin that helps players find groups for PvM content in Old School RuneScape. It allows users to quickly post LFG (Looking For Group) requests to a designated Discord server via a webhook.


1.1.1 Update !!!
New:
Added a field for input that asks how long until your group starts. Then shares in the users local time when the event starts. 
Added a skill level question. This includes: new, experienced, professional, and anyone welcome.
Cleaned up plugin panel to make it not look so clunky. 

## Features
✅ Simple form-based UI for entering LFG details  
✅ Automatically includes the player's in-game name in the LFG request  
✅ Supports boss name, clan chat, team size, loot type (splits/FFA), and world selection  
✅ Sends formatted messages to a Discord webhook  
✅ Prevents spam by enforcing a cooldown between submissions  
✅ Disables submission if required fields are empty

## How It Works
1. Open the **LFG Helper** panel in RuneLite.
2. **Configure the webhook and role ID**
    - Your server owner will provide a **Discord webhook URL** and a **PVMer role ID**.
    - Enter these in the plugin configuration settings.
3. Fill out the form:
    - **Boss Name**: Name of the boss you're looking to fight.
    - **Clan Chat**: The in-game clan chat for organizing the group.
    - **Team Size**: Enter `99` if there is no limit.
    - **Loot Type**: Splits or Free-for-All.
    - **World**: The world number for the event.
4. Click **Submit** to send the request to the designated Discord channel.
5. Ensure you are logged in before submitting. If not, the plugin will notify you.

## Setting Up Discord Integration (For Server Owners)
To use this plugin, server owners need to set up the following:

### 1️⃣ Create a Webhook
A webhook is required for the plugin to send LFG messages to your Discord server.
- In Discord, go to **Server Settings → Integrations → Webhooks**
- Create a new webhook and copy the **Webhook URL**
- Provide this URL to users who will be using the plugin

### 2️⃣ Create a "PVMer" Role
To allow the plugin to tag players looking for groups, create a role that will specifically be used for people who want the LFG functions.
- Go to **Server Settings → Roles → Create Role**
- Assign this role to users who want LFG notifications
- Copy the **Role ID** and provide it to plugin users

### Note
In order to get the role ID you must turn on developer mode. As long as you're not changing the role name constantly - this can be done once and never again.
- On Desktop:
- Go to User Settings (click the gear icon next to your username).
- Scroll down to Advanced under App Settings.
- Toggle on Developer Mode.
- Go to Server Settings → Roles.
- Right-click the role and select Copy ID.

🔹 **No Security Risk**: Sharing the role ID does not pose any security concerns as long as no additional permissions are assigned to the role. The ID is simply used for tagging in Discord messages.

## Example Discord Message
@PVMer, [Player Name] is starting a group for [Boss Name]!<br>
Boss: [Boss Name]<br>
Clan Chat: [Clan Chat]<br>
Team Size: [Size]<br>
Loot Type: [Splits/FFA]<br>
World: [World]
