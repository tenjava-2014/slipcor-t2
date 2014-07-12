package com.tenjava.slipcor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneWire;

import java.util.*;

public class Chargeable {
    /**
     * the BlockFaces to check for connections
     */
    private static BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};

    /**
     * the blocks belonging to this Chargeable
     *
     * The key is the block, duh, the value is either:
     * true - redstone should not be update by the server
     * false - redstone may be altered once
     */
    private final Map<Block, Boolean> blockMap = new HashMap<Block, Boolean>();

    /**
     * the plugin instance
     */
    private final TenJava plugin;

    /**
     * the power value display item frame
     */
    private final ItemFrame frame;

    /**
     * the block where the Chargeable is placed
     */
    private final Block b;

    /**
     * the Runnable ID for cancelling purposes
     */
    private final int checkID;

    /**
     * the power value
     */
    private int power;

    /**
     * Create a Chargeable instance
     * @param plugin the TenJava plugin instance
     * @param block the block where to place it
     * @param power the initial power value
     */
    public Chargeable(final TenJava plugin, final Block block, final int power) {
        block.setType(TenJava.getBatteryItem().getType());
        this.power = power;
        this.plugin = plugin;
        b = block;

        frame = b.getWorld().spawn(b.getLocation(), ItemFrame.class);
        frame.setFacingDirection(BlockFace.EAST);

        final ItemStack display = new ItemStack(Material.REDSTONE);
        final ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(String.valueOf(power));
        display.setItemMeta(meta);

        frame.setItem(display.clone());

        checkID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Checker(), 20L, 20L);
    }

    /**
     * Constructor shortcut for lazy developers, defaulting to no power
     * @param plugin the TenJava plugin instance
     * @param block the block where to place it
     */
    public Chargeable(final TenJava plugin, final Block block) {
        this(plugin, block, 0);
    }

    /**
     * properly cleanup when removing a Chargeable
     */
    public void destroy() {
        Bukkit.getScheduler().cancelTask(checkID);
        frame.remove();
    }

    /**
     * hand over the Block instance
     * @return the block
     */
    public Block getBlock() {
        return b;
    }

    /**
     * hand over the power value
     * @return the power value
     */
    public int getPower() {
        return power;
    }

    /**
     * hand over the block map
     * @return the block map
     */
    public Map<Block, Boolean> getBlockMap() {
        return blockMap;
    }

    /**
     * update a Chargeable:
     *
     * * check ingoing power
     * * check outgoing power
     * * commit changes to the environment
     */
    private void update() {
        final Map<BlockFace, Byte> result = new HashMap<BlockFace, Byte>();

        for (BlockFace face : faces) { // for all possible connection faces
            if (b.getRelative(face).getState().getData() instanceof Redstone) {
                if (b.getRelative(face).getState().getData() instanceof RedstoneWire) {
                    // the next step is a wire, save data
                    RedstoneWire wire = (RedstoneWire) b.getRelative(face).getState().getData();
                    result.put(face, wire.getData());
                } else if (b.getRelative(face).getType() == Material.DAYLIGHT_DETECTOR) {
                    // it's a daylight detector
                    result.put(face,  b.getRelative(face).getState().getData().getData());
                } else if (plugin.getConfig().getBoolean("burnout")){
                    if (b.getState().getData().getData() < 2) {
                        Block block = b.getRelative(face);
                        if (plugin.getConfig().contains("burnouts") &&
                                plugin.getConfig().getConfigurationSection("burnouts").contains(block.getType().name())) {
                            String value = plugin.getConfig().getConfigurationSection("burnouts").getString(block.getType().name());
                            try {
                                Material mat = Material.valueOf(value);
                                block.setType(mat);
                                getBlockMap().clear(); // force update
                            } catch (RuntimeException e) {
                                plugin.getLogger().warning("Unknown material set in 'burnouts'->'" + block.getType().name()+"': " + value);
                            }
                        }
                    }
                }
            }
        }

        if (result.size() == 0) {
            return; // no connections, nothing happens!
        }

        int connections = result.size();

        int applicablePower = power / connections; // the shared power per connection

        for (BlockFace face : faces) { // for all connection faces
            if (b.getRelative(face).getState().getData() instanceof RedstoneWire) {

                final int localPower = b.getRelative(face).getState().getData().getData();

                blockMap.put(b.getRelative(face), false);

                if (localPower > applicablePower) {
                    // fill power and reduce
                    power += localPower-applicablePower;
                    reduce(b, face, localPower-applicablePower);
                } else if (localPower < applicablePower) {
                    // send out power
                    power -= applicablePower-localPower;
                    raise(b, face, applicablePower-localPower);
                }
            }
        }

        // Update the item frame display
        final ItemStack display = frame.getItem();
        final ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(String.valueOf(power));
        display.setItemMeta(meta);
        frame.setItem(display.clone());
    }

    /**
     * raise the wire current in an outgoing direction by a certain value
     * @param block the source Block
     * @param face the BlockFace direction
     * @param i the value to raise
     */
    private void raise(final Block block, final BlockFace face, final int i) {
        final Block checkBlock = block.getRelative(face);

        for (BlockFace bf : faces) {
            if (bf == face.getOppositeFace()) {
                continue; // never go back!
            }
            if (checkBlock.getRelative(bf).getState().getData() instanceof RedstoneWire) {
                final BlockState state = checkBlock.getRelative(bf).getState();
                final RedstoneWire wire = (RedstoneWire) state.getData();
                wire.setData((byte) (wire.getData()+i));
/*
                checkBlock.getRelative(BlockFace.UP, 4).setType(Material.WOOL);
                checkBlock.getRelative(BlockFace.UP, 4).setData(wire.getData());
*/
                state.update();

                if (i > 0) { // if there is still current left, raise the next
                    raise(checkBlock, bf, i - 1);
                }
            }
        }
    }

    /**
     * reduce the wire in an incoming direction by a certain value
     * @param block the source Block
     * @param face the BlockFace direction
     * @param i the value to reduce
     */
    private void reduce(final Block block, final BlockFace face, final int i) {
        final Block checkBlock = block.getRelative(face);

        for (BlockFace bf : faces) {
            if (bf == face.getOppositeFace()) {
                continue; // never go back!
            }
            if (checkBlock.getRelative(bf).getState().getData() instanceof RedstoneWire) {
                final BlockState state = checkBlock.getRelative(bf).getState();
                final RedstoneWire wire = (RedstoneWire) state.getData();
                wire.setData((byte) (wire.getData()-i));
/*
                checkBlock.getRelative(BlockFace.UP, 4).setType(Material.WOOL);
                checkBlock.getRelative(BlockFace.UP, 4).setData(wire.getData());
*/
                state.update();

                if (i > 0) { // if there is still current left, reduce the next
                    reduce(checkBlock, bf, i - 1);
                }
            }
        }
    }

    /**
     * the Timer class that simply updates the Chargeable
     */
    class Checker implements Runnable {
        @Override
        public void run() {
            Chargeable.this.update();
        }
    }
}
