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

public class Main extends JavaPlugin implements Listener{
private static String localVersion;
//private static String pluginName;
public Map<UUID, Boolean> pvpDisabledPlayers;// = new HashMap<UUID, Boolean>();
public Map<UUID, Boolean> adminLogging = new HashMap<UUID, Boolean>();

	public void onEnable() {
		pvpDisabledPlayers = new HashMap<UUID, Boolean>();
		Bukkit.getLogger().info("----------- Attempting to enable PvPToggle -----------");
		PluginDescriptionFile pdf = this.getDescription();
		localVersion = pdf.getVersion();
		//pluginName = pdf.getName();
		ConfigUtility.firstRun(this);
		Bukkit.getLogger().info("Events Registered");
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
	private String bothDisabled;
	private String playerCausingHarmDisabled;
	private String playerGettingHurtDisabled;
	private String OPUsingBypassedPvP;
	private boolean DisabledByDefault = false;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("pvptoggle")) {
			if (sender.hasPermission("pvptoggle.use")) {
				UUID playerUUID = Bukkit.getServer().getPlayer(sender.getName()).getUniqueId();
				switch(args.length) {
				case 0:
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					sender.sendMessage(StringUtility.format("&b&lPvPToggle Usage"));
					sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <off/on>' &bto toggle PvP on or off."));
					if (sender.hasPermission("pvptoggle.admin")) {
						sender.sendMessage(StringUtility.format("&3&o'/PvPToggle <user> [off/on]'&b use just user to view current status for that user, or disable it by adding either off/on."));
					}
					sender.sendMessage(StringUtility.format("&7&m<----------->"));
					break;
					
					
				case 1:
					if (args[0].equalsIgnoreCase("off")) {
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.put(playerUUID,true);
						}
						pvpDisabledPlayers.put(playerUUID, true);
						sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o You have disabled PvP"));
					}else if (args[0].equalsIgnoreCase("on")) {
						if (pvpDisabledPlayers.containsKey(playerUUID)) {
							pvpDisabledPlayers.put(playerUUID, false);
						}
						pvpDisabledPlayers.put(playerUUID, false);
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
							readFromConfig();
							sender.sendMessage(StringUtility.format("&7[&3PvPToggle&7]&3&o Config Reloaded."));
						}
					} else { sender.sendMessage(StringUtility.format(wrongUsage)); }
					break;
				
				
				case 2:
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
						
					}else { sender.sendMessage(StringUtility.format(noPermission)); }
					break;
				
				
				default:
					sender.sendMessage(StringUtility.format(wrongUsage));
					break;
				}
			}else { sender.sendMessage(StringUtility.format(noPermission)); }
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
	
	/*
	 * Ints returned are;
	 * 0 is neither have PvP disabled
	 * 1 both players have it disabled
	 * 2 playerGettingHurt has it disabled
	 * 3 playerCausingHarm has it disabled
	 * 4 playerCausingHarm is opped and bypassing is enabled.
	 */
	public int isPvPDisabledForPlayers(Player playerGettingHurt, Player playerCausingHarm) {
		UUID playerHurtUUID = playerGettingHurt.getUniqueId();
		UUID playerHarmUUID = playerCausingHarm.getUniqueId();
		if (!pvpDisabledPlayers.containsKey(playerHurtUUID) || (!pvpDisabledPlayers.containsKey(playerHarmUUID))) {
			if (!pvpDisabledPlayers.containsKey(playerHurtUUID)) {
				pvpDisabledPlayers.put(playerHurtUUID, DisabledByDefault);
			}
			if (!pvpDisabledPlayers.containsKey(playerHarmUUID)) {
				pvpDisabledPlayers.put(playerHarmUUID, DisabledByDefault);
			}
		}
		if (pvpDisabledPlayers.containsKey(playerHurtUUID) && (pvpDisabledPlayers.containsKey(playerHarmUUID))) {
			if ((pvpDisabledPlayers.get(playerHurtUUID) == true) && (pvpDisabledPlayers.get(playerHarmUUID) == true)){
				if ((Bukkit.getPlayer(playerHarmUUID).isOp() && (OPsToBypass))) {
					return 4;
				}
				return 1;
			}
			if ((pvpDisabledPlayers.get(playerHurtUUID)) || (pvpDisabledPlayers.get(playerHarmUUID))){
				if (pvpDisabledPlayers.get(playerHurtUUID)) {
					if ((Bukkit.getPlayer(playerHarmUUID).isOp() && (OPsToBypass))) {
						return 4;
					}
					return 2;
				}else if (pvpDisabledPlayers.get(playerHarmUUID)) {
					return 3;
				}
			}
		}
		return 0;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent  e) {
		debugging(
				"EntityDamagedEvent Called: " + "\n" +
				"Damage Amount: " + e.getDamage() + "\n" +
				"EventName: " + e.getEventName() + "\n" +
				"Entity: " + e.getEntity()  + "\n"
				);
		if ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player)) {
			debugging("#2");
			UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();
			UUID playerCausingHarm = Bukkit.getPlayer(e.getDamager().getName()).getUniqueId();
			
			int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(playerGettingHurt), Bukkit.getPlayer(playerCausingHarm));
			debugging("PvP Check:" + pvpCheck + "");
			debugging("SELECT * FROM pvpDisabledPlayers: " + "\n"+pvpDisabledPlayers.entrySet());
			switch(pvpCheck) {
			case 4:
				e.getDamager().sendMessage(StringUtility.format(OPUsingBypassedPvP)
						.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
						.replace("{PLAYERCAUSINGHARM}", Bukkit.getPlayer(playerCausingHarm).getName()));
				debugging("CASE 4: EntityDamaged - OPUsingBypassedPvP");
				e.setCancelled(false);
				break;
			case 1:
				e.getDamager().sendMessage(StringUtility.format(bothDisabled)
						.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
						.replace("{PLAYERCAUSINGHARM}", Bukkit.getPlayer(playerCausingHarm).getName()));
				debugging("CASE 1: EntityDamaged - BothDisabled");
				e.setCancelled(true);
				break;
			case 2:
				e.getDamager().sendMessage(StringUtility.format(playerGettingHurtDisabled)
						.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
						.replace("{PLAYERCAUSINGHARM}", Bukkit.getPlayer(playerCausingHarm).getName()));
				debugging("CASE 2: EntityDamaged - playerGettingHurtDisabled");
				e.setCancelled(true);
				break;
			case 3:
				e.getDamager().sendMessage(StringUtility.format(playerCausingHarmDisabled)
						.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
						.replace("{PLAYERCAUSINGHARM}", Bukkit.getPlayer(playerCausingHarm).getName()));
				debugging("CASE 3: EntityDamaged - playerCausingHarmDisabled");
				e.setCancelled(true);
				break;
			default:
					break;
			}
		
		}else if ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Arrow)) {
			final Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player playerCausingHarm = (Player)arrow.getShooter();
				UUID playerGettingHurt = Bukkit.getPlayer(e.getEntity().getName()).getUniqueId();
				
				int pvpCheck = isPvPDisabledForPlayers(Bukkit.getPlayer(playerGettingHurt), playerCausingHarm);
				switch(pvpCheck) {
				case 4:
					playerCausingHarm.sendMessage(StringUtility.format(OPUsingBypassedPvP)
							.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
							.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
					debugging("CASE 4: EntityDamaged - OPUsingBypassedPvP");
					e.setCancelled(false);
					break;
				case 1:
					playerCausingHarm.sendMessage(StringUtility.format(bothDisabled)
							.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
							.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
					debugging("CASE 1: EntityDamaged - BothDisabled");
					e.setCancelled(true);
					break;
				case 2:
					playerCausingHarm.sendMessage(StringUtility.format(playerGettingHurtDisabled)
							.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
							.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
					debugging("CASE 2: EntityDamaged - playerGettingHurtDisabled");
					e.setCancelled(true);
					break;
				case 3:
					playerCausingHarm.sendMessage(StringUtility.format(playerCausingHarmDisabled)
							.replace("{PLAYERGETTINGHURT}", Bukkit.getPlayer(playerGettingHurt).getName())
							.replace("{PLAYERCAUSINGHARM}", playerCausingHarm.getName()));
					debugging("CASE 3: EntityDamaged - playerCausingHarmDisabled");
					e.setCancelled(true);
					break;
				default:
						break;
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFishEvent(PlayerFishEvent e) {
		Player playerFishing = e.getPlayer();
		debugging(
				"EntityDamagedEvent Called: " + "\n" +
				"Player: " + e.getPlayer() + "\n" +
				"EventName: " + e.getEventName() + "\n" +
				"Caught: " + e.getCaught()  + "\n"
				);
		Entity caught = e.getCaught();
		if (caught == null) {
			//Do nothing lol.
		}else if (caught instanceof Player) {
			Player playerCaught = (Player)caught;
			int pvpCheck = isPvPDisabledForPlayers(playerCaught, playerFishing);
			switch(pvpCheck) {
			case 4:
				playerFishing.sendMessage(StringUtility.format(OPUsingBypassedPvP)
						.replace("{PLAYERGETTINGHURT}", playerCaught.getName())
						.replace("{PLAYERCAUSINGHARM}", playerFishing.getName()));
				debugging("CASE 4: EntityDamaged - OPUsingBypassedPvP");
				e.setCancelled(false);
				break;
			case 1:
				playerFishing.sendMessage(StringUtility.format(bothDisabled)
						.replace("{PLAYERGETTINGHURT}", playerCaught.getName())
						.replace("{PLAYERCAUSINGHARM}", playerFishing.getName()));
				debugging("CASE 1: EntityDamaged - BothDisabled");
				e.getHook().remove();
				e.setCancelled(true);
				break;
			case 2:
				playerFishing.sendMessage(StringUtility.format(playerGettingHurtDisabled)
						.replace("{PLAYERGETTINGHURT}", playerCaught.getName())
						.replace("{PLAYERCAUSINGHARM}", playerFishing.getName()));
				debugging("CASE 2: EntityDamaged - playerGettingHurtDisabled");
				e.getHook().remove();
				e.setCancelled(true);
				break;
			case 3:
				playerFishing.sendMessage(StringUtility.format(playerCausingHarmDisabled)
						.replace("{PLAYERGETTINGHURT}", playerCaught.getName())
						.replace("{PLAYERCAUSINGHARM}", playerFishing.getName()));
				debugging("CASE 3: EntityDamaged - playerCausingHarmDisabled");
				e.getHook().remove();
				e.setCancelled(true);
				break;
			default:
					break;
			}

		}

	}
	
	boolean debuggingEnabled = false;
	private void debugging(String debuglog) {
		if (debuggingEnabled == true) {
			Bukkit.getLogger().warning(debuglog);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!pvpDisabledPlayers.containsKey(p.getUniqueId())) {
			if (ConfigUtility.configContainsBoolean("Data." + p.getUniqueId())) {
				boolean b = ConfigUtility.configReadBoolean("Data." + p.getUniqueId());
				pvpDisabledPlayers.put(p.getUniqueId(), b);
			}else {
				pvpDisabledPlayers.put(p.getUniqueId(), DisabledByDefault);
			}
		}
	}

	public void readFromConfig() {
		/*
		 * Write defaults to configuration file.
		 */
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.NoPermission")) {
			ConfigUtility.populateConfig("DefaultMessages.NoPermission", "&7[&3PvPToggle&7] &3&oYou do not have the required permissions to perform this command.", "String");
		}
		noPermission = ConfigUtility.configReadString("DefaultMessages.NoPermission");
		
		if (!ConfigUtility.configContainsBoolean("DefaultMessages.WrongUsage")) {
			ConfigUtility.populateConfig("DefaultMessages.WrongUsage", "&7[&3PvPToggle&7] &3&oIncorrect usage, use '/pvptoggle' for help.", "String");
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
			ConfigUtility.populateConfig("DefaultMessages.BothPlayersPvPDisabled", "&7[&3PvPToggle&7] &3&oBoth you ({PLAYERCAUSINGHARM}) and {PLAYERGETTINGHURT} have PvP Disabled.", "String");
		}
		bothDisabled = ConfigUtility.configReadString("DefaultMessages.BothPlayersPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerGettingHurtPvPDisabled")) {
			ConfigUtility.populateConfig("DefaultMessages.PlayerGettingHurtPvPDisabled", "&7[&3PvPToggle&7] &3&o{PLAYERGETTINGHURT} has PvP Disabled.", "String");
		}
		playerGettingHurtDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerGettingHurtPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.PlayerCausingHarmPvPDisabled")) {
			ConfigUtility.populateConfig("DefaultMessages.PlayerCausingHarmPvPDisabled", "&7[&3PvPToggle&7] &3&oYour PvP is disabled. Change this by using /PvPToggle.", "String");
		}
		playerCausingHarmDisabled = ConfigUtility.configReadString("DefaultMessages.PlayerCausingHarmPvPDisabled");

		if (!ConfigUtility.configContainsBoolean("DefaultMessages.OPUsingBypassedPvP")) {
			ConfigUtility.populateConfig("DefaultMessages.OPUsingBypassedPvP", "&7[&3PvPToggle&7] &3&oSorry {PLAYERCAUSINGHARM}, you're currently attacking {PLAYERGETTINGHURT} whilst your PvP is disabled for yourself. Please use /PvPToggle to change this..", "String");
		}
		OPUsingBypassedPvP = ConfigUtility.configReadString("DefaultMessages.OPUsingBypassedPvP");

		
		ConfigUtility.saveConfiguration();
		/*
		 * We check the settings currently saved by players.
		 * Data.PlayerUUID <false/true>
		 */
		try{
			for (String i : getConfig().getConfigurationSection("Data").getKeys(false)) {
				try{
					boolean y = ConfigUtility.configReadBoolean("Data." + i);
					pvpDisabledPlayers.put(UUID.fromString(i), y);
				}catch(NullPointerException e) {
					pvpDisabledPlayers.put(UUID.fromString(i), false);
				}
			}
		}catch(NullPointerException e) {	}
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
