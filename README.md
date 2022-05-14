# modupdater

CLI application for updating Fabric, Quilt and Forge mods.

## Default commands

### Config

Show the config used

    Usage: config
    Aliases: config

### Download

Download all loaded mods. You can specify the name of one mod or loader

    Usage: download ?[mod(-s)/downloader]
    Aliases: download, d

### Exit

Exit the command line interface

    Usage: exit
    Aliases: exit

### Help

Get help

    Usage: help
    Aliases: help

### Info

Get information about the mod

    Usage: info [mod]
    Aliases: info

### Install

Downloads and installs the specified mods

    Usage: install ?[mod(-s)/downloader]
    Aliases: install, i

### List

List all loaded mods

    Usage: list
    Aliases: list, l

### Load

Load mods from the default mods folder

    Usage: load
    Aliases: load

### Search

Trying to find a new downloader for the specified mod

    Usage: search ?[mod(-s)/downloader]
    Aliases: search, s

### Set specific download data

Set specific data for the mod that takes precedence over the download link

    Usage: set-specific [mod] [value]
    Aliases: set-specific, ss

### Set url

Set a download url for the specified mod

    Usage: set-url [mod] [url]
    Aliases: set-url, su

### Setup

Run setup

    Usage: setup
    Aliases: setup

## Default ModDownloaders

### CurseForge

Can download mods from https://www.curseforge.com/minecraft

    Specific download data: numeric project id

### GitHub

Can download mods from https://github.com

    Specific download data: repo_name

### Modrinth

Can download mods from https://modrinth.com

    Specific download data: projectId (like 'sodium' or 'appleskin')

### Searching

Trying to find a mod on Modrinth or CurseForge.

### Unknown

Just copies the last file.