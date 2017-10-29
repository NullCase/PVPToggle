package uk.co.ElmHoe;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ElmHoe.Bukkit.ConfigUtility;

public class Main extends JavaPlugin implements Listener{

	public void onEnable() {
		ConfigUtility.firstRun(this);
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		
	}
}
