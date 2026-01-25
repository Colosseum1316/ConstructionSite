# ConstructionSite

ConstructionSite is a Spigot plugin. Inspired by [CENSORED] MapParser plugin.

ConstructionSite parses a Minecraft world of certain patterns under certain rules and produces a special config file for [CENSORED] minigame server to use.

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

## Usage

Drop the plugin jar into `plugins` directory, then start server.
<u>**You're advised to use the plugin in a dedicated Spigot instance instead of mixing with existing stuff.**</u>

To use the plugin, join game and run `/help ConstructionSite` for help.

## FAQs

### Why's there no document on commands?

Because the commands in the plugin are subject to change.

### Why should I use the plugin in another server? Can't I just use it in my SMP or whatsoever?

Because the plugin has reliance on a premade world save,
and a selected few of XYZ coordinates are hardcoded in the plugin codebase based on it.
As such, for consistency, you're advised to do so.

Secondly, what the plugin provides is never meant for **public** Minecraft servers.
No builder would ever expect a 3-year-old kid ruining their fruit.
