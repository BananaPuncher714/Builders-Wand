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
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.bananapuncher714.builderswand.util.BlockUtil;
import net.minecraft.server.v1_11_R1.EntityArmorStand;
import net.minecraft.server.v1_11_R1.EnumItemSlot;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_11_R1.WorldServer;

public class PreviewShower {
	private static Map< UUID, Map< Location, EntityArmorStand > > entities = new HashMap< UUID, Map< Location, EntityArmorStand > >();

	public PreviewShower( Plugin plugin ) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask( plugin, this::showPreview, 0, 1 );
	}

	private void showPreview() {
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
		Map< Location, EntityArmorStand > map = entities.get( p.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, EntityArmorStand >();
			entities.put( p.getUniqueId(), map );
		}
		if ( map.containsKey( loc ) ) {
			return;
		}

		WorldServer s = ((CraftWorld)loc.getWorld()).getHandle();
		EntityArmorStand stand = new EntityArmorStand(s);

		stand.setLocation( loc.getX() + .5, loc.getY() - .5, loc.getZ() + .5, 0, 0 );
		stand.setMarker( true );
		stand.setSmall( true );
		stand.setNoGravity( true );
		stand.setInvisible( true );
		stand.setInvulnerable( true );
		
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving( stand );
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);

		map.put( loc, stand );
	}

	private static void killAllBut( Collection< Location > locations, Player player, Material mat, byte data ) {
		Map< Location, EntityArmorStand > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, EntityArmorStand >();
			entities.put( player.getUniqueId(), map );
			return;
		}
		for ( Iterator< Entry< Location, EntityArmorStand> > it = map.entrySet().iterator(); it.hasNext(); ) {
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
		Map< Location, EntityArmorStand > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			map = new HashMap< Location, EntityArmorStand >();
			entities.put( player.getUniqueId(), map );
		}
		if ( !map.containsKey( location ) ) {
			return;
		}
		EntityArmorStand stand = map.get( location );
		ItemStack item = CraftItemStack.asBukkitCopy( stand.getEquipment( EnumItemSlot.HEAD ) );
		if ( item.getType() != material || item.getDurability() != data ) {
			PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment( stand.getId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy( new ItemStack( material, 1, data ) ) );
			((CraftPlayer)player).getHandle().playerConnection.sendPacket(equipment);
		}
	}
	
	private static void kill( Location location, Player player ) {
		Map< Location, EntityArmorStand > map = entities.get( player.getUniqueId() );
		if ( map == null ) {
			return;
		}
		if ( !map.containsKey( location ) ) {
			return;
		}

		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy( map.get( location ).getId() );
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}
}
