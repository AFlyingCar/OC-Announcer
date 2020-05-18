package com.aflyingcar.oc_announcer.client;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.aflyingcar.oc_announcer.common.CommonProxy;
import com.aflyingcar.oc_announcer.common.handler.SoundHandler;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerSounds() {
        File vox_pack_path = new File(Minecraft.getMinecraft().mcDataDir + VOXResourcePack.RELATIVE_VOX_PATH);
        if(!vox_pack_path.exists())
            return;

        for(File file : vox_pack_path.listFiles()) {
            if(file.isDirectory()) {
                String vox_name = file.getName();
                OCAnnouncer.getLogger().debug("Loading VOX Pack '" + vox_name + "'");
                VOXResourcePack r = new VOXResourcePack(vox_name);
                for(File vox_file : new File(file, "sounds").listFiles()) {
                    OCAnnouncer.addSoundToVOX(vox_name, vox_file.getName());
                    SoundHandler.registerSound(r.addSound(vox_name, vox_file.getName()));
                }
                r.registerAsResourceLocation();
            }
        }
    }

    @Override
    public void preinit() {
        super.preinit();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postinit() {
        super.postinit();
    }
}
