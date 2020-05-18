package com.aflyingcar.oc_announcer.client;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class VOXResourcePack implements IResourcePack {
    protected final static String PACK_NAME = OCAnnouncer.MODID;
    public final static String RELATIVE_VOX_PATH = "/assets/OCAnnouncer/vox_packs/";

    private String relative_path = RELATIVE_VOX_PATH;
    private String pack_name = PACK_NAME;

    private static Map<String, List<String>> sounds = new HashMap<>();

    public VOXResourcePack(String vox_name) {
        relative_path += vox_name + "/sounds/";
//        pack_name += "." + vox_name;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        if(location.getResourcePath().equals("sounds.json")) {
            return generateJSON();
        } else {
            File path = getPath(location);
            return new FileInputStream(path);
        }
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return location.getResourceDomain().equals(getPackName()) &&
                (location.getResourcePath().equals("sounds.json") || getPath(location).exists());
    }

    protected File getPath(ResourceLocation location) {
        String filename;
        if(location.getResourcePath().endsWith(".ogg")) {
            filename = location.getResourcePath();//.substring(7, location.getResourcePath().indexOf("."));
            int d1 = filename.indexOf('.');
            if(d1 != -1) {
                int d2 = filename.indexOf('.', d1 + 1);
                // Chop off the pack-identifier if one is there
                if(d2 != -1)
                    filename = filename.substring(d1 + 1);
            }
        } else {
            filename = location.getResourcePath().substring(7);
        }
        return new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath(), relative_path + filename);
    }

    @Override
    public Set<String> getResourceDomains() {
        return Sets.newHashSet(getPackName());
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return pack_name;
    }

    @Nullable
    public static List<String> getWordListForVOX(String vox_name) {
        return sounds.getOrDefault(vox_name, null);
    }

    public static ResourceLocation addSound(String vox_name, String path) {
        if(!sounds.containsKey(vox_name))
            sounds.put(vox_name, new ArrayList<>());
        sounds.get(vox_name).add(path);

        return soundToResourceLocation(vox_name, path);
    }

    @SideOnly(Side.CLIENT)
    public void registerAsResourceLocation() {
        OCAnnouncer.getLogger().debug("Registering vox pack " + getPackName());
        List<IResourcePack> defaultResourcePacks = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
        defaultResourcePacks.add(this);
        Minecraft.getMinecraft().refreshResources();
    }

    protected static String buildSoundName(String vox_name, String path) {
        return vox_name + "." + path.substring(0, path.lastIndexOf('.'));
    }

    protected static ResourceLocation soundToResourceLocation(String vox_name, String path) {
        return new ResourceLocation(PACK_NAME, buildSoundName(vox_name, path));
    }

    protected static InputStream generateJSON() {
        JsonObject root = new JsonObject();
        for(Map.Entry<String, List<String>> entry : sounds.entrySet()) {
            for(String file : entry.getValue()) {
                JsonObject event = new JsonObject();
                event.addProperty("category", "records");
                JsonArray sounds = new JsonArray();
                JsonObject sound = new JsonObject();

                String sound_name = buildSoundName(entry.getKey(), file);

                // ${PACK_NAME}_${vox_name}:sounds/${PATH_TO_SOUND}
                sound.addProperty("name", PACK_NAME + ":" + sound_name);
                sound.addProperty("stream", false);
                sounds.add(sound);
                event.add("sounds", sounds);
                event.addProperty("subtitle", file.substring(0, file.lastIndexOf('.')));
                root.add(sound_name, event);
            }
        }

        return new ByteArrayInputStream(new Gson().toJson(root).getBytes());
    }
}
