package uk.co.ElmHoe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ElmHoe.Bukkit.ConfigUtility;
import uk.co.ElmHoe.Bukkit.UpdateUtility;
import uk.co.ElmHoe.Bukkit.Utilities.StringUtility;

public class Main extends JavaPlugin implements Listener{
private static String localVersion;
private static String pluginName;
public Map<UUID, Boolean> pvpDisabledPlayers = new HashMap<UUID, Boolean>();
public Map<UUID, Boolean> adminLogging = new HashMap<UUID, Boolean>();

	public void onEnable() {
		Bukkit.getLogger().info("----------- Attempting to enable PvPToggle -----------");
		PluginDescriptionFile pdf = this.getDescription();
		localVersion = pdf.getVersion();
		pluginName = pdf.getName();
		ConfigUtility.firstRun(this);
		Bukkit.getPluginManager().registerEvents(this, this);
/*		UpdateUtility.updateChecking(
				spigotURL, 
				localVersion, 
				pluginName, 
				updateBitlyLink
				);*/
		readFromConfig();
		Bukkit.getLogger().info("----------- Enabled PvPToggle V" + localVersion + " -----------");
		
	}
	
	
	private String noPermission;
	private String wrongUsage;
	private Boolean OPsToBypass;
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pvptoggle")) {
			if (sender.hasPermission("pvptoggle.use")) {
				UUID playerUUID = Bukkit.getServer().getPlayer(sender.getName()).getUniqueId();
				if (args.length <= 0) {
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					sender.sendMessage(StringUtility.format("&b&lPvPToggle Usage"));
					sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <off/on>' &bto toggle PvP on or off."));
					if (sender.hasPermission("pvptoggle.admin")) {
						sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <user> [off/on]'&b use just user to view current status for that user, or disable it by adding either off/on."));
					}
				sender.sendMessage(StringUtility.format("&7&m<----------->"));
				
				
				}else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("off")) {
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.remove(playerUUID);
						}
						pvpDisabledPlayers.put(playerUUID, false);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have disabled PvP"));
					}else if (args[0].equalsIgnoreCase("on")) {
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.remove(playerUUID);
						}
						pvpDisabledPlayers.put(playerUUID, true);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have enabled PvP"));
					}else if (args[0].equalsIgnoreCase("reload")) {
						if (sender.hasPermission("pvptoggle.admin")) {
							try{
								for (UUID i : pvpDisabledPlayers.keySet()) {
									debugging(i.toString());
									String y = pvpDisabledPlayers.get(i).booleanValue() + "";
									ConfigUtility.populateConfig("Data." + i, y.toLowerCase(), "Boolean");
								}
								for (UUID a : adminLogging.keySet()) {
									String y = adminLogging.get(a).booleanValue() + "";
									ConfigUtility.populateConfig("Admins." + a, y.toLowerCase(), "Boolean");
								}
								ConfigUtility.saveConfiguration();
							}catch(NullPointerException e) {}
							ConfigUtility.loadYamls();
							sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o Config Reloaded."));
						}
					} else {
						sender.sendMessage(StringUtility.format(wrongUsage));
					}
					
					
				}else if (args.length == 2) {
					if (sender.hasPermission("pvptoggle.admin")) {
						String player = args[0];
						UUID puuid = null;
						Boolean yn = false;
						if (args[1].equalsIgnoreCase("true")){
							yn = true;
						}else if (args[1].equalsIgnoreCase("false")) {
							yn = false;
						}else {
							sender.sendMessage(StringUtility.format(wrongUsage));
							return false;
						}
						try{
							puuid = Bukkit.getServer().getPlayer(player).getUniqueId();
						}catch(Exception e) { sender.sendMessage("Failed to get UUID for Player: " + player); return false;}
						Player p = Bukkit.getPlayer(puuid);
						pvpDisabledPlayers.put(puuid, yn);
						String en = "";
						if (yn == true) {en = "enabled";}
						if (yn == false) {en = "disabled";}
						
						if (p.isOnline()) {
							p.sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3" + sender.getName() + "&3 has set your pvp to " + en + "."));
						}
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3You have successfully changed " + p.getName() + "'s&3 PvP Status to " + en + "."));
					}else {
						sender.sendMessage(StringUtility.format(wrongUsage));
					}
				}
			}else {
				/*
				 * Sender doesn't have permission.
				 */
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
	
/*	@EventHandler(priority = EventPriority.NORMAL)
	public void onBowFire(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player) {
			Player playerShooting = Bukkit.getPlayer(e.getEntity().getName());
			if ((playerShooting.isOp()) && ())
			if (pvpDisabledPlayers.containsKey(playerShooting.getUniqueId() + "")) {
				boolean pvpDisabled = false;
				pvpDisabled = pvpDisabledPlayers.get(playerShooting.getUniqueId() + "");
				if (pvpDisabled == false) {
					playerShooting.sendMessage("&7[&3PvPToggle&7] &3&oYou can't use the bow whilst your PvP is disabled.");
					e.setCancelled(true);
				}
			}
		}
	}*/
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent  e) {
		debugging("#1");
		if ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player)) {
			debugging("#2");
			UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();
			UUID playerCausingHarm = Bukkit.getPlayer(e.getDamager().getName()).getUniqueId();
			if (Bukkit.getPlayer(playerCausingHarm).isOp()) {
				debugging("#3");
				debugging(pvpDisabledPlayers.toString());
				if (OPsToBypass == true) {
					debugging("#4");
					debugging(pvpDisabledPlayers.get(playerCausingHarm).toString());
					if (pvpDisabledPlayers.containsKey(playerCausingHarm)) {
						debugging("#5");
						if (pvpDisabledPlayers.get(playerCausingHarm)) {
							debugging("#6");
							Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oYou've just attacked another player whilst you've got PvP disabled."));
						}
					}
				}else if ((pvpDisabledPlayers.containsKey(playerGettingHurt)) && (pvpDisabledPlayers.containsKey(playerCausingHarm))) {
					debugging("#7");

					Boolean pvp = pvpDisabledPlayers.get(playerGettingHurt);
					Boolean pch = pvpDisabledPlayers.get(playerCausingHarm);
					if ((pvp == false)&&(pch == true)) {
						debugging("#8");

						Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oPlayer " + Bukkit.getPlayer(playerGettingHurt).getName() + " has PvP Disabled"));
						e.setCancelled(true);
					}else if ((pvp == false) && (pch == false)) {
						debugging("#9");

						Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oBoth you and " + Bukkit.getPlayer(playerGettingHurt).getName() + " have PvP Disabled"));
						e.setCancelled(true);
					}else if ((pvp == true) && (pch == false)) {
						debugging("#10");
						
						Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oYou have PvP Disabled"));
						e.setCancelled(true);
					}
				}else if (pvpDisabledPlayers.containsKey(playerGettingHurt)) {
					Boolean pvp = pvpDisabledPlayers.get(playerGettingHurt);
					if (!pvp) {
						
					}
				}else if (pvpDisabledPlayers.containsKey(playerCausingHarm)) {
					
				}
			}else if ((pvpDisabledPlayers.containsKey(playerGettingHurt)) && (pvpDisabledPlayers.containsKey(playerCausingHarm))) {
				debugging("#11");
				Boolean pvp = pvpDisabledPlayers.get(playerGettingHurt);
				Boolean pch = pvpDisabledPlayers.get(playerCausingHarm);
				if ((pvp == false)&&(pch == true)) {
					debugging("#12");

					Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oPlayer " + Bukkit.getPlayer(playerGettingHurt).getName() + " has PvP Disabled"));
					e.setCancelled(true);
				}else if ((pvp == false) && (pch == false)) {
					debugging("#12");
					
					Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oBoth you and " + Bukkit.getPlayer(playerGettingHurt).getName() + " have PvP Disabled"));
					e.setCancelled(true);
				}else if ((pvp == true) && (pch == false)) {
					debugging("#13");

					Bukkit.getPlayer(playerCausingHarm).sendMessage(StringUtility.format("&7[&3PvPToggle&7] &3&oYou have PvP Disabled"));
					e.setCancelled(true);
				}
			}
		}
	}
	
	private boolean debuggingEnabled = false;
	private void debugging(String debuglog) {
		if (debuggingEnabled == true) {
			Bukkit.getLogger().warning(debuglog);
		}
	}
	
	boolean DisabledByDefault = false;
	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (ConfigUtility.configContainsBoolean("Data." + p.getUniqueId())) {
			boolean b = ConfigUtility.configReadBoolean("Data." + p.getUniqueId());
			pvpDisabledPlayers.put(p.getUniqueId(), b);
		}else {
			pvpDisabledPlayers.put(p.getUniqueId(), DisabledByDefault);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDisconnect(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		pvpDisabledPlayers.remove(p.getUniqueId());
	}

	
	public void readFromConfig() {
		/*
		 * Write defaults to configuration file.
		 */
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.NoPermission")) {
			ConfigUtility.populateConfig("DefaultMessages.NoPermission", "&7[&3PvPToggle&7] &3&oYou do not have the required permissions to perform this command.", "String");
		}else { noPermission = ConfigUtility.configReadString("DefaultMessages.NoPermission");}
		
		
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.WrongUsage")) {
			ConfigUtility.populateConfig("DefaultMessages.WrongUsage", "&7[&3PvPToggle&7] &3&oIncorrect usage, use '/pvptoggle' for help.", "String");
		}else { wrongUsage = ConfigUtility.configReadString("DefaultMessages.WrongUsage");}
		
		if (!ConfigUtility.configContainsBoolean("DisabledByDefault")) {
			ConfigUtility.populateConfig("DisabledByDefault", "true", "Boolean");
		}else { DisabledByDefault = ConfigUtility.configReadBoolean("DisabledByDefault");}

		if (!ConfigUtility.configContainsBoolean("OPsToBypass")) {
			ConfigUtility.populateConfig("OPsToBypass", "false", "Boolean");
		}else { OPsToBypass = ConfigUtility.configReadBoolean("OPsToBypass");}
		ConfigUtility.saveConfiguration();
		/*
		 * We check the settings currently saved by players.
		 * Data.PlayerUUID <false/true>
		 */
		for (String i : getConfig().getConfigurationSection("Data").getKeys(false)) {
			try{
				boolean y = ConfigUtility.configReadBoolean("Data." + i);
				pvpDisabledPlayers.put(UUID.fromString(i), y);
			}catch(NullPointerException e) {
				pvpDisabledPlayers.put(UUID.fromString(i), false);
			}
		}
		/*
		 * We check the admins settings.
		 * Admins.PlayerUUID.LoggingEnabled <true/false>
		 */
		try{
			for (String logging : getConfig().getConfigurationSection("Admins").getKeys(false)) {
				try {
					boolean y = ConfigUtility.configReadBoolean("Admins." + logging + ".LoggingEnabled");
					adminLogging.put(UUID.fromString(logging), y);
				}catch(NullPointerException e) {
					adminLogging.put(UUID.fromString(logging), false);
				}
			}
		}catch(NullPointerException e) {	}
	}
}
