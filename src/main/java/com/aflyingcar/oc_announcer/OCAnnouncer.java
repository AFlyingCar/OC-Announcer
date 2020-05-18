package com.aflyingcar.oc_announcer;

import com.aflyingcar.oc_announcer.common.CommonProxy;
import com.aflyingcar.oc_announcer.common.block.BlockAnnouncer;
import com.aflyingcar.oc_announcer.common.tileentity.TileEntityAnnouncer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(modid = OCAnnouncer.MODID, name = OCAnnouncer.NAME, version = OCAnnouncer.VERSION)
public class OCAnnouncer {
    public static final String MODID = "oc_announcer";
    public static final String NAME = "Open Computers Announcer";
    public static final String VERSION = "1.0";

    private static Map<String, List<String>> vox_sound_lists = new HashMap<>();

    private static Logger logger;

    @SidedProxy(clientSide = "com.aflyingcar.oc_announcer.client.ClientProxy", serverSide = "com.aflyingcar.oc_announcer.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger getLogger() {
        return logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        proxy.preinit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    public static void addSoundToVOX(String vox, String file) {
        if(!vox_sound_lists.containsKey(vox))
            vox_sound_lists.put(vox, new ArrayList<>());
        vox_sound_lists.get(vox).add(file);
    }

    public static Map<String, List<String>> getVOXSoundLists() {
        return vox_sound_lists;
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void handleRegisterBlock(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(CommonProxy.announcerBlock);

            GameRegistry.registerTileEntity(TileEntityAnnouncer.class, new ResourceLocation(MODID, BlockAnnouncer.NAME));
        }

        @SubscribeEvent
        public static void handleRegisterItem(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(new ItemBlock(CommonProxy.announcerBlock).setRegistryName(CommonProxy.announcerBlock.getRegistryName()));
        }
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventBusSubscriber
    public static class ClientEventHandler {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            registerModel(Item.getItemFromBlock(CommonProxy.announcerBlock), 0);
        }

        protected static void registerModel(Item item, int meta) {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
