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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import uk.co.ElmHoe.Bukkit.ConfigUtility;
import uk.co.ElmHoe.Bukkit.Utilities.StringUtility;

public class Main extends JavaPlugin implements Listener {
	private static String localVersion;
	// private static String pluginName;
	public Map<UUID, Boolean> pvpDisabledPlayers;// = new HashMap<UUID, Boolean>();
	public Map<UUID, Boolean> adminLogging = new HashMap<UUID, Boolean>();
	public Map<UUID, Boolean> playersInPvP = new HashMap<UUID, Boolean>();

	public void onEnable() {
		pvpDisabledPlayers = new HashMap<UUID, Boolean>();
		Bukkit.getLogger().info("----------- Attempting to enable PvPToggle -----------");
		PluginDescriptionFile pdf = this.getDescription();
		localVersion = pdf.getVersion();
		// pluginName = pdf.getName();
		ConfigUtility.firstRun(this);
		Bukkit.getLogger().info("Events Registered");
		Bukkit.getPluginManager().registerEvents(this, this);
		/*
		 * UpdateUtility.updateChecking( spigotURL, localVersion, pluginName,
		 * updateBitlyLink );
		 */
		readFromConfig();
		Bukkit.getLogger().info("----------- Enabled PvPToggle V" + localVersion + " -----------");

	}

	private String noPermission;
	private String wrongUsage;
	private Boolean OPsToBypass;
	private String bothDisabled;
	private String playerCausingHarmDisabled;
	private String playerGettingHurtDisabled;
	private String OPUsingBypassedPvP;
	private boolean DisabledByDefault = false;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pvptoggle")) {
			if (sender.hasPermission("pvptoggle.use")) {
				UUID playerUUID = Bukkit.getServer().getPlayer(sender.getName()).getUniqueId();
				switch (args.length) {
				case 0:
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					sender.sendMessage(StringUtility.format("&b&lPvPToggle Usage"));
					sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <off/on>' &bto toggle PvP on or off."));
					if (sender.hasPermission("pvptoggle.admin")) {
						sender.sendMessage(StringUtility.format(
								"&3&o'/PvPToggle <user> [off/on]'&b use just user to view current status for that user, or disable it by adding either off/on."));
					}
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					break;

				case 1:
					if (args[0].equalsIgnoreCase("off") || (args[0].equalsIgnoreCase("disable"))) {
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.put(playerUUID, true);
						}
						pvpDisabledPlayers.put(playerUUID, true);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have disabled PvP"));
					} else if (args[0].equalsIgnoreCase("on") || (args[0].equalsIgnoreCase("enable"))){
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.put(playerUUID, false);
						}
						pvpDisabledPlayers.put(playerUUID, false);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have enabled PvP"));
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (sender.hasPermission("pvptoggle.admin")) {
							try {
								for (UUID i : pvpDisabledPlayers.keySet()) {
									String y = pvpDisabledPlayers.get(i).booleanValue() + "";
									ConfigUtility.populateConfig("Data." + i, y.toLowerCase(), "Boolean");
								}
								for (UUID a : adminLogging.keySet()) {
									String y = adminLogging.get(a).booleanValue() + "";
									ConfigUtility.populateConfig("Admins." + a, y.toLowerCase(), "Boolean");
								}
								ConfigUtility.saveConfiguration();
							} catch (NullPointerException e) {
							}
							ConfigUtility.loadYamls();
							readFromConfig();
							sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o Config Reloaded."));
						}
					} else {
						sender.sendMessage(StringUtility.format(wrongUsage));
					}
					break;

				case 2:
					if (sender.hasPermission("pvptoggle.admin")) {
						String player = args[0];
						UUID puuid = null;
						Boolean yn = false;
						if (args[1].equalsIgnoreCase("on") || (args[1].equalsIgnoreCase("enable"))) {
							yn = true;
						}else if (args[1].equalsIgnoreCase("off")||args[1].equalsIgnoreCase("disable")) {
							
						}else {
							break;
						}
						try {
							puuid = Bukkit.getServer().getPlayer(player).getUniqueId();
						} catch (Exception e) {
							sender.sendMessage("Failed to get UUID for Player: " + player);
							break;
						}
						Player p = Bukkit.getPlayer(puuid);
						pvpDisabledPlayers.put(puuid, yn);
						String en = "";

						if (yn == true) {
							en = "enabled";
							if (pvpDisabledPlayers.containsKey(playerUUID)) {
								pvpDisabledPlayers.put(playerUUID, false);
							}
							pvpDisabledPlayers.put(playerUUID, false);
						}
						if (yn == false) {
							en = "disabled";
							if (pvpDisabledPlayers.containsKey(playerUUID)) {
								pvpDisabledPlayers.put(playerUUID, true);
							}
							pvpDisabledPlayers.put(playerUUID, true);
						}

						if (p.isOnline()) {
							p.sendMessage(StringUtility.format(
									"&7[&3PvPToggle&7] &3" + sender.getName() + "&3 has "+ en + " your PvP."));
						}
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3You have successfully changed "
								+ p.getName() + "'s&3 PvP Status to " + en + "."));

					} else {
						sender.sendMessage(StringUtility.format(noPermission));
					}
					break;

				default:
					sender.sendMessage(StringUtility.format(wrongUsage));
					break;
				}
			} else {
				sender.sendMessage(StringUtility.format(noPermission));
			}
			return true;
		}
		return false;
	}

	public void onDisable() {
		for (UUID i : pvpDisabledPlayers.keySet()) {
			String y = pvpDisabledPlayers.get(i).booleanValue() + "";
			ConfigUtility.populateConfig("Data." + i, y.toLowerCase(), "Boolean");
		}
		for (UUID a : adminLogging.keySet()) {
			String y = adminLogging.get(a).booleanValue() + "";
			ConfigUtility.populateConfig("Admins." + a, y.toLowerCase(), "Boolean");
		}
		ConfigUtility.saveConfiguration();
	}

	/**
	 * This will check if the specific players have PVP disabled. 
	 * 
	 * @param 	playerGettingHurt 	This will be the player that's currently being attacked
	 * @param 	playerCausingHarm	This will be the player that's doing the attacking.
	 * 
	 * @return 	1 - Cancelled 	- 	Both players have PvP Disabled.<br>
	 * 2 - Cancelled 	- 	The player that is getting hurt has PvP Disabled.<br>
	 * 3 - Cancelled 	- 	The player that is causing harm has it disabled.<br>
	 * 4 - Allowed 	-	The player causing harm is OP and can bypass.<br>
	 * 0 - Allowed		- 	Allows the PvP to go on as normal.
	 */
	public int isPvPDisabledForPlayers(Player playerGettingHurt, Player playerCausingHarm) {
		UUID playerHurtUUID = playerGettingHurt.getUniqueId();
		UUID playerHarmUUID = playerCausingHarm.getUniqueId();
		
		/*
		 * If the player isn't already in an array, it means they've not got a set
		 * value. Instead, we then set the value to the default value the server-owner
		 * has set.
		 */
		if (!pvpDisabledPlayers.containsKey(playerHurtUUID)) {
			pvpDisabledPlayers.put(playerHurtUUID, DisabledByDefault);
		} else if (!pvpDisabledPlayers.containsKey(playerHarmUUID)) {
			pvpDisabledPlayers.put(playerHarmUUID, DisabledByDefault);
		}

		else if (pvpDisabledPlayers.get(playerHurtUUID) && (playerCausingHarm.isOp()) && (OPsToBypass)) {
			playerCausingHarm.sendMessage(StringUtility.format(OPUsingBypassedPvP)
					.replace("{PLAYERGETTINGHURT}", playerGettingHurt.getName())
					.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
			addPlayerToPvPTimer(playerHurtUUID, playerHarmUUID);
			return 4;
			
		}
		
		else if ((pvpDisabledPlayers.get(playerHurtUUID) == true) && (pvpDisabledPlayers.get(playerHarmUUID) == true)) {
			playerCausingHarm.sendMessage(StringUtility.format(bothDisabled)
					.replace("{PLAYERGETTINGHURT}", playerGettingHurt.getName())
					.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
			return 1;
		}

		else if (pvpDisabledPlayers.get(playerHurtUUID)) {
			playerCausingHarm.sendMessage(StringUtility.format(playerGettingHurtDisabled)
					.replace("{PLAYERGETTINGHURT}", playerGettingHurt.getName())
					.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
			return 2;
		}

		else if (pvpDisabledPlayers.get(playerHarmUUID)) {
			playerCausingHarm.sendMessage(StringUtility.format(playerCausingHarmDisabled)
					.replace("{PLAYERGETTINGHURT}", playerGettingHurt.getName())
					.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
			return 3;
		}
		addPlayerToPvPTimer(playerHurtUUID, playerHarmUUID);

		return 0;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent e) {
		if ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player)) {
			UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();
			UUID playerCausingHarm = Bukkit.getPlayer(e.getDamager().getName()).getUniqueId();
			
			if (e.getDamager().getName() == e.getEntity().getName()) {
				//Do nothing, don't even check if the player is attacking themselves.
			}else {
				int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(playerGettingHurt),Bukkit.getPlayer(playerCausingHarm));
				switch (pvpCheck) {
				case 4:
					e.setCancelled(false);
					break;
				case 1:
					e.setCancelled(true);
					break;
				case 2:
					e.setCancelled(true);
					break;
				case 3:
					e.setCancelled(true);
					break;
				default:
					break;
				}
			}

		} else if ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Projectile)) {
			final Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player playerCausingHarmp = (Player) arrow.getShooter();
				UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();
				Player playerGettingHurtp = (Player) e.getEntity();
				if (e.getDamager().getName() == e.getEntity().getName()) {
					//Do nothing, don't even check if the player is attacking themselves.
				}else {
					int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(playerGettingHurt), playerCausingHarmp);
					switch (pvpCheck) {
					case 4:
						e.setCancelled(false);
						break;
					case 1:
						arrow.remove();
						playerGettingHurtp.setFireTicks(0);
						e.setCancelled(true);
						break;
					case 2:
						arrow.remove();
						playerGettingHurtp.setFireTicks(0);
						e.setCancelled(true);
						break;
					case 3:
						arrow.remove();
						playerGettingHurtp.setFireTicks(0);
						e.setCancelled(true);
						break;
					default:
						break;
					}
				}

			}
		}
	}

/*	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerProjectileEvent(ProjectileHitEvent e) {
		if ((e.getEntity() instanceof Arrow) && (e.getHitEntity() instanceof Player)) {
			final Arrow arrow = (Arrow) e.getHitEntity();
			if (arrow.getShooter() instanceof Player) {
				Player playerCausingHarmp = (Player) arrow.getShooter();
				UUID playerCausingHarm = playerCausingHarmp.getUniqueId();
				UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();

				int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(playerGettingHurt), Bukkit.getPlayer(playerCausingHarm));
				switch (pvpCheck) {
				case 4:
					break;
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				default:
					break;
				}

			}
		}
	}
*/
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFishEvent(PlayerFishEvent e) {
		Player playerFishing = e.getPlayer();
		Entity caught = e.getCaught();
		if (caught == null) {
			// Do nothing lol.
		} else if (caught instanceof Player) {
			Player playerCaught = (Player) caught;
			if (playerCaught == playerFishing) {
				//Do nothing, again.
			}else {
				int pvpCheck = isPvPDisabledForPlayers(playerCaught, playerFishing);
				switch (pvpCheck) {
				case 4:
					e.setCancelled(false);
					break;
				case 1:
					e.getHook().remove();
					e.setCancelled(true);
					break;
				case 2:
					e.getHook().remove();
					e.setCancelled(true);
					break;
				case 3:
					e.getHook().remove();
					e.setCancelled(true);
					break;
				default:
					break;
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!pvpDisabledPlayers.containsKey(p.getUniqueId())) {
			if (ConfigUtility.configContainsBoolean("Data." + p.getUniqueId())) {
				boolean b = ConfigUtility.configReadBoolean("Data." + p.getUniqueId());
				pvpDisabledPlayers.put(p.getUniqueId(), b);
			} else {
				pvpDisabledPlayers.put(p.getUniqueId(), DisabledByDefault);
			}
		}
	}

	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (playersInPvP.get(p.getUniqueId())) {
			p.setHealth(0);
			Bukkit.broadcastMessage(p.getName() + " logged out whilst in PvP.");
		}

	}

	public void readFromConfig() {
		/*
		 * Write defaults to configuration file.
		 */
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.NoPermission")) {
			ConfigUtility.populateConfig("DefaultMessages.NoPermission",
					"&7[&3PvPToggle&7] &3&oYou do not have the required permissions to perform this command.",
					"String");
		}
		noPermission = ConfigUtility.configReadString("DefaultMessages.NoPermission");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.WrongUsage")) {
			ConfigUtility.populateConfig("DefaultMessages.WrongUsage",
					"&7[&3PvPToggle&7] &3&oIncorrect usage, use '/pvptoggle' for help.", "String");
		}
		wrongUsage = ConfigUtility.configReadString("DefaultMessages.WrongUsage");

		if (!ConfigUtility.configContainsBoolean("DisabledByDefault")) {
			ConfigUtility.populateConfig("DisabledByDefault", "true", "Boolean");
		}
		DisabledByDefault = ConfigUtility.configReadBoolean("DisabledByDefault");

		if (!ConfigUtility.configContainsBoolean("OPsToBypass")) {
			ConfigUtility.populateConfig("OPsToBypass", "false", "Boolean");
		}
		OPsToBypass = ConfigUtility.configReadBoolean("OPsToBypass");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.BothPlayersPvPDisabled")) {
			ConfigUtility.populateConfig("DefaultMessages.BothPlayersPvPDisabled",
					"&7[&3PvPToggle&7] &3&oBoth you ({PLAYERCAUSINGHARM}) and {PLAYERGETTINGHURT} have PvP Disabled.",
					"String");
		}
		bothDisabled = ConfigUtility.configReadString("DefaultMessages.BothPlayersPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerGettingHurtPvPDisabled")) {
			ConfigUtility.populateConfig("DefaultMessages.PlayerGettingHurtPvPDisabled",
					"&7[&3PvPToggle&7] &3&o{PLAYERGETTINGHURT} has PvP Disabled.", "String");
		}
		playerGettingHurtDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerGettingHurtPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerCausingHarmPvPDisabled")) {
			ConfigUtility.populateConfig("DefaultMessages.PlayerCausingHarmPvPDisabled",
					"&7[&3PvPToggle&7] &3&oYour PvP is disabled. Change this by using /PvPToggle.", "String");
		}
		playerCausingHarmDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerCausingHarmPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.OPUsingBypassedPvP")) {
			ConfigUtility.populateConfig("DefaultMessages.OPUsingBypassedPvP",
					"&7[&3PvPToggle&7] &3&oSorry {PLAYERCAUSINGHARM}, you're currently attacking {PLAYERGETTINGHURT} whilst your PvP is disabled for yourself. Please use /PvPToggle to change this..",
					"String");
		}
		OPUsingBypassedPvP = ConfigUtility.configReadString("DefaultMessages.OPUsingBypassedPvP");

		ConfigUtility.saveConfiguration();
		/*
		 * We check the settings currently saved by players. Data.PlayerUUID
		 * <false/true>
		 */
		try {
			for (String i : getConfig().getConfigurationSection("Data").getKeys(false)) {
				try {
					boolean y = ConfigUtility.configReadBoolean("Data." + i);
					pvpDisabledPlayers.put(UUID.fromString(i), y);
				} catch (NullPointerException e) {
					pvpDisabledPlayers.put(UUID.fromString(i), false);
				}
			}
		} catch (NullPointerException e) {
		}
		/*
		 * We check the admins settings. Admins.PlayerUUID.LoggingEnabled <true/false>
		 */
		try {
			for (String logging : getConfig().getConfigurationSection("Admins").getKeys(false)) {
				try {
					boolean y = ConfigUtility.configReadBoolean("Admins." + logging + ".LoggingEnabled");
					adminLogging.put(UUID.fromString(logging), y);
				} catch (NullPointerException e) {
					adminLogging.put(UUID.fromString(logging), false);
				}
			}
		} catch (NullPointerException e) {
		}
	}

	public void addPlayerToPvPTimer(UUID playerToAdd) {
		playersInPvP.put(playerToAdd, true);
		new BukkitRunnable() {
			@Override
			public void run() {
				removePlayersFromPvPTimer(playerToAdd);
			}
		}.runTaskLater(this, 100);
	}

	public void addPlayerToPvPTimer(UUID playerToAdd, UUID playerToAlsoAdd) {
		playersInPvP.put(playerToAdd, true);
		playersInPvP.put(playerToAlsoAdd, true);
		new BukkitRunnable() {
			@Override
			public void run() {
				removePlayersFromPvPTimer(playerToAdd, playerToAlsoAdd);
			}
		}.runTaskLater(this, 100);
	}

	public void removePlayersFromPvPTimer(UUID playerToRemove) {
		playersInPvP.remove(playerToRemove);
	}

	public void removePlayersFromPvPTimer(UUID playerToRemove, UUID playerToAlsoRemove) {
		playersInPvP.remove(playerToRemove);
		playersInPvP.remove(playerToAlsoRemove);
	}

	/*
	 * TO DO
	 */
	public void listPlayersInPvPTimer(CommandSender s) {
	}

}
