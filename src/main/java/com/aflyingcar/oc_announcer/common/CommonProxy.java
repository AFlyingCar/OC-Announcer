package com.aflyingcar.oc_announcer.common;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.aflyingcar.oc_announcer.client.VOXResourcePack;
import com.aflyingcar.oc_announcer.common.block.BlockAnnouncer;
import com.aflyingcar.oc_announcer.common.handler.SoundHandler;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.util.Objects;

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

    public void registerSounds() {
        File dataDirectory = FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
        File vox_pack_path = new File(dataDirectory, VOXResourcePack.RELATIVE_VOX_PATH);
        if(!vox_pack_path.exists()) return;

        // This should never be null, because we checked for its existance up above
        for(File file : Objects.requireNonNull(vox_pack_path.listFiles())) {
            if(file.isDirectory()) {
                String vox_name = file.getName();
                OCAnnouncer.getLogger().debug("Loading VOX Pack '" + vox_name + "'");
                // This should also never be null, again because we checked for if it is a directory
                for(File vox_file : Objects.requireNonNull(new File(file, "sounds").listFiles())) {
                    OCAnnouncer.addSoundToVOX(vox_name, vox_file.getName());
                }
            }
        }
    }
}
