package io.github.bananapuncher714.builderswand;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.builderswand.util.BlockUtil;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( event.getAction() != Action.RIGHT_CLICK_BLOCK ) {
			return;
		}
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if ( item == null || item.getType() == Material.AIR ) {
			return;
		}
		int size = BuildersWand.getBuildSize( item );
		if ( size == 0 ) {
			return;
		}
		
		Block block = event.getClickedBlock();
		if ( block == null || block.getType() == Material.AIR ) {
			return;
		}
		
		BlockFace face = BlockUtil.getBlockFace( player );
		if ( face == null ) {
			face = event.getBlockFace();
		}
		Material blockType = block.getType();
		
		ItemStack cost = new ItemStack( blockType, 1, block.getData() );
		for ( Location location : PreviewShower.getValidLocations( block.getLocation().add( face.getModX(), face.getModY(), face.getModZ() ), face, blockType, block.getData(), size, 7 ) ) {
			if ( player.getLocation().getBlock().getLocation().equals( location ) || player.getLocation().add( 0, 1, 0 ).getBlock().getLocation().equals( location ) ) {
				continue;
			}
			if ( location.getBlock().getType() != Material.AIR ) {
				continue;
			}
			if ( player.getGameMode() == GameMode.CREATIVE || player.getInventory().containsAtLeast( cost, 1 ) ) { 
				if ( player.getGameMode() != GameMode.CREATIVE ) {
					player.getInventory().removeItem( cost );
				}
			} else {
				continue;
			}
			location.getBlock().setType( blockType );
			location.getBlock().setData( block.getData() );
		}
	}

}
