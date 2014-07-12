package com.tenjava.slipcor;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slipcor on 12.07.2014.
 */
public class CmdBattery implements CommandExecutor {
    final TenJava plugin;
    static Material blockType;
    static List<String> lore = Arrays.asList(new String[]{"Battery Placer","Right click to place a Battery", "Left click to turn the clicked", "block into a Battery"});

    final Map<String, ItemStack> saved = new HashMap<String, ItemStack>();

    public CmdBattery(TenJava tenJava) {
        plugin = tenJava;
        String type = plugin.getConfig().getString("blocktype");
        try {
            blockType = Material.valueOf(type);
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Unknown MATERIAL set in 'blocktype': " + type);
            blockType = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) commandSender;

        ItemStack batteryItem = getBatteryItem();

        if (saved.containsKey(player.getName())) {
            // we need to remove



            player.getInventory().remove(batteryItem);
            player.getInventory().addItem(saved.get(player.getName()));

            saved.remove(player.getName());

            player.sendMessage("The Battery Placer has been removed!");
        } else {
            saved.put(player.getName(), player.getItemInHand());

            player.setItemInHand(batteryItem);

            player.sendMessage("Here is the Battery Placer!");
        }


        return true;
    }

    protected static ItemStack getBatteryItem() {
        ItemStack batteryItem = new ItemStack(blockType);
        ItemMeta meta = batteryItem.getItemMeta();
        meta.setLore(lore);
        batteryItem.setItemMeta(meta);
        return batteryItem;
    }
}
