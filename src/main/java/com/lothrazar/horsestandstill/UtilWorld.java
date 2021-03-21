package com.lothrazar.horsestandstill;

import net.minecraft.util.math.BlockPos;

public class UtilWorld
{
    public static double distanceBetweenHorizontal(BlockPos start, BlockPos end)
    {
        int xDistance = Math.abs(start.getX() - end.getX());
        int zDistance = Math.abs(start.getZ() - end.getZ());
        // ye olde pythagoras
        return Math.sqrt(xDistance * xDistance + zDistance * zDistance);
    }
}