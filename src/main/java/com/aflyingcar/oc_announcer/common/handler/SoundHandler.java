package com.aflyingcar.oc_announcer.common.handler;

import java.util.Map;
import java.util.HashMap;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegistryEvent;

@Mod.EventBusSubscriber
public final class SoundHandler {
    private static Map<String, SoundEvent> sounds_map;
    private static int size = 0;

    static {
        sounds_map = new HashMap<>();
    }

    public static void init() {
        size = SoundEvent.REGISTRY.getKeys().size();
    }

    public static void registerSound(ResourceLocation location) {
        SoundEvent event = new SoundEvent(location).setRegistryName(location);

        // SoundEvent.REGISTRY.register(size, location, event);

        sounds_map.put(location.getResourcePath(), event);
    }

    public static SoundEvent getSoundEvent(ResourceLocation location) {
        return sounds_map.get(location.getResourcePath());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        final SoundEvent[] events = sounds_map.values().toArray(new SoundEvent[0]);

        if(events == null) {
            OCAnnouncer.getLogger().error("Failed to register sounds: null");
        } else {
            event.getRegistry().registerAll(events);
        }
    }
}

