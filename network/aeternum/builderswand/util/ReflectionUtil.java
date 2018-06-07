package network.aeternum.builderswand.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;

public class ReflectionUtil {
	private static HashMap< String, Class<?> > classCache;
	private static HashMap< String, Method > methodCache;
	private static HashMap< Class< ? >, Constructor< ? > > constructorCache;
	private static Field connection;
	private static String version;
	
	static {
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		classCache = new HashMap< String, Class<?> >();
		try {
			classCache.put( "CraftPlayer", Class.forName( "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer" ) );
			classCache.put( "PlayerConnection", Class.forName( "net.minecraft.server." + version + "." + "PlayerConnection" ) );
			
			classCache.put( "CraftWorld", Class.forName( "org.bukkit.craftbukkit." + version + "." + "CraftWorld" ) );
			classCache.put( "CraftItemStack", Class.forName( "org.bukkit.craftbukkit." + version + ".inventory." + "CraftItemStack" ) );
			classCache.put( "ItemStack", Class.forName( "net.minecraft.server." + version + "." + "ItemStack" ) );
			
			classCache.put( "EntityArmorStand", Class.forName( "net.minecraft.server." + version + "." + "EntityArmorStand" ) );
			classCache.put( "EntityLiving", Class.forName( "net.minecraft.server." + version + "." + "EntityLiving" ) );
			classCache.put( "EntityPlayer", Class.forName( "net.minecraft.server." + version + "." + "EntityPlayer" ) );

			classCache.put( "Packet", Class.forName( "net.minecraft.server." + version + "." + "Packet" ) );
			classCache.put( "PacketPlayOutEntityDestroy", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutEntityDestroy" ) );
			classCache.put( "PacketPlayOutEntityEquipment", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutEntityEquipment" ) );
			classCache.put( "PacketPlayOutSpawnEntityLiving", Class.forName( "net.minecraft.server." + version + "." + "PacketPlayOutSpawnEntityLiving" ) );
			classCache.put( "World", Class.forName( "net.minecraft.server." + version + "." + "World" ) );
			
			classCache.put( "EnumItemSlot", Class.forName( "net.minecraft.server." + version + "." + "EnumItemSlot" ) );
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		methodCache = new HashMap< String, Method >();
		try {
			methodCache.put( "asNMSCopy", getNMSClass( "CraftItemStack" ).getMethod( "asNMSCopy", ItemStack.class ) );
			methodCache.put( "asBukkitCopy", getNMSClass( "CraftItemStack" ).getMethod( "asBukkitCopy", getNMSClass( "ItemStack" ) ) );
			
			methodCache.put( "getWorldHandle", getNMSClass( "CraftWorld" ).getMethod( "getHandle" ) );
			methodCache.put( "getHandle", getNMSClass( "CraftPlayer" ).getMethod( "getHandle" ) );
			methodCache.put( "sendPacket", getNMSClass( "PlayerConnection" ).getMethod( "sendPacket", getNMSClass( "Packet" ) ) );
			
			methodCache.put( "setLocation", getNMSClass( "EntityArmorStand" ).getMethod( "setLocation", double.class, double.class, double.class, float.class, float.class ) );
			methodCache.put( "setMarker", getNMSClass( "EntityArmorStand" ).getMethod( "setMarker", boolean.class ) );
			methodCache.put( "setSmall", getNMSClass( "EntityArmorStand" ).getMethod( "setSmall", boolean.class ) );
			methodCache.put( "setNoGravity", getNMSClass( "EntityArmorStand" ).getMethod( "setNoGravity", boolean.class ) );
			methodCache.put( "setInvisible", getNMSClass( "EntityArmorStand" ).getMethod( "setInvisible", boolean.class ) );
			methodCache.put( "setInvulnerable", getNMSClass( "EntityArmorStand" ).getMethod( "setInvulnerable", boolean.class ) );
			methodCache.put( "getId", getNMSClass( "EntityArmorStand" ).getMethod( "getId" ) );
			
			methodCache.put( "getEquipment", getNMSClass( "EntityArmorStand" ).getMethod( "getEquipment", getNMSClass( "EnumItemSlot" ) ) );
			methodCache.put( "valueOf", getNMSClass( "EnumItemSlot" ).getMethod( "valueOf", String.class ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		constructorCache = new HashMap< Class< ? >, Constructor< ? > >();
		try {
			constructorCache.put( getNMSClass( "EntityArmorStand" ), getNMSClass( "EntityArmorStand" ).getConstructor( getNMSClass( "World" ) ) );
			
			constructorCache.put( getNMSClass( "PacketPlayOutSpawnEntityLiving" ),  getNMSClass( "PacketPlayOutSpawnEntityLiving" ).getConstructor( getNMSClass( "EntityLiving" ) ) );
			constructorCache.put( getNMSClass( "PacketPlayOutEntityEquipment" ), getNMSClass( "PacketPlayOutEntityEquipment" ).getConstructor( int.class, getNMSClass( "EnumItemSlot" ), getNMSClass( "ItemStack" ) ) );
			constructorCache.put( getNMSClass( "PacketPlayOutEntityDestroy" ), getNMSClass( "PacketPlayOutEntityDestroy" ).getConstructor( int[].class ) );
			
			connection = getNMSClass( "EntityPlayer" ).getField( "playerConnection" );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public static Method getMethod( String name ) {
		return methodCache.containsKey( name ) ? methodCache.get( name ) : null;
	}

	public static Constructor< ? > getConstructor( Class< ? > clazz ) {
		return constructorCache.containsKey( clazz ) ? constructorCache.get( clazz ) : null;
	}

	public static Class<?> getNMSClass(String name) {
		if ( classCache.containsKey( name ) ) {
			return classCache.get( name );
		}

		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Field getField() {
		return connection;
	}
}
