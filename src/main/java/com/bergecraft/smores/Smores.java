package com.bergecraft.smores;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
                dropBlockAsMaterial(broken, mat);
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
    
    @Bergification(opt="stone_gold_iron", def="true")
    @EventHandler(ignoreCancelled=true, priority = EventPriority.HIGH)
    public void onGoldOreBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        Material mat = broken.getType();
        Material tool = e.getPlayer().getItemInHand().getType();
            
        if((mat == Material.GOLD_ORE && (tool == Material.STONE_PICKAXE || tool == Material.GOLD_PICKAXE))
                || ((mat == Material.IRON_ORE || mat == Material.LAPIS_ORE) && tool == Material.GOLD_PICKAXE)) {
            dropBlockAsMaterial(broken, mat);
        } else if (mat == Material.IRON_ORE && tool == Material.STONE_PICKAXE) {
            dropBlockAsMaterial(broken, null);
        }
    }
    
    private void dropBlockAsMaterial(Block b, Material m) {
        b.setType(Material.AIR);
        b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(m));
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
}