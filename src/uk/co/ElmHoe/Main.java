package uk.co.ElmHoe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ElmHoe.Bukkit.ConfigUtility;
import uk.co.ElmHoe.Bukkit.Utilities.StringUtility;

public class Main extends JavaPlugin implements Listener {
	private static String localVersion;

	public Map<UUID, Boolean> pvpDisabledPlayers;
	public Map<UUID, Boolean> playersInPvP = new HashMap<UUID, Boolean>();

	public void onEnable() 
	{
		pvpDisabledPlayers = new HashMap<UUID, Boolean>();
		Bukkit.getLogger().info("----------- Attempting to enable PvPToggle -----------");
		PluginDescriptionFile pdf = this.getDescription();
		localVersion = pdf.getVersion();
		ConfigUtility.firstRun(this);
		Bukkit.getLogger().info("Events Registered");
		Bukkit.getPluginManager().registerEvents(this, this);

		readFromConfig();
		Bukkit.getLogger().info("----------- Enabled PvPToggle v" + localVersion + " -----------");

	}
	private Boolean OPsToBypass;
	private Boolean AllowSelfHarming;
	private Boolean DisabledByDefault = false;

	private String noPermission;
	private String wrongUsage;
	private String bothDisabled;
	private String playerCausingHarmDisabled;
	private String playerGettingHurtDisabled;
	private String OPUsingBypassedPvP;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		if (cmd.getName().equalsIgnoreCase("pvptoggle")) 
		{
			if (sender.hasPermission("pvptoggle.use")) 
			{
				UUID playerUUID = Bukkit.getServer().getPlayer(sender.getName()).getUniqueId();
				if (args.length == 0)
				{
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					sender.sendMessage(StringUtility.format("&b&lPvPToggle Usage"));
					sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <off/on>' &bto toggle PvP on or off."));
					
					if (sender.hasPermission("pvptoggle.admin"))
						sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <user> [off/on]'&b use just user to view current status for that user, or disable it by adding either off/on."));
					
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
				} 
				else if (args.length == 1)
				{
					if (args[0].equalsIgnoreCase("off") || (args[0].equalsIgnoreCase("disable"))) 
					{
						pvpDisabledPlayers.put(playerUUID, true);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have disabled PvP"));
					} 
					else if (args[0].equalsIgnoreCase("on") || (args[0].equalsIgnoreCase("enable")))
					{
						pvpDisabledPlayers.put(playerUUID, false);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have enabled PvP"));
					}
					else if (args[0].equalsIgnoreCase("reload")) 
					{
						if (sender.hasPermission("pvptoggle.admin")) 
						{
							try {
								for (UUID i : pvpDisabledPlayers.keySet()) 
								{
									String y = pvpDisabledPlayers.get(i).booleanValue() + "";
									ConfigUtility.populateConfig("Data." + i, y.toLowerCase(), "Boolean");
								}
								ConfigUtility.saveConfiguration();
							} 
							catch (NullPointerException e) 
							{
									//Do nothing
							}
							ConfigUtility.loadYamls();
							readFromConfig();
							sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o Config Reloaded."));
						}
					} 
					else 
					{
						sender.sendMessage(StringUtility.format(wrongUsage));
					}
				} 
				else if (args.length == 2)
				{
					if (sender.hasPermission("pvptoggle.admin")) 
					{
						String player = args[0];
						UUID puuid = null;
						Boolean yn = false;
						if (args[1].equalsIgnoreCase("on") || (args[1].equalsIgnoreCase("enable"))) 
							yn = true;
						try 
						{
							puuid = Bukkit.getServer().getPlayer(player).getUniqueId();
						} 
						catch (Exception e) 
						{
							sender.sendMessage("Failed to get UUID for Player: " + player);
						}
						Player p = Bukkit.getPlayer(puuid);
						pvpDisabledPlayers.put(puuid, yn);
						String en = "";

						if (yn) 
						{
							en = "enabled";
							pvpDisabledPlayers.put(playerUUID, false);
						}
						else
						{
							en = "disabled";
							pvpDisabledPlayers.put(playerUUID, true);
						}

						if (p.isOnline()) 
						{
							p.sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3" + sender.getName() + "&3 has "+ en + " your PvP."));
						}
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3You have successfully changed "
								+ p.getName() + "'s&3 PvP Status to " + en + "."));

					} 
					else 
					{
						sender.sendMessage(StringUtility.format(noPermission));
					}
				}
				else
				{
					sender.sendMessage(StringUtility.format(wrongUsage));
				}
			} 
			else 
				sender.sendMessage(StringUtility.format(noPermission));
			return true;
		}
		return false;
	}

	public void onDisable() 
	{
		for (UUID i : pvpDisabledPlayers.keySet()) 
		{
			String y = pvpDisabledPlayers.get(i).booleanValue() + "";
			ConfigUtility.populateConfig("Data." + i, y.toLowerCase(), "Boolean");
		}
		ConfigUtility.saveConfiguration();
	}

	/**
	 * This will check if the specific players have PVP disabled. 
	 * 
	 * @param 	userGettingHurt 	This will be the player that's currently being attacked
	 * @param 	userCausingHarm	This will be the player that's doing the attacking.
	 * 
	 * @return 	1 - Cancelled 	- 	Both players have PvP Disabled.<br>
	 * 2 - Cancelled 	- 	The player that is getting hurt has PvP Disabled.<br>
	 * 3 - Cancelled 	- 	The player that is causing harm has it disabled.<br>
	 * 4 - Allowed 		-	The player causing harm is OP and can bypass.<br>
	 * 0 - Allowed		- 	Allows the PvP to go on as normal.
	 */
	public int isPvPDisabledForPlayers(Player userGettingHurt, Player userCausingHarm) 
	{
		UUID userHurtUUID = userGettingHurt.getUniqueId();
		UUID userHarmingUUID = userCausingHarm.getUniqueId();
		
		/*
		 * If the player isn't already in an array, it means they've not got a set
		 * value. Instead, we then set the value to the default value the server-owner
		 * has set.
		 */
		if (!(pvpDisabledPlayers.containsKey(userHurtUUID)))
			pvpDisabledPlayers.put(userHurtUUID, DisabledByDefault);

		else if (!(pvpDisabledPlayers.containsKey(userHarmingUUID)))
			pvpDisabledPlayers.put(userHarmingUUID, DisabledByDefault);

		else if (pvpDisabledPlayers.get(userHurtUUID) && (userCausingHarm.isOp()) && (OPsToBypass)) 
		{
			userCausingHarm.sendMessage(StringUtility.format(OPUsingBypassedPvP)
					.replace("{userGettingHurt}", userGettingHurt.getName())
					.replace("{userCausingHarm}", userCausingHarm.getName()));
			return 4;
			
		}
		
		else if ((pvpDisabledPlayers.get(userHurtUUID) == true) && (pvpDisabledPlayers.get(userHarmingUUID) == true)) 
		{
			userCausingHarm.sendMessage(StringUtility.format(bothDisabled)
					.replace("{userGettingHurt}", userGettingHurt.getName())
					.replace("{userCausingHarm}", userCausingHarm.getName()));
			return 1;
		}

		else if (pvpDisabledPlayers.get(userHurtUUID)) 
		{
			userCausingHarm.sendMessage(StringUtility.format(playerGettingHurtDisabled)
					.replace("{userGettingHurt}", userGettingHurt.getName())
					.replace("{userCausingHarm}", userCausingHarm.getName()));
			return 2;
		}

		else if (pvpDisabledPlayers.get(userHarmingUUID)) 
		{
			userCausingHarm.sendMessage(StringUtility.format(playerCausingHarmDisabled)
					.replace("{userGettingHurt}", userGettingHurt.getName())
					.replace("{userCausingHarm}", userCausingHarm.getName()));
			return 3;
		}
		return 0;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent e) 
	{
		if ((e.getEntity() instanceof Player) && ((e.getDamager() instanceof Player) || (e.getDamager() instanceof Arrow)))
		{
			
			UUID userGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();

			if (e.getDamager() instanceof Player)
			{
				UUID userCausingHarm = Bukkit.getPlayer(e.getDamager().getName()).getUniqueId();
				int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(userGettingHurt),Bukkit.getPlayer(userCausingHarm));

				if (userGettingHurt == userCausingHarm) 
					if (AllowSelfHarming == false)
						if (pvpCheck == 1)
							e.setCancelled(true);
				else 
					if (pvpCheck == 1 || pvpCheck == 2 || pvpCheck == 3)
						e.setCancelled(true);
			}
			if (e.getDamager() instanceof Arrow)
			{
				final Arrow arrow = (Arrow) e.getDamager();
				if (arrow.getShooter() instanceof Player) 
				{
					Player playerCausingHarm = (Player) arrow.getShooter();
					Player playerGettingHurt = (Player) e.getEntity();
					int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(userGettingHurt), playerCausingHarm);

					if (playerCausingHarm == playerGettingHurt)
						if (AllowSelfHarming == false)
							if (pvpCheck == 1)
								e.setCancelled(true);
					else
						if (pvpCheck == 1 || pvpCheck == 2 || pvpCheck == 3)
						{
							arrow.remove();
							playerGettingHurt.setFireTicks(0);
							e.setCancelled(true);
						}
				}

			}
		
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFishEvent(PlayerFishEvent e) 
	{
		Player playerFishing = e.getPlayer();
		Entity caught = e.getCaught();
		if (caught instanceof Player)
		{
			Player playerCaught = (Player) caught;
			if (playerCaught == playerFishing)
			{
				//Do nothing, again.
			} 
			else 
			{
				int pvpCheck = isPvPDisabledForPlayers(playerCaught, playerFishing);
				if (pvpCheck == 1 || pvpCheck == 2 || pvpCheck == 3)
				{
					e.getHook().remove();
					e.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(PlayerJoinEvent e) 
	{
		Player p = e.getPlayer();
		if (!pvpDisabledPlayers.containsKey(p.getUniqueId()))
			if (ConfigUtility.configContainsBoolean("Data." + p.getUniqueId())) 
			{
				boolean b = ConfigUtility.configReadBoolean("Data." + p.getUniqueId());
				pvpDisabledPlayers.put(p.getUniqueId(), b);
			} else {
				pvpDisabledPlayers.put(p.getUniqueId(), DisabledByDefault);
			}
	}

	public void readFromConfig()
	{
		/*
		 * Write defaults to configuration file.
		 */
		//Settings
		if (!ConfigUtility.configContainsBoolean("Settings.AllowSelfHarming"))
			ConfigUtility.populateConfig("Settings.AllowSelfHarming","false","Boolean");
		
		if (!ConfigUtility.configContainsBoolean("Settings.DisabledByDefault"))
			ConfigUtility.populateConfig("Settings.DisabledByDefault","true","Boolean");

		if (!ConfigUtility.configContainsBoolean("Settings.OPsToBypass"))
			ConfigUtility.populateConfig("Settings.OPsToBypass","false","Boolean");

		//Default Messages
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.NoPermission"))
			ConfigUtility.populateConfig("DefaultMessages.NoPermission","&7[&3PvPToggle&7] &3&oYou do not have the required permissions to perform this command.","String");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.WrongUsage"))
			ConfigUtility.populateConfig("DefaultMessages.WrongUsage","&7[&3PvPToggle&7] &3&oIncorrect usage, use '/pvptoggle' for help.","String");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.BothPlayersPvPDisabled"))
			ConfigUtility.populateConfig("DefaultMessages.BothPlayersPvPDisabled","&7[&3PvPToggle&7] &3&oBoth you ({userCausingHarm}) and {userGettingHurt} have PvP Disabled.","String");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerGettingHurtPvPDisabled"))
			ConfigUtility.populateConfig("DefaultMessages.PlayerGettingHurtPvPDisabled","&7[&3PvPToggle&7] &3&o{userGettingHurt} has PvP Disabled.","String");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerCausingHarmPvPDisabled"))
			ConfigUtility.populateConfig("DefaultMessages.PlayerCausingHarmPvPDisabled","&7[&3PvPToggle&7] &3&oYour PvP is disabled. Change this by using /PvPToggle.","String");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.OPUsingBypassedPvP"))
			ConfigUtility.populateConfig("DefaultMessages.OPUsingBypassedPvP","&7[&3PvPToggle&7] &3&oSorry {userCausingHarm}, you're currently attacking {userGettingHurt} whilst your PvP is disabled for yourself. Please use /PvPToggle to change this..","String");

		
		noPermission = ConfigUtility.configReadString("DefaultMessages.NoPermission");
		wrongUsage = ConfigUtility.configReadString("DefaultMessages.WrongUsage");
		DisabledByDefault = ConfigUtility.configReadBoolean("Settings.DisabledByDefault");
		OPsToBypass = ConfigUtility.configReadBoolean("Settings.OPsToBypass");
		bothDisabled = ConfigUtility.configReadString("DefaultMessages.BothPlayersPvPDisabled");
		playerGettingHurtDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerGettingHurtPvPDisabled");
		playerCausingHarmDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerCausingHarmPvPDisabled");
		OPUsingBypassedPvP = ConfigUtility.configReadString("DefaultMessages.OPUsingBypassedPvP");
		AllowSelfHarming = ConfigUtility.configReadBoolean("Settings.AllowSelfHarming");

		
		ConfigUtility.saveConfiguration();
		/*
		 * We check the settings currently saved by players. Data.PlayerUUID
		 * <false/true>
		 */
		try {
			for (String i : getConfig().getConfigurationSection("Data").getKeys(false)) 
			{
				try {
					boolean y = ConfigUtility.configReadBoolean("Data." + i);
					pvpDisabledPlayers.put(UUID.fromString(i), y);
				} 
				catch (NullPointerException e) 
				{
					pvpDisabledPlayers.put(UUID.fromString(i), false);
				}
			}
		} 
		catch (NullPointerException e)
		{
			
		}
	}
}
