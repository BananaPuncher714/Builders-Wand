package io.github.bananapuncher714.builderswand;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bananapuncher714.builderswand.util.BlockUtil;
import io.github.bananapuncher714.builderswand.util.ReflectionUtil;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;

public class PreviewShower extends BukkitRunnable{
	private static Map< UUID, Map< Location, Object > > entities = new HashMap< UUID, Map< Location, Object > >();

	public PreviewShower( Plugin plugin ) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask( plugin, this, 0, 1 );
	}

	@Override
	public void run() {
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			ItemStack item = player.getItemInHand();
			if ( item == null || item.getType() == Material.AIR ) {
				killAllBut( null, player, null, ( byte ) 0 );
				continue;
			}
			Block block = player.getTargetBlock( ( HashSet< Byte > ) null, 5 );
			if ( block == null || block.getType() == Material.AIR ) {
				killAllBut( null, player, null, ( byte ) 0 );
				continue;
			}
			int size = BuildersWand.getBuildSize( item );
			if ( size == 0 ) {
				killAllBut( null, player, null, ( byte ) 0 );
				continue;
			}
			BlockFace face = BlockUtil.getBlockFace( player );
			if ( face == null ) {
				killAllBut( null, player, null, ( byte ) 0 );
				continue;
			}
			Material blockType = block.getType();

			killAllBut( getValidLocations( block.getLocation().add( face.getModX(), face.getModY(), face.getModZ() ), face, blockType, block.getData(), size, 7 ), player, blockType, block.getData() );
		}
	}

	public void disable() {
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			killAllBut( null, player, null, ( byte ) 0 );
		}
	}

	public static List< Location > getValidLocations( Location loc, BlockFace face, Material material, byte data, int max, int radius ) {
		List< Location > locations = new ArrayList< Location >();

		int maxDistance = radius * radius;
		boolean isX = face.getModX() != 0, isY = face.getModY() != 0, isZ = face.getModZ() != 0;
		locations.add( loc );

		Set< Location > checked = new HashSet< Location >();
		Queue< Location > checking = new ArrayDeque< Location >();

		checking.add( loc.clone() );
		while ( locations.size() < max ) {
			Location check = checking.poll();
			checked.add( check );
			for ( int x = -1; x < 2; x++ ) {
				for ( int z = -1; z < 2; z++ ) {
					if ( x == 0 && z == 0 ) {
						continue;
					}
					Location clone = check.clone();
					if ( isX ) {
						clone.add( 0, x, z );
					} else if ( isY ) {
						clone.add( x, 0, z );
					} else if ( isZ ) {
						clone.add( x, z, 0 );
					}
					if ( checked.contains( clone ) ) {
						continue;
					} else {
						checked.add( clone );
					}
					if ( loc.distanceSquared( clone ) > maxDistance ) {
						continue;
					}

					Block cloneBlock = clone.getBlock();
					Block relative = cloneBlock.getRelative( face.getOppositeFace() );
					if ( cloneBlock.getType() != Material.AIR || relative.getType() != material || relative.getData() != data ) {
						continue;
					}

					if ( locations.size() >= max ) {
						break;
					}

					locations.add( clone );
					checking.add( clone );
					checked.add( clone );
				}
			}
			if ( checking.isEmpty() ) {
				break;
			}
		}

		return locations;
	}

	private static void spawn( Location loc, Player p ) {
		Map< Location, Object > map = entities.get( p.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, Object >();
			entities.put( p.getUniqueId(), map );
		}
		if ( map.containsKey( loc ) ) {
			return;
		}

		try {
			Object worldServer = ReflectionUtil.getMethod( "getWorldHandle" ).invoke( loc.getWorld() );
			Object armorStand = ReflectionUtil.getConstructor( ReflectionUtil.getNMSClass( "EntityArmorStand" ) ).newInstance( worldServer );

			ReflectionUtil.getMethod( "setLocation").invoke( armorStand, loc.getX() + .5, loc.getY() - .5, loc.getZ() + .5, 0f, 0f );
			ReflectionUtil.getMethod( "setMarker").invoke( armorStand, true );
			ReflectionUtil.getMethod( "setSmall").invoke( armorStand, true );
			ReflectionUtil.getMethod( "setNoGravity").invoke( armorStand, true );
			ReflectionUtil.getMethod( "setInvisible").invoke( armorStand, true );
			ReflectionUtil.getMethod( "setInvulnerable").invoke( armorStand, true );

			Object packet = ReflectionUtil.getConstructor( ReflectionUtil.getNMSClass( "PacketPlayOutSpawnEntityLiving" ) ).newInstance( armorStand );

			Object playerConnection = ReflectionUtil.getField().get( ReflectionUtil.getMethod( "getHandle" ).invoke( p ) );
			ReflectionUtil.getMethod( "sendPacket" ).invoke( playerConnection, packet );

			map.put( loc, armorStand );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private static void killAllBut( Collection< Location > locations, Player player, Material mat, byte data ) {
		Map< Location, Object > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, Object >();
			entities.put( player.getUniqueId(), map );
			return;
		}
		for ( Iterator< Entry< Location, Object> > it = map.entrySet().iterator(); it.hasNext(); ) {
			Location location = it.next().getKey();
			if ( locations == null || !locations.contains( location ) ) {
				kill( location, player );
				it.remove();
			}
		}
		if ( locations != null ) {
			for ( Location location : locations ) {
				spawn( location, player );
				update( location, player, mat, data );
			}
		}
	}

	private static void update( Location location, Player player, Material material, byte data ) {
		Map< Location, Object > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, Object >();
			entities.put( player.getUniqueId(), map );
		}
		if ( !map.containsKey( location ) ) {
			return;
		}
		try {
			Object stand = map.get( location );
			ItemStack item = ( ItemStack ) ReflectionUtil.getMethod( "asBukkitCopy" ).invoke( null, ReflectionUtil.getMethod( "getEquipment" ).invoke( stand, ReflectionUtil.getMethod( "valueOf" ).invoke( null, "HEAD" ) ) );
			if ( item.getType() != material || item.getDurability() != data ) {

				Object packet = ReflectionUtil.getConstructor( ReflectionUtil.getNMSClass( "PacketPlayOutEntityEquipment" ) ).newInstance( ReflectionUtil.getMethod( "getId" ).invoke( stand ), ReflectionUtil.getMethod( "valueOf" ).invoke( null, "HEAD" ), ReflectionUtil.getMethod( "asNMSCopy" ).invoke( null, new ItemStack( material, 1, data ) ) );
				Object playerConnection = ReflectionUtil.getField().get( ReflectionUtil.getMethod( "getHandle" ).invoke( player ) );
				ReflectionUtil.getMethod( "sendPacket" ).invoke( playerConnection, packet );
			}
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private static void kill( Location location, Player player ) {
		Map< Location, Object > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			return;
		}
		if ( !map.containsKey( location ) ) {
			return;
		}

		try {
			Object packet = ReflectionUtil.getConstructor( ReflectionUtil.getNMSClass( "PacketPlayOutEntityDestroy" ) ).newInstance( new int[] { ( int ) ReflectionUtil.getMethod( "getId" ).invoke( map.get( location ) ) } );
			Object playerConnection = ReflectionUtil.getField().get( ReflectionUtil.getMethod( "getHandle" ).invoke( player ) );
			ReflectionUtil.getMethod( "sendPacket" ).invoke( playerConnection, packet );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
}
