package com.minelittlepony;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class MineLittlePony {
    public static Object getConfig() {
        throw new RuntimeException();
    }
}
