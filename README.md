![saves2](https://user-images.githubusercontent.com/51991564/114282630-55edc980-9a45-11eb-8fda-a3a921783cbc.png)

SpigotMC: https://www.rayzs.de/rayzsanticrasher/get

Welcome to the one of the best anticrash-plugin on the spigotmc forum.
You're asking why my plugin is one of the best?
Then look at these features m8.

-------------------------------------------------------------------------------------

What you can do, if my anticrash is blocking innocent people?
In a situation like this, i will please you to write my private on spigotmc and sent me what is happening by starting the following action. Remember. I also need the kickreason on a situation like this. Then you can go to the "checks.yml" and can set the check on "false".

What for permissions do I have included?
Type commands included 'illegal symbols' > rayzsanticrasher.bypass
 Get actionbar on live attack > rayzsanticrasher.attack
 /rac use > rayzsanticrasher.use
 /rac notify > rayzsanticrasher.notify
 /rac addons > rayzsanticrasher.addons
 /rac tps > rayzsanticrasher.tps
/rac unblockips > rayzsanticrasher.unblockips

How you can interact with my addonsystem.
Then click here.

What my antivpn-system actually do?
I actually use a api website for this.(https://vpnapi.io/)
 On every join to the server, my plugin ask in a scheduler and new thread if the connection of the player is used by a virtual proxy. If the player got detected by using a vpn, he will get blocked by my system (also iptabled - optional).

What my antibot-system actually do?
In my antibot, the user will get teleported by joining the server minimal blocks in the air. By multiplying the ping of the user with my scheduler, can I check if the user is standing on this location or not. If the play is standing on the location, he will be kicked, cause he is a bot. Everyone is able to join the server. Bots will be get kicked, cause they aren't moving then they got teleported.

What my firewall actually do?
My firewall register every success connection from a player in a whitelist. During a attack where lots of connections attacking the server, the plugin is going to activate the firewall. All after connections during this time are gonna be blocked and will be added to a waitinglist. In this waitinglist is my system checking the provider of the actually ip and is checking if the ip is a virtual proxy or not. If not, the ip will be added to the whitelist and the user can join the server. If not, the ip will be blocked / iptabled (optional).
 After 5 seconds the system will check if everything is now over. If the attack is over, everyone can join the server and not no longer only the whitelisted people. The blocked ips will not be able to create a connection to the server again.

What does 'OnlyProxyPing' mean?
OnlyProxyPing is my new creation of OnlyProxyJoin. OnlyProxyJoin kicked players which are connecting by a ip which is not allowed. In OnlyProxyPing is the player even not able to ping the server as well.
 This is by default deactivated and can be activated in the checks.yml.

-------------------------------------------------------------------------------------
Security is important... Use RAC for it! :)
