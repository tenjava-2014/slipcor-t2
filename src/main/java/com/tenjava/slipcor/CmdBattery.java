package com.tenjava.slipcor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CmdBattery implements CommandExecutor {

    final TenJava plugin;
    final Map<String, ItemStack> saved = new HashMap<String, ItemStack>();

    public CmdBattery(final TenJava tenJava) {
        plugin = tenJava;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) commandSender;

        ItemStack batteryItem = TenJava.getBatteryItem();

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
}
