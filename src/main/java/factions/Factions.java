package factions;

import factions.commands.FactionsCommand;
import factions.events.flags.Flag;
import factions.events.flags.FlagsEvent;
import factions.forgeevents.LootCrateEvent;
import factions.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * 
 * @author Alec
 */
@Mod(modid = Factions.MODID, version = Factions.VERSION)
public class Factions {
	
	/**  */
	public static final String MODID = "factions";
	
	/**  */
	public static final String VERSION = "1.12.2-0.3.1";
	
	/**  */
	@SidedProxy(clientSide = "factions.proxy.ClientProxy",
			    serverSide = "factions.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	/**  */
	@Mod.Instance
	public static Factions instance;
	
	/**  */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Flag.init();
		proxy.preInit(event);
	}
	
	/**  */
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}
	
	/**  */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		MinecraftForge.EVENT_BUS.register(new FlagsEvent());
		if(event.getSide().isServer()) {
		}
		proxy.postInit(event);
	}
	
	/**  */
	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new FactionsCommand());
	}
}