package com.tenjava.slipcor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChargeListener implements Listener {
    final TenJava plugin;
    public ChargeListener(final TenJava plugin) {
       this.plugin = plugin;
    }

    private boolean handleCancel(final Cancellable event, final Player player, final ItemStack checkItem, final boolean announce) {
        ItemStack batteryItem = TenJava.getBatteryItem();

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
    public void onBreak(final BlockBreakEvent event) {
        for (Chargeable c : plugin.getChargeables()) {
            if (c.getBlock().equals(event.getBlock())) {
                event.setCancelled(true);
                int power = c.getPower();
                c.destroy();
                plugin.removeChargeable(c);
                event.getBlock().setType(Material.AIR);

                ItemStack battery = TenJava.getBatteryItem();
                ItemMeta meta = battery.getItemMeta();
                meta.setDisplayName(ChatColor.ITALIC.toString() + power);
                battery.setItemMeta(meta);

                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), battery);

                return;
            }
            if (c.getBlockMap().containsKey(event.getBlock())) {
                c.getBlockMap().clear();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        ItemStack batteryItem = TenJava.getBatteryItem();

        int power = 0;

        if (event.getItemInHand().hasItemMeta() && batteryItem.getItemMeta().getLore().equals(event.getItemInHand().getItemMeta().getLore())) {
            if (event.getItemInHand().getItemMeta().hasDisplayName()) {
                try {
                    String display = event.getItemInHand().getItemMeta().getDisplayName();
                    if (!display.startsWith(ChatColor.ITALIC.toString())) {
                        return;
                    }
                    power = Integer.parseInt(ChatColor.stripColor(display.substring(0, display.length())));
                } catch (NumberFormatException e) {
                }
            }
            event.setCancelled(true);
            event.getItemInHand().setType(Material.AIR);

            plugin.addChargeable(new Chargeable(plugin, event.getBlock(), power));
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onItemClick(final InventoryClickEvent event) {
        handleCancel(event, (Player) event.getWhoClicked(), event.getCurrentItem(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(final PlayerDropItemEvent event) {
        handleCancel(event, event.getPlayer(), event.getItemDrop().getItemStack(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
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

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.ITEM_FRAME) {
            return;
        }

        ItemFrame frame = (ItemFrame)event.getEntity();

        for (Chargeable c : plugin.getChargeables()) {
            if (c.getBlock().equals(frame.getLocation().getBlock().getRelative(frame.getAttachedFace()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRedstoneChange(final BlockRedstoneEvent event) {
        for (Chargeable c : plugin.getChargeables()) {
            if (!c.getBlockMap().containsKey(event.getBlock())) {
                continue;
            }

            if (c.getBlockMap().get(event.getBlock())) {
                event.setNewCurrent(event.getOldCurrent());
            } else {
                c.getBlockMap().put(event.getBlock(), true);
            }
        }
    }
}
