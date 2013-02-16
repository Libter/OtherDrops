package com.gmail.zariust.otherdrops;

import static com.gmail.zariust.common.Verbosity.EXTREME;
import static com.gmail.zariust.common.Verbosity.HIGH;
import static com.gmail.zariust.common.Verbosity.HIGHEST;

import java.io.IOException;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

import me.drakespirit.plugins.moneydrop.MoneyDrop;
import me.taylorkelly.bigbrother.BigBrother;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.metrics.Metrics;
import com.nijiko.permissions.PermissionHandler;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class Dependencies {
	// Plugin Dependencies
	public static LogBlock logBlock = null;
	public static Consumer lbconsumer = null; 						// for LogBlock support
	public static BigBrother bigBrother = null;						// for BigBrother support
	public static CoreProtectAPI coreProtect = null;				    // for CoreProtect support
	public static PermissionHandler yetiPermissionsHandler = null;	// for Permissions support
	public static WorldGuardPlugin worldGuard = null;			// for WorldGuard support
	public static HawkEye hawkEye = null;
	public boolean usingHawkEye = false; 							// for HawkEye support
	boolean enabled;
	public static MobArena mobArena = null;
	public static MobArenaHandler mobArenaHandler = null;			// for MobArena
	public static MoneyDrop moneyDrop = null;						// for MoneyDrop

	public static Economy vaultEcon = null;
	public static Permission vaultPerms = null;

	static String foundPlugins = "";
	static String notFoundPlugins = "";
			

	public static void init() {
		try {
			enableMetrics();
			worldGuard = (WorldGuardPlugin)getPlugin("WorldGuard");
			logBlock = (LogBlock) getPlugin("LogBlock");
			bigBrother = (BigBrother)getPlugin("BigBrother");
			coreProtect = loadCoreProtect();
			hawkEye = (HawkEye)getPlugin("HawkEye");
			mobArena = (MobArena)getPlugin("MobArena");
			moneyDrop = (MoneyDrop)getPlugin("MoneyDrop");

			setupVault();


			if (coreProtect!=null){ //Ensure we have access to the API
				foundPlugins += ", CoreProtect";
				//coreProtect.testAPI(); //Will print out "[CoreProtect] API Test Successful." in the console.
			}

			if (logBlock != null) {
				lbconsumer = ((LogBlock)logBlock).getConsumer();
			}

			if (mobArena != null) {
				mobArenaHandler = new MobArenaHandler();
			}

			if (!foundPlugins.isEmpty()) Log.logInfo("Found plugin(s): '"+foundPlugins+"'", Verbosity.NORMAL);
			if (!notFoundPlugins.isEmpty()) Log.logInfo("Plugin(s) not found: '"+notFoundPlugins+"' (OtherDrops will continue to load)", Verbosity.HIGH);
		} catch (Exception e) {
			Log.logInfo("Failed to load one or more optional dependencies - continuing OtherDrops startup.");
			e.printStackTrace();
		}
	}

	public static Plugin getPlugin(String name) {
		Plugin plugin = OtherDrops.plugin.getServer().getPluginManager().getPlugin(name);

		if (plugin == null) {
			if (notFoundPlugins.isEmpty()) notFoundPlugins += name;
			else notFoundPlugins += ", " + name;
		} else {
			if (foundPlugins.isEmpty()) foundPlugins += name;
			else foundPlugins += ", " + name;
		}

		return plugin;
	}

	private static CoreProtectAPI loadCoreProtect() {
		Plugin plugin = OtherDrops.plugin.getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
			return null;
		}

		// Check that a compatible version of CoreProtect is loaded
		if (Double.parseDouble(plugin.getDescription().getVersion()) < 1.6){
			return null;
		}

		// Check that the API is enabled
		CoreProtectAPI CoreProtect = ((CoreProtect)plugin).getAPI();
		if (CoreProtect.isEnabled()==false){
			return null;
		}

		return CoreProtect;
	}

	public static boolean hasPermission(Permissible who, String permission) {
		if (who instanceof ConsoleCommandSender) return true;
		boolean perm = who.hasPermission(permission);
		if (!perm) {
			Log.logInfo("SuperPerms - permission ("+permission+") denied for "+who.toString(),HIGHEST);
		} else {
			Log.logInfo("SuperPerms - permission ("+permission+") allowed for "+who.toString(),HIGHEST);
		}
		return perm;
	}

	private static void setupVault() {
		if (OtherDrops.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			vaultEcon = null;
			Log.logInfo("Couldn't load Vault.",EXTREME); // Vault's not essential so no need to worry.
			return;
		}
		Log.logInfo("Hooked into Vault.",HIGH);
		RegisteredServiceProvider<Economy> rsp = OtherDrops.plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			vaultEcon = null;
			Log.logWarning("...couldn't hook into Vault economy module.",Verbosity.NORMAL);
			return;
		}
		vaultEcon = rsp.getProvider();

		//   RegistereredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		//   chat = rsp.getProvider();
		//	return chat != null;

		RegisteredServiceProvider<Permission> rsp_perms = OtherDrops.plugin.getServer().getServicesManager().getRegistration(Permission.class);
		if (rsp_perms == null) {
			vaultPerms = null;
			Log.logWarning("...couldn't hook into Vault permissions module.",Verbosity.NORMAL);
			return;
		}
		vaultPerms = rsp_perms.getProvider();
	}


	public static void enableMetrics()
	{
		try {
			Metrics metrics = new Metrics(OtherDrops.plugin);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}
	
	
	// If logblock plugin is available, inform it of the block destruction before we change it
	public static boolean queueBlockBreak(String playerName, Block block)
	{
		if (block == null) {
			Log.logWarning("Queueblockbreak: block is null - this shouldn't happen (please advise developer).  Player = "+playerName, HIGH);			
			return false;
		}
		
		String message = playerName+"-broke-"+block.getType().toString();
		
		if (Dependencies.hasBigBrother()) {
			// Block Breakage
			Log.logInfo("Attempting to log to BigBrother: "+message, HIGHEST);
			bigBrother.onBlockBroken(playerName, block, block.getWorld().getName());
		}
		
		if (Dependencies.hasLogBlock()) {
			BlockState before = block.getState();
			Log.logInfo("Attempting to log to LogBlock: "+message, HIGHEST);
			lbconsumer.queueBlockBreak(playerName, before);
		}
		
		if (Dependencies.hasCoreProtect()) {
			Log.logInfo("Attempting to log to CoreProtect: "+message, HIGHEST);
		  Dependencies.getCoreProtect().logRemoval(playerName, block.getLocation(), block.getTypeId(), block.getData());
		}
		
		if (Dependencies.hasHawkEye()) {
			Log.logInfo("Attempting to log to HawkEye: "+message, HIGHEST);
			
			// FIXME: Causes class not found since I'm using "new BlockEntry(...)" - need to stick to API methods?
//			boolean result = HawkEyeAPI.addEntry(plugin, new BlockEntry(playerName, DataType.BLOCK_BREAK, block));

			boolean result = HawkEyeAPI.addCustomEntry(OtherDrops.plugin, "ODBlockBreak", OtherDrops.plugin.getServer().getPlayer(playerName), block.getLocation(), block.getType().toString());
			if (!result) Log.logWarning("Warning: HawkEyeAPI logging failed.", Verbosity.HIGH);
		}
		return true;
	}

	private static boolean hasHawkEye() {
		return Dependencies.hawkEye != null;
	}

	private static boolean hasLogBlock() {
		return Dependencies.logBlock != null;
	}

	private static boolean hasBigBrother() {
		return Dependencies.bigBrother != null;
	}

	public static boolean hasMobArena() {
		return Dependencies.mobArena != null;
	}

	public static MobArenaHandler getMobArenaHandler() {
		return Dependencies.mobArenaHandler;
	}

	public static boolean hasWorldGuard() {
		return Dependencies.worldGuard != null;
	}

	public static WorldGuardPlugin getWorldGuard() {
		return Dependencies.worldGuard;
	}

	public static boolean hasVaultEcon() {
		return Dependencies.vaultEcon != null;
	}

	public static Economy getVaultEcon() {
		return Dependencies.vaultEcon;
	}

	public static boolean hasMoneyDrop() {
		return Dependencies.moneyDrop != null;
	}

	public static MoneyDrop getMoneyDrop() {
		return Dependencies.moneyDrop;
	}
	
	public static boolean hasCoreProtect() {
		return Dependencies.coreProtect != null;
	}

	public static CoreProtectAPI getCoreProtect() {
		return Dependencies.coreProtect;
	}

}