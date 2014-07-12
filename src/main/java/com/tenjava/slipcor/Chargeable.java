package com.tenjava.slipcor;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;

/**
 * Created by slipcor on 12.07.2014.
 */
public class Chargeable {
    private static Material offMaterial;
    private static Material onMaterial;
    private static int threshold;
    private static BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP};

    static {
        offMaterial = Material.COAL_BLOCK;
        onMaterial = Material.IRON_BLOCK;
        threshold = 50;
    }

    private int power;
    private Block b;
    private ItemFrame[] frames = new ItemFrame[4];

    public Chargeable(TenJava plugin, Block block) {
        block.setType(offMaterial);
        power = 0;
        b = block;

        update();
    }

    public Block getBlock() {
        return b;
    }

    void update() {
        for (BlockFace face : faces) {
            power += b.getBlockPower(face);
        }
        if (power > 100) {
            power = 100;
        }
    }
}
