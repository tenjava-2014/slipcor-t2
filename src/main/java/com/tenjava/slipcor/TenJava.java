package com.tenjava.slipcor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TenJava extends JavaPlugin {
    static Material blockType;
    static List<String> lore = Arrays.asList(new String[]{"Battery Placer", "Right click to place a Battery", "Left click to turn the clicked", "block into a Battery"});

    /**
     * Get an instance of the Battery Item
     * @return the instance
     */
    static ItemStack getBatteryItem() {
        ItemStack batteryItem = new ItemStack(blockType);
        ItemMeta meta = batteryItem.getItemMeta();
        meta.setLore(lore);
        batteryItem.setItemMeta(meta);
        return batteryItem;
    }

    /**
     * the list containing all Chargeables
     */
    private final List<Chargeable> chargeableList = new ArrayList<Chargeable>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new ChargeListener(this), this);
        getCommand("battery").setExecutor(new CmdBattery(this));
        String type = getConfig().getString("blocktype");
        try {
            blockType = Material.valueOf(type);
            lore = getConfig().getStringList("lore");
        } catch (RuntimeException e) {
            getLogger().warning("Unknown MATERIAL set in 'blocktype': " + type);
            getLogger().warning("Defaulting to IRON_BLOCK");
            blockType = Material.IRON_BLOCK;
        }
    }

    /**
     * add a Chargable to the list
     * @param chargeable the Chargable to add
     */
    public void addChargeable(final Chargeable chargeable) {
        if (!chargeableList.contains(chargeable)) {
            chargeableList.add(chargeable);
        }
    }

    /**
     * get all Chargeables
     * @return all Chargeables
     */
    public List<Chargeable> getChargeables () {
        return chargeableList;
    }

    /**
     * remove a Chargable from the list
     * @param chargeable the Chargable to remoev
     */
    public void removeChargeable(final Chargeable chargeable) {
        chargeableList.remove(chargeable);
    }
}
