package io.github.bananapuncher714.builderswand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import io.github.bananapuncher714.builderswand.util.BlockUtil;

public class PlayerListener implements Listener {
	BuildersWand plugin;
	
	public PlayerListener( BuildersWand plugin ) {
		this.plugin = plugin;
	}
	
	@EventHandler( ignoreCancelled = true, priority = EventPriority.HIGHEST )
	public void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( event.getAction() != Action.RIGHT_CLICK_BLOCK ) {
			return;
		}
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if ( !player.hasPermission( "builderswand.admin" ) && !player.hasPermission( "builderswand.use" ) ) {
			return;
		}
		ItemStack item = player.getItemInHand();
		if ( item == null || item.getType() == Material.AIR ) {
			return;
		}
		int size = BuildersWand.getBuildSize( item );
		int range = BuildersWand.getRange( item );
		if ( size == 0 || range == 0 ) {
			return;
		}
		Block block = event.getClickedBlock();
		if ( block == null || block.getType() == Material.AIR ) {
			return;
		}
		
		BlockFace face = BlockUtil.getBlockFace( player, plugin.getReplaceables() );
		if ( face == null ) {
			face = event.getBlockFace();
		}
		Material blockType = block.getType();
		
		ItemStack cost = new ItemStack( blockType, 1, block.getData() );
		for ( Location location : PreviewShower.getValidLocations( block.getLocation().add( face.getModX(), face.getModY(), face.getModZ() ), face, plugin.getReplaceables(), blockType, block.getData(), size, range ) ) {
			if ( player.getLocation().getBlock().getLocation().equals( location ) || player.getLocation().add( 0, 1, 0 ).getBlock().getLocation().equals( location ) ) {
				continue;
			}
			if ( !plugin.getReplaceables().contains( location.getBlock().getType() ) ) {
				continue;
			}
			boolean hasItem = true;
			if ( player.getGameMode() == GameMode.CREATIVE || player.getInventory().containsAtLeast( cost, 1 ) ) { 
				hasItem = player.getGameMode() != GameMode.CREATIVE;
			} else {
				continue;
			}
			BlockState state = location.getBlock().getState();
			state.setType( blockType );
			state.setRawData( block.getData() );
			BlockPlaceEvent buildEvent = new BlockPlaceEvent( location.getBlock(), state, location.getBlock().getRelative( face.getOppositeFace() ), cost, player, true, EquipmentSlot.HAND );
			Bukkit.getPluginManager().callEvent( buildEvent );
			if ( buildEvent.isCancelled() ) {
				continue;
			}
			if ( hasItem ) {
				player.getInventory().removeItem( cost );
			}
			state.update( true );
		}
	}

}
