package io.github.bananapuncher714.builderswand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.builderswand.util.NBTEditor;

public class BuildersWand extends JavaPlugin {
	private PreviewShower shower;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( new PlayerListener(), this );
		shower = new PreviewShower( this );
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( !( sender instanceof Player ) ) {
			sender.sendMessage( "You must be a player to receive a wand!" );
			return false;
		}
		Player player = ( Player ) sender;
		int size = 9;
		if ( args.length > 0 ) {
			try {
				size = Math.max( 1, Integer.parseInt( args[ 0 ] ) );
			} catch ( Exception exception ) {
				size = 9;
			}
		}
		
		ItemStack item = new ItemStack( Material.STICK );
		
		player.getInventory().addItem( getBuildersWand( item, size ) );
		player.sendMessage( "Recieved builders wand of (" + size + ") size!" );
		
		return true;
	}

	public static ItemStack getBuildersWand( ItemStack item, int buildSize ) {
		return NBTEditor.setItemTag( item, buildSize, "builderswand", "wand-size" );
	}
	
	public static int getBuildSize( ItemStack item ) {
		if ( item == null || NBTEditor.getItemTag( item, "builderswand", "wand-size" ) == null ) {
			return 0;
		}
		return ( int ) NBTEditor.getItemTag( item, "builderswand", "wand-size" );
	}
}
