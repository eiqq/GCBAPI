package org.EIQUI.GCBAPI.blockbreaker;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class BlockSaver {

    public Material material;
    public BlockData blockData;

    public BlockSaver(Material m,BlockData bd){
        material = m;
        blockData = bd;
    }
}
