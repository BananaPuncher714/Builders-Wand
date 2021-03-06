package network.aeternum.builderswand.util;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BlockUtil {
	/**
	* Gets the BlockFace of the block the player is currently targeting.
	*
	* @param the player who's targeted blocks BlockFace is to be checked.
	* @return the BlockFace of the targeted block, or null if the targeted block is non-occluding.
	*/
	public static BlockFace getBlockFace( Player player, Set< Material > blocks ) {
	    List< Block > lastTwoTargetBlocks = player.getLastTwoTargetBlocks( blocks, 100);
	    if ( lastTwoTargetBlocks.size() != 2 ) { //|| !lastTwoTargetBlocks.get( 1 ).getType().isOccluding() ) {
	    	return null;
	    }
	    Block targetBlock = lastTwoTargetBlocks.get( 1 );
	    Block adjacentBlock = lastTwoTargetBlocks.get( 0 );
	    return targetBlock.getFace( adjacentBlock );
	}
}
