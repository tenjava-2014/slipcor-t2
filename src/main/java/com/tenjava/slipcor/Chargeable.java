package com.tenjava.slipcor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ItemFrame;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneWire;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by slipcor on 12.07.2014.
 */
public class Chargeable {
    private static Material offMaterial;
    private static Material onMaterial;
    private static int threshold;
    private static BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};

    static {
        offMaterial = Material.COAL_BLOCK;
        onMaterial = Material.IRON_BLOCK;
        threshold = 50;
    }

    private int power;
    private Block b;
    private ItemFrame[] frames = new ItemFrame[4];

    private final int checkID;

    public Chargeable(TenJava plugin, Block block) {
        block.setType(offMaterial);
        power = 0;
        b = block;

        checkID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Checker(), 20L, 20L);
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(checkID);
    }

    public Block getBlock() {
        return b;
    }

    void update() {

        Map<BlockFace, Byte> result = new HashMap<BlockFace, Byte>();


        for (BlockFace face : faces) {
            if (b.getRelative(face).getState().getData() instanceof Redstone) {
                if (b.getRelative(face).getState().getData() instanceof RedstoneWire) {
                    RedstoneWire wire = (RedstoneWire) b.getRelative(face).getState().getData();
                    result.put(face, wire.getData());
                } else {
                    result.put(face, (byte) 15); // check for daylight
                }
            }
        }

        if (result.size() == 0) {
            return;
        }

        int connections = result.size();

        int applicablePower = power / connections;

        for (BlockFace face : faces) {
            if (b.getRelative(face).getState().getData() instanceof RedstoneWire) {
                RedstoneWire wire = (RedstoneWire) b.getRelative(face).getState().getData();

                int localPower = wire.getData();

                if (localPower > applicablePower) {
                    // fill power and reduce
                    Bukkit.broadcastMessage("adding: " + face.name());
                    power += localPower-applicablePower;
                    reduce(b, face, localPower-applicablePower);
                } else if (localPower < applicablePower) {
                    // send out power
                    power -= applicablePower-localPower;
                    raise(b, face, applicablePower-localPower);
                }
            }
        }

        Bukkit.broadcastMessage("Power: " + power);
    }

    /**
     * raise the wire current in an outgoing direction by a certain value
     * @param face the BlockFace direction
     * @param i the value to raise
     */
    private void raise(Block block, BlockFace face, int i) {
        Bukkit.broadcastMessage("Raising block " + block.getX()+"/" + block.getY()+"/" + block.getZ()+"/"+face.name()+": "+i);
        Block checkBlock = block.getRelative(face);

        for (BlockFace bf : faces) {
            if (bf == face.getOppositeFace()) {
                continue;
            }
            if (checkBlock.getRelative(bf).getState().getData() instanceof RedstoneWire) {
                BlockState state = checkBlock.getRelative(bf).getState();
                RedstoneWire wire = (RedstoneWire) state.getData();
                wire.setData((byte) (wire.getData()+i));

                checkBlock.getRelative(BlockFace.UP, 4).setType(Material.WOOL);
                checkBlock.getRelative(BlockFace.UP, 4).setData(wire.getData());

                state.update();
                if (i>0) {
                    raise(checkBlock, bf, i - 1);
                }
            }
        }
    }

    /**
     * reduce the wire in an incoming direction by a certain value
     * @param face the BlockFace direction
     * @param i the value to reduce
     */
    private void reduce(Block block, BlockFace face, int i) {
        Bukkit.broadcastMessage("Reducing block " + block.getX()+"/" + block.getY()+"/" + block.getZ()+"/"+face.name()+": "+i);
        Block checkBlock = block.getRelative(face);

        for (BlockFace bf : faces) {
            if (bf == face.getOppositeFace()) {
                continue;
            }
            if (checkBlock.getRelative(bf).getState().getData() instanceof RedstoneWire) {
                BlockState state = checkBlock.getRelative(bf).getState();
                RedstoneWire wire = (RedstoneWire) state.getData();
                wire.setData((byte) (wire.getData()-i));

                checkBlock.getRelative(BlockFace.UP, 4).setType(Material.WOOL);
                checkBlock.getRelative(BlockFace.UP, 4).setData(wire.getData());

                state.update();

                if (i > 0) {
                    reduce(checkBlock, bf, i - 1);
                }
            }
        }
    }

    class Checker implements Runnable {
        @Override
        public void run() {
            Chargeable.this.update();
        }
    }
}
