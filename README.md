BetterRecords
=============
[![curse badge]][curse] [![discord badge]][discord] [![circle badge]][circle]

Better Records adds the ability to download your favorite songs
from the internet directly into your game, for all your friends to hear!

Modpacks
========
You may use this mod in your modpack as long as:

* You link back to the [Curse page][curse]
* You don't make money off of your modpack
* You do not take credit for making any part of this mod. Give credit where credit is due. (NicholasFeldman & Stumblinbear)

Developing / Compiling
======================
1. `gradle [setupDevWorkspace|setupDecompWorkspace] [eclipse|idea]`
2. `gradle build`

[curse]: https://minecraft.curseforge.com/projects/better-records "Link to Curse"
[curse badge]: http://cf.way2muchnoise.eu/full_better-records_downloads.svg "Curse Badge"
[discord]: https://discord.gg/uhQFPUs "Discord Invite Link"
[discord badge]: https://img.shields.io/discord/392066220259803156.svg?colorB=7289DA "Discord Badge"
[circle]: https://circleci.com/gh/NicholasFeldman/BetterRecords
[circle badge]: https://circleci.com/gh/NicholasFeldman/BetterRecords.svg?style=svg

## Release
To release the jar run: `gradle reobfShadowJar`

## Radio Stream directories

You can find most supported radio stations on the [SHOUTcast directory](https://directory.shoutcast.com/) and the [icecast directory](http://dir.xiph.org/).

### Using a radio stream
Each SHOUTcast (IcyStream) server 'advertisement' has a link to a M3U file. You can open this file in Notepad, and copy the URL from there. This will be usable right-away 