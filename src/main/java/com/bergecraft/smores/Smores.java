package com.bergecraft.smores;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.valadian.bergecraft.annotations.Bergification;

public class Smores extends JavaPlugin implements Listener {
    
    /**
     * Materials that are not to be smelted in a furnace
     */
    static final List<Material> SMELTED_ITEMS = Arrays.asList(
            Material.CLAY_BALL,
            Material.CLAY, 
            Material.SAND,
            Material.NETHERRACK,
            Material.COBBLESTONE,
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE, 
            Material.QUARTZ_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE);
    
    /**
     * Materials corresponding to an ore block
     */
    static final List<Material> ORES = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE, 
            Material.QUARTZ_ORE,
            Material.REDSTONE_ORE,
            Material.GLOWING_REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE);

    private static final String TAG = "[SmOres] ";
    private static final Logger mLog = Logger.getLogger("SmOres");
    private static Random r = new Random();
      
    public void onEnable() {
        removeFurnaceRecipes();
        getServer().getPluginManager().registerEvents(this, this);
        mLog.info(TAG + "enabled");
    }
      
    
    @Bergification(opt="furnace_can_smelt", def="true")
    public void removeFurnaceRecipes() {
      Iterator<Recipe> it = getServer().recipeIterator();
      
      while (it.hasNext()) {
        Recipe recipe = it.next();
        
        if (recipe instanceof FurnaceRecipe) {
            FurnaceRecipe smelt = (FurnaceRecipe)recipe;
            Material input = smelt.getInput().getType();
            
            if(SMELTED_ITEMS.contains(input)) {
              it.remove();
              mLog.info(TAG + "Furnace Recipe disabled for input: " + input);
            }
        }
      }
    }
    
    @Bergification(opt="ores_drop_self", def="true")
    @EventHandler(ignoreCancelled=true, priority = EventPriority.HIGH)
    public void onOreBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        Material mat = getOreMaterial(broken.getType());
        if(mat != null) {
            ItemStack tool = e.getPlayer().getItemInHand();
            
            if(!broken.getDrops(tool).isEmpty()) {
                wearTool(tool);
                dropBlockAsMaterial(broken, mat);
                e.setCancelled(true);
            }
        }
    }
    
    @Bergification(opt="ores_drop_self", def="true")
    @EventHandler(ignoreCancelled=true, priority = EventPriority.HIGH)
    public void onOreExplode(EntityExplodeEvent e) {
        Iterator<Block> it = e.blockList().iterator();
        while(it.hasNext()) {
            Block block = it.next();
            Material mat = getOreMaterial(block.getType());
            
            if(mat != null) {
                //handle ore damage manually, cancel then drop
                block.getDrops().clear();
                it.remove();
                
                if(r.nextFloat() < e.getYield()) {
                    dropBlockAsMaterial(block, mat);
                } else {
                    block.setType(Material.AIR);
                }
            }
        }
    }
    
    @Bergification(opt="gold_as_bronze", def="true")
    @EventHandler(ignoreCancelled=true, priority = EventPriority.NORMAL)
    public void onGoldOreBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        Material mat = broken.getType();
        ItemStack tool = e.getPlayer().getItemInHand();
        Material toolType = tool.getType();
            
        if((toolType == Material.STONE_PICKAXE && mat == Material.GOLD_ORE || mat == Material.GOLD_BLOCK) || (toolType == Material.GOLD_PICKAXE && 
                (mat == Material.IRON_ORE || mat == Material.IRON_BLOCK || mat == Material.GOLD_ORE || mat == Material.GOLD_BLOCK))) {
            wearTool(tool);
            broken.breakNaturally();
            e.setCancelled(true);
        } else if (toolType == Material.STONE_PICKAXE && 
                (mat == Material.LAPIS_ORE || mat == Material.LAPIS_BLOCK || mat == Material.IRON_ORE || mat == Material.IRON_BLOCK)) {
            wearTool(tool);
            dropBlockAsMaterial(broken, null);
            e.setCancelled(true);
        }
    }
    
    private void dropBlockAsMaterial(Block b, Material m) {
        b.setType(Material.AIR);
        if(m != null) {
            b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(m));
        }
    }
    
    private Material getOreMaterial(Material m) {
        if(ORES.contains(m)) {
            if(m == Material.GLOWING_REDSTONE_ORE) {
                return Material.REDSTONE_ORE;
            }
            return m;
        }
        return null;
    }
    
    private void wearTool(ItemStack tool) {
        if(r.nextInt(tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1) == 0) {
            tool.setDurability((short) (tool.getDurability() + 1));
        }
    }
}