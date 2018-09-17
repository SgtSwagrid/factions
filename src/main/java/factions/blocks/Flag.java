package factions.blocks;

import java.util.HashMap;
import java.util.Map;

import factions.Factions;
import factions.util.Colour;
import factions.util.Messenger;
import factions.util.Teams;
import factions.util.Values;
import factions.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Flag extends Block {
	
	private static Map<Colour, Flag> flags = new HashMap<>();
	
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 2.0, 1.4375);
	private static final AxisAlignedBB COLLISION_AABB = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 2.0, 0.625);
	
	private static final int CAPTURE_REWARD = 15;
	private static final int CAPTURE_PENALTY = 5;
	private static final int MAX_CAPTURES = 2;
	
	public final Colour COLOUR;
	
	{
		setBlockUnbreakable();
		setHardness(10000000.0F);
		setResistance(10000000.0F);
		//setCreativeTab(FactionsTab.INSTANCE);
	}
	
	private Flag(Colour colour) {
		
		super(Material.BARRIER);
		
		String name = colour.UNLOCALISED_NAME + "_flag";
		setUnlocalizedName(name);
		
		ResourceLocation resource = new ResourceLocation(Factions.MODID, name);
		setRegistryName(resource);
		ForgeRegistries.BLOCKS.register(this);
		
		COLOUR = colour;
	}
	
	private void interact(EntityPlayer player, World world, BlockPos pos) {
		
		if(COLOUR != Colour.fromFormatter(player.getTeam().getColor())) {
			pickup(player, world, pos);
		
		} else {
			capture(player, world, pos);
		}
	}
	
	private void pickup(EntityPlayer player, World world, BlockPos pos) {
		
		if(Values.getBoolean(player.getName() + "_has_flag")) {
			Messenger.tellPlayer(player, TextFormatting.YELLOW + "You can't carry multiple flags."
					+ TextFormatting.RESET);
			return;
		}
		
		if(!Values.getBoolean("purge_active")) {
			Messenger.tellPlayer(player, TextFormatting.YELLOW + "You can't pick up a flag before the purge starts."
					+ TextFormatting.RESET);
			return;
		}
		
		if(Values.getInteger(Colour.fromFormatter(player.getTeam().getColor()).UNLOCALISED_NAME
				+ "_capture_" + COLOUR.UNLOCALISED_NAME) >= MAX_CAPTURES) {
			Messenger.tellPlayer(player, TextFormatting.YELLOW + "You can't capture the same flag more than "
				+ MAX_CAPTURES + " times in the same purge." + TextFormatting.RESET);
			return;
		}
		
		Values.setEnum(player.getName() + "_flag", COLOUR);
		Values.setBoolean(player.getName() + "_has_flag", true);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		Values.setBoolean(COLOUR.UNLOCALISED_NAME + "_flag_exists", false);
		
		Team team = Teams.getTeam(COLOUR);
		Messenger.announce(player.getTeam().getColor() + player.getName() + TextFormatting.YELLOW
				+ " has picked up " + team.getColor() + team.getName() + "'s" + TextFormatting.YELLOW
				+ " flag." + TextFormatting.RESET);
	}
	
	private void capture(EntityPlayer player, World world, BlockPos pos) {
		
		if(!Values.getBoolean(player.getName() + "_has_flag")) {
			Messenger.tellPlayer(player, TextFormatting.YELLOW + "You can't pick up your own flag." + TextFormatting.RESET);
			return;
		}
		
		if(!Values.getBoolean("purge_active")) {
			Messenger.tellPlayer(player, TextFormatting.YELLOW + "You can't capture a flag after the purge has ended."
					+ TextFormatting.RESET);
			return;
		}
		
		Colour colour = Values.getEnum(player.getName() + "_flag", Colour.class);
		BlockPos home = Values.getPosition(colour.UNLOCALISED_NAME + "_flag_home");
		
		Values.setBoolean(player.getName() + "_has_flag", false);
		home = WorldUtils.placeBlock(world, home, getFlag(colour).getDefaultState());
		Values.setBoolean(colour.UNLOCALISED_NAME + "_flag_exists", true);
		Values.setPosition(colour.UNLOCALISED_NAME + "_flag_position", home);
		int captures = Values.increment(COLOUR.UNLOCALISED_NAME + "_capture_" + colour.UNLOCALISED_NAME, 1);
		
		Team team = Teams.getTeam(colour);
		Messenger.announce(player.getTeam().getColor() + player.getName() + TextFormatting.YELLOW
				+ " has captured " + team.getColor() + team.getName() + "'s" + TextFormatting.YELLOW
				+ " flag. It has been returned to " + TextFormatting.GRAY + Messenger.posStr(home)
				+ TextFormatting.YELLOW + ". [" + captures + "/" + MAX_CAPTURES + "]" + TextFormatting.RESET);
		
		Teams.givePoints(Teams.getTeam(COLOUR), CAPTURE_REWARD);
		Teams.givePoints(team, -CAPTURE_PENALTY);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote) interact(player, world, pos);
		return true;
	}
	
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if(!world.isRemote) interact(player, world, pos);
	}
	
	@Override public boolean isFullCube(IBlockState state) { return false; }
	
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	
	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}
	
	@Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return COLLISION_AABB;
	}
	
	@Override public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos position) {
		return MapColor.getBlockColor(COLOUR.DYE_COLOUR);
	}
	
	public static void init() {
		
		for(Colour colour : Colour.values()) {
			flags.put(colour, new Flag(colour));
		}
	}
	
	 public static Flag getFlag(Colour colour) {
		 return flags.get(colour);
	 }
	 
	 public static class FlagEvent {
		 
		 private void drop(EntityPlayer player) {
			
			 if(Values.getBoolean(player.getName() + "_has_flag")) {
				 
				 Flag flag = getFlag(Values.getEnum(player.getName() + "_flag", Colour.class));
				
				 BlockPos pos = WorldUtils.placeBlock(player.getEntityWorld(),
						 player.getPosition(), flag.getDefaultState());
				 
				 Values.setBoolean(player.getName() + "_has_flag", false);
				 Values.setBoolean(flag.COLOUR.UNLOCALISED_NAME + "_flag_exists", true);
				 Values.setPosition(flag.COLOUR.UNLOCALISED_NAME + "_flag_position", pos);
				 
				 Team team = Teams.getTeam(flag.COLOUR);
				 Messenger.announce(player.getTeam().getColor() + player.getName() + TextFormatting.YELLOW
						 + " has dropped " + team.getColor() + team.getName() + "'s" + TextFormatting.YELLOW
						 + " flag at " + TextFormatting.GRAY + Messenger.posStr(pos)
						 + TextFormatting.YELLOW + "." + TextFormatting.RESET);
			 }
		 }
		 
		 @SubscribeEvent
		 public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
			 drop(event.player);
		 }
		 
		 @SubscribeEvent
		 public void onPlayerDeath(LivingDeathEvent event) {
			 if(event.getEntityLiving() instanceof EntityPlayer) {
				 drop((EntityPlayer) event.getEntityLiving());
			 }
		 }
	 }
}