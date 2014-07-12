package com.tenjava.slipcor;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Redstone;

/**
 * Created by slipcor on 12.07.2014.
 */
public class ChargeListener implements Listener {
    final TenJava plugin;
    public ChargeListener(final TenJava plugin) {
       this.plugin = plugin;
    }

    private boolean handleCancel(final Cancellable event, final Player player, final ItemStack checkItem, final boolean announce) {
        ItemStack batteryItem = CmdBattery.getBatteryItem();

        if (checkItem.getType() == batteryItem.getType() && checkItem.hasItemMeta() &&
                batteryItem.getItemMeta().getLore().equals(checkItem.getItemMeta().getLore())) {
            if (announce) {
                player.sendMessage("Don't do that to the Battery Placer!");
            }
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        for (Chargeable c : plugin.getChargeables()) {
            if (c.getBlock().equals(event.getBlock())) {
                c.destroy();
                plugin.removeChargeable(c);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemClick(InventoryClickEvent event) {
        handleCancel(event, (Player) event.getWhoClicked(), event.getCurrentItem(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        handleCancel(event, event.getPlayer(), event.getItemDrop().getItemStack(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand() == null) {
            return;
        }

        if (event.hasBlock() && handleCancel(event, event.getPlayer(), event.getPlayer().getItemInHand(), false)) {
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                plugin.addChargeable(new Chargeable(plugin, event.getClickedBlock()));
            } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                plugin.addChargeable(new Chargeable(plugin, event.getClickedBlock().getRelative(event.getBlockFace())));
            }
        }
    }
}
