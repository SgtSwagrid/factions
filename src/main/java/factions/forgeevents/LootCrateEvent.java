package factions.forgeevents;

import java.util.List;
import java.util.Random;

import factions.util.Colour;
import factions.util.Messenger;
import factions.util.Teams;
import factions.util.Values;
import factions.util.WorldUtils;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class LootCrateEvent {
	
	public enum Tier {
		
		COMMON("Common", Colour.WHITE),
		RARE("Rare", Colour.BLUE),
		LEGENDARY("Legendary", Colour.ORANGE);
		
		public final String NAME;
		public final Colour COLOUR;
		
		Tier(String name, Colour colour) {
			NAME = name;
			COLOUR = colour;
		}
	}
	
	private static final int MAX_DISTANCE = 800;
	private static final float FREQUENCY = 24.0F; //per day
	private static final int COUNTDOWN = 15; //mins
	private static final int INACCURACY = 50;
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event) {
		
		if(Values.getBoolean("loot_crate_enabled")) {
			if(!Values.getBoolean("loot_crate_active")) {
				if(Math.random() < FREQUENCY / (20 * 60 * 60 * 24)) {
					countdown();
				}
			
			} else {
				if(getTimestamp() >= Values.getInteger("loot_crate_time")) {
					Tier tier = Values.getEnum("loot_crate_tier", Tier.class);
					trigger(Values.getInteger("loot_crate_x") + (int) (INACCURACY * Math.random()),
							Values.getInteger("loot_crate_z") + (int) (INACCURACY * Math.random()), tier);
					Values.setBoolean("loot_crate_active", false);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		
		if(Values.getBoolean("loot_crate_active")) {
			
			Tier tier = Values.getEnum("loot_crate_tier", Tier.class);
			
			Messenger.tellPlayer(event.player, TextFormatting.YELLOW + "A " + tier.COLOUR.FORMATTER + tier.NAME
					+ " Loot Crate" + TextFormatting.YELLOW + " is scheduled to land within "
					+ INACCURACY + " blocks of [x:" + Values.getInteger("loot_crate_x") + ", z:"
					+ Values.getInteger("loot_crate_z") + "]" + TextFormatting.YELLOW + " in "
					+ getTimeToDrop() + " minute(s)." + TextFormatting.RESET);
		}
	}
	
	private static void countdown() {
		
		double random = Math.random();
		
		Tier tier;
		
		if(random < 0.65F) tier = Tier.COMMON;
		else if(random < 0.9F) tier = Tier.RARE;
		else tier = Tier.LEGENDARY;
		
		int x = (int) (MAX_DISTANCE * 2 * Math.random() - MAX_DISTANCE);
		int z = (int) (MAX_DISTANCE * 2 * Math.random() - MAX_DISTANCE);
		
		Messenger.announce(TextFormatting.YELLOW + "A " + tier.COLOUR.FORMATTER + tier.NAME
				+ " Loot Crate" + TextFormatting.YELLOW + " is scheduled to land within " + INACCURACY
				+ " blocks of [x:" + x + ", z:" + z + "]" + TextFormatting.YELLOW + " in " + COUNTDOWN + " minutes." + TextFormatting.RESET);
		
		Values.setInteger("loot_crate_x", x);
		Values.setInteger("loot_crate_z", z);
		Values.setInteger("loot_crate_time", (int) (getTimestamp() + COUNTDOWN));
		Values.setEnum("loot_crate_tier", tier);
		Values.setBoolean("loot_crate_active", true);
	}
	
	public static void trigger(int x, int z, Tier tier) {
		
		World world = DimensionManager.getWorld(0);
		
		int y = WorldUtils.getHeight(world, x, z);
		
		world.createExplosion(null,x, y, z, 10, true);
		world.spawnEntity(new EntityLightningBolt(world, x, y + 45, z, false));
		
		y = WorldUtils.getHeight(world, x, z);
		
		Values.setBoolean("loot_crate_opened", false);
		Values.setPosition("last_loot_crate", new BlockPos(x, y, z));
		
		world.setBlockState(new BlockPos(x, y, z), Blocks.CHEST.getDefaultState());
		
		LootTable table = DimensionManager.getWorld(0).getLootTableManager().getLootTableFromLocation(new ResourceLocation("factions:" + tier.NAME.toLowerCase()));
		LootContext context = new LootContext.Builder(DimensionManager.getWorld(0)).build();
		TileEntityChest container = (TileEntityChest) world.getTileEntity(new BlockPos(x, y, z));
		
		//WIP START
		List<ItemStack> items = table.generateLootForPools(new Random(), context);
		IItemHandler itemHandler = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		for(ItemStack item : items) {
			for(int i = 0; i < itemHandler.getSlots(); i++) {
				//if(container.isItemValidForSlot(i, item)) {
					item = itemHandler.insertItem(i, item, false);
					if(item.isEmpty()) break;
				//}
			}
		}
		//WIP END
		
		table.fillInventory(container, new Random(), context);
		
		
		Messenger.announce(TextFormatting.YELLOW + "The loot crate has arrived at [x:" + x + ", z:" + z + "]"
				+ TextFormatting.YELLOW + "." + TextFormatting.RESET);
	}
	
	@SubscribeEvent
	public void onChestOpened(RightClickBlock event) {
		if(event.getPos().equals(Values.getPosition("last_loot_crate"))) {
			if(!event.getEntityPlayer().isSneaking()) {
				TileEntity t = event.getWorld().getTileEntity(event.getPos());
				if(t instanceof TileEntityChest) {
					if(!Values.getBoolean("loot_crate_opened")) {
						EntityPlayer player = event.getEntityPlayer();
						Teams.givePoints(player.getTeam(), 3);
						Messenger.announce(player.getTeam().getColor() + player.getName() + TextFormatting.YELLOW + " has claimed the Loot Crate." + TextFormatting.RESET);
						Values.setBoolean("loot_crate_opened", true);
					}
				}
			}
		}
	}
	
	private static int getTimeToDrop() {
		int currentTime = getTimestamp();
		int dropTime = Values.getInteger("loot_crate_time");
		return dropTime - currentTime;
	}
	
	private static int getTimestamp() {
		return (int) (System.currentTimeMillis() / 60000L);
	}
}