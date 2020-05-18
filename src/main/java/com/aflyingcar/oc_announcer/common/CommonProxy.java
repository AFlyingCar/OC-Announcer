package com.aflyingcar.oc_announcer.common;

import com.aflyingcar.oc_announcer.common.block.BlockAnnouncer;
import com.aflyingcar.oc_announcer.common.handler.SoundHandler;
import net.minecraft.block.Block;

public class CommonProxy {
    public static Block announcerBlock;

    static {
        announcerBlock = new BlockAnnouncer();
    }

    public void preinit() {
        SoundHandler.init();
        registerSounds();
    }
    public void init() {
    }
    public void postinit() { }

    public void registerSounds() { }
}
