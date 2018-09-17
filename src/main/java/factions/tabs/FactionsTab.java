package factions.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class FactionsTab extends CreativeTabs {
	
	public static final FactionsTab INSTANCE = new FactionsTab();
	
	private static final ItemStack ICON = new ItemStack(Items.IRON_AXE);
	
	private FactionsTab() { super(getNextID(), "factions"); }
	
	@Override public ItemStack getTabIconItem() { return ICON; }

}