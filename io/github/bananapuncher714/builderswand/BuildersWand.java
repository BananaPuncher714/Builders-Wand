package io.github.bananapuncher714.builderswand;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.builderswand.util.NBTEditor;

public class BuildersWand extends JavaPlugin {
	public static final int DEFAULT_RANGE = 7;
	
	private PreviewShower shower;
	private Set< Material > replaceables = new HashSet< Material >();
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadConfig();
		
		Bukkit.getPluginManager().registerEvents( new PlayerListener( this ), this );
		shower = new PreviewShower( this );
	}
	
	@Override
	public void onDisable() {
		shower.disable();
	}
	
	private void loadConfig() {
		FileConfiguration config = getConfig();
		for ( String string : config.getStringList( "can-replace" ) ) {
			Material mat = Material.getMaterial( string.toUpperCase() );
			if ( mat == null ) {
				getLogger().info( "Invalid MATERIAL found in config! (" + string + ")" );
				continue;
			}
			replaceables.add( mat );
		}
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( !( sender instanceof Player ) ) {
			sender.sendMessage( "You must be a player to receive a wand!" );
			return false;
		}
		Player player = ( Player ) sender;
		int size = 9;
		int range = DEFAULT_RANGE;
		if ( args.length > 0 ) {
			try {
				size = Math.max( 1, Integer.parseInt( args[ 0 ] ) );
			} catch ( Exception exception ) {
				size = 9;
			}
		}
		if ( args.length > 1 ) {
			try {
				range = Math.max( 1, Integer.parseInt( args[ 1 ] ) );
			} catch ( Exception exception ) {
			}
		}
		
		ItemStack item = new ItemStack( Material.STICK );
		
		player.getInventory().addItem( getBuildersWand( item, size, range ) );
		player.sendMessage( "Recieved builders wand of (" + size + ") size and (" + range + ") range!" );
		
		return true;
	}
	
	public Set< Material > getReplaceables() {
		return replaceables;
	}

	public static ItemStack getBuildersWand( ItemStack item, int buildSize ) {
		return getBuildersWand( item, buildSize, DEFAULT_RANGE );
	}
	
	public static ItemStack getBuildersWand( ItemStack item, int buildSize, int range ) {
		item = NBTEditor.setItemTag( item, buildSize, "builderswand", "wand-size" );
		return NBTEditor.setItemTag( item, range, "builderswand", "wand-range" );
	}
	
	public static int getBuildSize( ItemStack item ) {
		if ( item == null || NBTEditor.getItemTag( item, "builderswand", "wand-size" ) == null ) {
			return 0;
		}
		return ( int ) NBTEditor.getItemTag( item, "builderswand", "wand-size" );
	}
	
	public static int getRange( ItemStack item ) {
		if ( item == null || NBTEditor.getItemTag( item, "builderswand", "wand-range" ) == null ) {
			return 0;
		}
		return ( int ) NBTEditor.getItemTag( item, "builderswand", "wand-range" );
	}
}
