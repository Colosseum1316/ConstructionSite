# ConstructionSite

ConstructionSite is a Spigot plugin. Inspired by [CENSORED] MapParser plugin.

ConstructionSite parses a Minecraft world of certain patterns under certain rules and produces a special config file for [CENSORED] minigame server to use.

ConstructionSite relies on [ColosseumSpigot](https://github.com/Colosseum1316/Colosseum), which is dedicated to Minecraft 1.8.8. 
Any other Spigot 1.8.8 fork probably won't support this plugin properly, beware.

## Layout

The plugin reads 2 directories: `map` and `parse`.

`map` is for existing world saves. The plugin reads every directory inside `map` and tries to load it as Minecraft world save.

`parse` is temporary for world save files that's being parsed by the plugin. <u>**Everything inside `parse` will be deleted upon every plugin activation. DO NOT store any important file in it.**</u>

The 2 directories are in server's working directory, which is usually same as where the server jar is in.

Upon every successful parse, parsed world save is zipped and stored in `plugins/ConstructionSite/parsed`.

## Config

### config.yml

```yaml
parse:
  maximum_radius: 1000
```

### join-text.yml

`join-text.yml` is a list of messages that will be sent upon every player join. In most cases this file can be safely ignored.

## Usage

Drop the plugin jar into `plugins` directory, then start server.
<u>**You're advised to use the plugin in a dedicated Spigot instance instead of mixing with existing stuff.**</u>

To use the plugin, join game and run `/help ConstructionSite` for help.

## FAQs

### Why's the plugin failing to load?

Probably you're not using [ColosseumSpigot](https://github.com/Colosseum1316/Colosseum).

### Why's there no document on commands?

Because the commands in the plugin are subject to change.

### Why should I use the plugin in another server? Can't I just use it in my SMP or whatsoever?

Because the plugin has reliance on a premade world save,
a selected few of XYZ coordinates are hardcoded in the plugin codebase based on it.
As such, for consistency, you're advised to do so.

Secondly, what the plugin provides is never meant for **public** Minecraft servers.
No builder would ever expect a 3-year-old kid ruining their fruit.

### Then why's the plugin licensed under GPL3?

It's because of upstream. Technically no one really cares about it,
unless something dead serious that requires court interference strikes in, which has practically down to zero possibility for this plugin.
It's not AGPL either, just relax. 

### The plugin document sucks!

Then please open a Pull Request instead of constant nonsensical annoyance. We assume you should've figured out the full usage by then.

### What's exactly [CENSORED]?

A clue to you: The plugin is made against a Minecraft minigame server network that shut down in 2023.

If you wish to know what exactly [CENSORED] is, you may contact us by sending an email to `/dev/null` and we will get back to you within 14 business days.

Impatient? Intrigued? Mischievous? Then try posting/guessing/spamming in Issues/Pull Requests/Discussions, and we're pretty sure in this case your GitHub ID would be noted and reported, and **any** post/comment you leave around here would be wiped clean as much as possible. Please don't ever try to reveal [CENSORED] here in any of our repositories, unless you really regard DMCA as a cyber nuclear weapon.

### Ok I'll stop asking what's [CENSORED], but why do you say like that?

It's due to something you would never want to hear about.

### What's that?

I said stop.
