slipcor's ten.java submission
==============================

[![ten.java](https://cdn.mediacru.sh/hu4CJqRD7AiB.svg)](https://tenjava.com/)

This is a submission for the 2014 ten.java contest.

- __Theme:__ How can energy be harnessed and used in the Minecraft world?
- __Time:__ Time 2 (7/12/2014 09:00 to 7/12/2014 19:00 UTC)
- __MC Version:__ 1.7.9 (latest Bukkit beta)
- __Stream URL:__ https://twitch.tv/slipcor

---------------------------------------

Features
--------
* Charge a custom block with power signals
* supports any byte format redstone signal (scaleable!)
* supports any Material your implementation supports!
* Configurable:
** Power sources may burn out
*** Torch -> Torch_Off
*** Redstone_Block -> Coal_Block
*** Lever On -> Lever Off
* Block keeps charge when broken
* Displays charge via item frames
* Display charge via displayname
* Can be charged by:
** daylight sensor / any other direct source
** applied indirect current
* Will send out charge to:
** direct redstone connection

---------------------------------------

Usage
-----

1. Install plugin
2. run the command /b or /battery
3. place the Iron Block (or whatever you configured it to be)
4. Watch it consuming all your redstone >)
5. Remove it before it re-sends the current to the burnt out wires!
6. Place it where you need the power
7. Enjoy!