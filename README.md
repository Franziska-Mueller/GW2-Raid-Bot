# Riz GW2 Event Bot

This bot is meant to be used to help organize teams for events (raids, fractals, dungeons, etc.) in discord servers for GuildWars 2.
It attempts to create a streamlined user experience so that creating and joining are easy.

## Discord.com and Gateway Intents

As you may know, the Discord API was updated over time and we used to make use of an older version.
The new changes requiring us to upgrade our API connector also require the the new Gateway Intents.
We have updated the bot using the new Gateway Intents but to run the bot, you will have to enable the `Server Members Intent` for the bot from your Developer Portal's bot page. It is located in the `Bot` tab in the `Privileged Gateway Intents` section.
If you don't enable this the bot will not work, it will just log off again as soon as the first action is called.

## Usage

Once the bot is running, you need to set the event leader role (using the !setEventLeaderRole \[role\] command) to a server user role.
Any user who should be able to create events needs this role.

To create an event, use the !createEvent command. This will prompt the bot to ask you some questions about your event, and once you are done, it will create the embedded message
announcing the event in the channel that you specified.
For people to join the event, they simply need to click a reaction on the embedded message and then answer the bot on what role they want to play.
After this do this, they can click another specialization reaction to set themselves as a flex role (other roles they can play if necessary).

Event leaders can also remove people from events via !removeFromEvent \[event id\] \[name\]

## Getting Started

If you already have a registered bot and the emoji deployed, you can skip ahead to [Starting The Bot](#starting-the-bot).

### Prerequisites

- Java 8 or higher
- Apache Maven
- A Discord Bot Token for a registered Discord App
- A Discord server with the emoji to use on it
- The bot invited to the server with the emoji

### Preparations

The emoji used for reactions and in messages the bot sends need to be put onto a server the bot has access to. If you have no idea where to get the emoji for the bot, create an empty server to put just the bot and yourself on. This server will help debugging, preparing and testing as well as keep your guild server free from the emoji you are about to litter the test server with.

Extract the `Reactions.zip` file and upload the emoji in there on your test server under `Server Settings` => `Emoji`. After that, you need to grab all of the IDs of the emoji you uploaded in order for the bot to use them.

- Use the emotes in chat and put a backslash (`\`) in front of them to see their emoji ID.
- Copy the ID
- Paste the ID into the configuration file to assign it to the corresponding emote registration.

For the corresponding variable name to assign the emoji to, see [Configuration](#configuration).

#### Create An App

To create a Discord app, follow these easy steps:

- Log into the [Discord Developer Portal](https://discord.com/developers/applications)
- Create an app by clicking the button `New Application`
- Choose a name for your application and click `Create`
- In the menu to the left, select `OAuth2`
- From the `Scopes` panel, select the following options
  - `bot`
  - `applications.commands`
- From the `Bot Permissions` panel, select the following options
  - From "General Permissions"
    - `View Channel`
  - From "Text Permissions"
    - `Send Messages`
    - `Manage Messages`
    - `Read Message History`
    - `Use External Emojis`
    - `Add Reactions`
    - `Use Slash Commands`
- Copy the link from the bottom of the `Scopes` panel by clicking the `Copy` button to the right of it
- Paste the link in a new tab and authorize the bot to join your desired server

After you have authorized the bot to join your server, you should see it in the member list.
It will still be offline.

#### Get A Token

To get a token to use with the bot, go to your application's `Bot` page (from the navigation panel on th left) in the [Discord Developer Portal](https://discord.com/developers/applications). You will see a field called `Username` which you can change to your liking and below it there is a region that reads `Token`. Just use the `Copy` button to copy the token. Then paste it into the configuration with the corresponding variable name. For the variable name to use, please refer to the [Configuration](#configuration) section.

While you have that page open, scroll down and enable the `Privileged Gateway Intents` that are `Server Members Intent` and `Message Content Intent`. These are required for the bot to log in.

### Starting The Bot

To download the source code, use the corresponding `Code` button on GitHub (or just clone it if you can).

Open a terminal in the directory you have put the source code into and run

```sh
mvn package
```

to build the jar file that you will be running.

Make sure you have the correct configuration set according to the [Configuration](#configuration) section.

Then run

```sh
java -jar <path-to-jar>
```

Assuming you are still in the project directory and didn't move the jar file yet, the path to the jar file is `target/GW2-Raid-Bot-1.0-SNAPSHOT.jar`.

### Configuration

There are two types of coniguration, one uses environment variables to supply the bot with IDs and secrets for operation, the other uses a yaml file to configure logging.

#### Environment Variables

Environment variable can be used in two ways:

- Using actual environment variables from the system
- Using a .env file

The variables the bot reads from the environment are explained in the following table:

| Variable           | Purpose                                                           |
| ------------------ | ----------------------------------------------------------------- |
| DISCORD_TOKEN      | Token the bot uses to authorize with the Discord API.             |
| RAIDAR_USERNAME    | Username of a raidar account to upload dps reports if so desired. |
| RAIDAR_PASSWORD    | Password of a raidar account to upload dps reports if so desired. |
| EMOTE_DRAGONHUNTER | Emoji ID for the Dragonhunter class.                              |
| EMOTE_FIREBRAND    | Emoji ID for the Firebrand class.                                 |
| EMOTE_HERALD       | Emoji ID for the Herald class.                                    |
| EMOTE_RENEGADE     | Emoji ID for the Renegade class.                                  |
| EMOTE_BERSERKER    | Emoji ID for the Berserker class.                                 |
| EMOTE_SPELLBREAKER | Emoji ID for the Spellbreaker class.                              |
| EMOTE_SCRAPPER     | Emoji ID for the Scrapper class.                                  |
| EMOTE_HOLOSMITH    | Emoji ID for the Holosmith class.                                 |
| EMOTE_DRUID        | Emoji ID for the Druid class.                                     |
| EMOTE_SOULBEAST    | Emoji ID for the Soulbeast class.                                 |
| EMOTE_DAREDEVIL    | Emoji ID for the Daredevil class.                                 |
| EMOTE_DEADEYE      | Emoji ID for the Deadeye class.                                   |
| EMOTE_WEAVER       | Emoji ID for the Weaver class.                                    |
| EMOTE_TEMPEST      | Emoji ID for the Tempest class.                                   |
| EMOTE_CHRONOMANCER | Emoji ID for the Chronomancer class.                              |
| EMOTE_MIRAGE       | Emoji ID for the Mirage class.                                    |
| EMOTE_REAPER       | Emoji ID for the Reaper class.                                    |
| EMOTE_SCOURGE      | Emoji ID for the Scourge class.                                   |
| EMOTE_GUARDIAN     | Emoji ID for the Guardian class.                                  |
| EMOTE_REVENANT     | Emoji ID for the Revenant class.                                  |
| EMOTE_WARRIOR      | Emoji ID for the Warrior class.                                   |
| EMOTE_ENGINEER     | Emoji ID for the Engineer class.                                  |
| EMOTE_RANGER       | Emoji ID for the Ranger class.                                    |
| EMOTE_THIEF        | Emoji ID for the Thief class.                                     |
| EMOTE_ELEMENTALIST | Emoji ID for the Elementalist class.                              |
| EMOTE_MESMER       | Emoji ID for the Mesmer class.                                    |
| EMOTE_NECROMANCER  | Emoji ID for the Necromancer class.                               |
| EMOTE_FLEX         | Emoji ID for the Flex role reaction.                              |
| EMOTE_SWAP         | Emoji ID for the Swap reaction.                                   |
| EMOTE_EDIT         | Emoji ID for the Edit reaction.                                   |
| EMOTE_CANCEL       | Emoji ID for the Cancel reaction.                                 |
| EMOTE_CHECK        | Emoji ID for the Check mark.                                      |

If you use a `.env` file, just create the file wherever you want to execute the bot or next to the jar file.

The `.env` file uses a very simple syntax where each line is one record and you just put the variable name on the left side of an equal sign (`=`) and then put the value on the right side.

The `.env` file will look something like this:

```env
VARIABLE1=value1
VARIABLE2=value2
VARIABLE3=value3
```

#### Logging Configuration

The configuration for log4j 2 is located in `src/main/resources`.

You may change it but changing the configuration requires you to rebuild the package before execution as the changes will not take effect otherwise.

## Credits

- Christopher Bitler: original bot development
- Tyler "Inverness": original bot idea

- Jeremy D. Thralls, Franziska Mueller: extensions to the original bot

## License

GPLv3
