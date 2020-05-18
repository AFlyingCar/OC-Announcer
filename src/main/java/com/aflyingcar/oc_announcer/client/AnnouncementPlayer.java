package com.aflyingcar.oc_announcer.client;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.aflyingcar.oc_announcer.common.handler.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AnnouncementPlayer {
    private static class AnnouncementSound extends PositionedSound implements ITickableSound {
        private boolean isDone = false;

        protected AnnouncementSound(ResourceLocation soundId, BlockPos pos, float volume, float pitch) {
            super(soundId, SoundCategory.BLOCKS);
            this.xPosF = pos.getX();
            this.yPosF = pos.getY();
            this.zPosF = pos.getZ();
            this.volume = volume;
            this.pitch = pitch;
            this.repeat = false;
        }

        @Override
        public boolean isDonePlaying() {
            SoundManager sndMgr = ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.audio.SoundHandler.class,
                    Minecraft.getMinecraft().getSoundHandler(),
                    "sndManager");
            return !sndMgr.isSoundPlaying(this);
        }

        @Override
        public void update() {

        }
    }

    // We'll only set it to 1 so that it still a) sounds good and b) kind of suppresses the problem of words getting skipped
    public static final int MAX_TICK_COUNTER = 1;

    private Queue<String> words;
    private AnnouncementSound sound;
    private int tickCounter = 0; // Counter before the next word is played

    private World world;
    private String vox;
    private BlockPos pos;
    private float volume;
    private float pitch;

    public AnnouncementPlayer(BlockPos newPos, World newWorld) {
        words = new ArrayDeque<>();

        pos = newPos;
        world = newWorld;
    }

    public boolean canAnnounceWord(String word) {
        List<String> words = VOXResourcePack.getWordListForVOX(vox);
        if(words == null) return false;
        else return words.contains(word + ".ogg");
    }

    @SideOnly(Side.CLIENT)
    public void update() {
        // We should play a sound if we aren't already
        if(!words.isEmpty()) {
            // Are we done yet?
            if(sound != null && sound.isDonePlaying()) {
                FMLClientHandler.instance().getClient().getSoundHandler().stopSound(sound);
                sound = null;
                tickCounter = MAX_TICK_COUNTER;
            }

            // Are we currently playing a sound?
            if(sound == null && tickCounter <= 0) {
                String word = words.remove();
                // System.out.println("Client will be playing the sound file for " + word + ". Playing sound with volume=" + volume + " and pitch=" + pitch + "@" + pos);
                // System.out.println("Resource is " + getResourceForWord(word));
                sound = new AnnouncementSound(getResourceForWord(word), pos, volume, pitch);
                FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
            } else if(tickCounter > 0) {
                --tickCounter;
            }
        }
    }

    private ResourceLocation getResourceForWord(String word) {
        // System.out.println(word + " ---> " + (new ResourceLocation(OCAnnouncer.MODID, vox + "." + word)));
        return new ResourceLocation(OCAnnouncer.MODID, vox + "." + word);
    }

    public boolean isAnnouncing() {
        return !words.isEmpty();
    }

    public boolean isPlaying() {
        return sound != null;
    }

    public static boolean isCharacterSpecial(char c) {
        return String.valueOf(c).matches("[^A-Za-z0-9 ,\\.\\!\\@\\#\\%\\?]");
    }

    public List<String> splitMessage(String message) {
        List<String> messageWords = new ArrayList<>();
        String word = "";
        for(char c : message.toLowerCase().toCharArray()) {
            if(Character.isWhitespace(c)) {
                messageWords.add(word);

                word = "";
            } else if(c == ',') {
                messageWords.add(word);
                messageWords.add("_comma");
                word = "";
            } else if(c == '.') {
                messageWords.add(word);
                messageWords.add("_period");
                word = "";
            } else if(c == '!') {
                messageWords.add(word);
                messageWords.add("_exclaim");
                word = "";
            } else if(c == '?') {
                messageWords.add(word);
                messageWords.add("_question");
                word = "";
            } else if(c == '@') {
                messageWords.add(word);
                messageWords.add("_at");
                word = "";
            } else if(c == '#') {
                messageWords.add(word);
                messageWords.add("_number");
                word = "";
            } else if(c == '%') {
                messageWords.add(word);
                messageWords.add("_percent");
                word = "";
            } else if(!isCharacterSpecial(c)) { // Skip the character if it is special (we won't have files for things like ( or )
                word += c;
            }
        }

        if(!word.isEmpty())
            messageWords.add(word);

        return messageWords;
    }

    public void makeAnnouncement(String message) {
        List<String> messageWords = splitMessage(message);
        for(String s : messageWords) {
            if(!s.isEmpty() && canAnnounceWord(s)) {
                words.add(s);
            }
        }
    }

    public void setPos(BlockPos newPos) {
        pos = newPos;
    }

    public void setVolume(float newVolume) {
        volume = newVolume;
    }

    public void setPitch(float newPitch) {
        pitch = newPitch;
    }

    public void setVOX(String newVOX) {
        vox = newVOX;
    }

    public void setWorld(World newWorld) {
        world = newWorld;
    }
}
