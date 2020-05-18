package com.aflyingcar.oc_announcer.common.tileentity;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.aflyingcar.oc_announcer.client.AnnouncementPlayer;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class TileEntityAnnouncer extends TileEntityEnvironment implements ITickable {
    // Maximum distance (in blocks) that the announcer can be heard from
    public static final int MAX_VOLUME = 1024;
    public static final float MAX_PITCH = 2.0f;

    private AnnouncementPlayer announcer;

    private float volume = 1.0f;
    private float pitch = 1.0f;
    private String voxName = "";

    private Queue<String> messages = new ArrayDeque<>();

    public TileEntityAnnouncer() {
        node = Network.newNode(this, Visibility.Network).withConnector().withComponent("oc_announcer").create();
        announcer = new AnnouncementPlayer(getPos(), world);
    }

    @Callback(doc = "function(distance:integer):string; Sets the maximum distance the announcer can be heard from.", direct = true)
    public Object[] setVolume(Context ctx, Arguments args) {
        if(args.count() != 1) {
            return new Object[] { false, "Invalid number of arguments." };
        }

        int distance = args.checkInteger(0);
        if(distance < 0) distance = 0;
        else if(distance > MAX_VOLUME) distance = MAX_VOLUME;

        setVolume(distance);

        return new Object[] { true, distance };
    }

    @Callback(doc = "function(distance:integer):string; Sets the maximum distance the announcer can be heard from.", direct = true)
    public Object[] setPitch(Context ctx, Arguments args) {
        if(args.count() != 1) {
            return new Object[] { false, "Invalid number of arguments." };
        }

        float pitch = (float)args.checkDouble(0);
        if(pitch < 0) pitch = 0;
        else if(pitch > MAX_PITCH) pitch = MAX_PITCH;

        setPitch(pitch);

        return new Object[] { true, pitch };
    }

    @Callback(doc = "function(vox_name:string):string; Sets the VOX set to use.", direct = true)
    public Object[] setVOX(Context ctx, Arguments args) {
        if(args.count() != 1) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        String voxName = args.checkString(0);
        if(setVOX(voxName)) {
            return new Object[]{true};
        } else {
            return new Object[]{false, "VOX Name '" + voxName + "' does not exist." };
        }
    }

    @Callback(doc = "function(message:string):string; Says a message in the currently set VOX pack.")
    public Object[] announce(Context ctx, Arguments args) {
        if(args.count() != 1) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        String message = args.checkString(0);

        messages.add(message);

        // We only want to mark that we are dirty when announcing a message
        //  there is no point in us marking as dirty during any other operation.
        markDirty();
        if(world != null) {
            world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), 3);
        }

        return new Object[]{ true };
    }

    @Callback(doc = "function():array; Lists all VOX Packs.")
    public Object[] listVOX(Context ctx, Arguments args) {
        if(args.count() != 0) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        return new Object[]{ true, OCAnnouncer.getVOXSoundLists().keySet().toArray() };
    }

    @Callback(doc = "function():string; Gets the VOX pack currently in use.")
    public Object[] getVOX(Context ctx, Arguments args) {
        if(args.count() != 0) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        return new Object[]{ true, voxName };
    }

    @Callback(doc = "function():int; Gets the current volume, in blocks.")
    public Object[] getVolume(Context ctx, Arguments args) {
        if(args.count() != 0) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        return new Object[]{ true, volume };
    }

    @Callback(doc = "function():int; Gets the current pitch.")
    public Object[] getPitch(Context ctx, Arguments args) {
        if(args.count() != 0) {
            return new Object[]{ false, "Invalid number of arguments." };
        }

        return new Object[]{true, pitch};
    }

    // https://wiki.mcjty.eu/modding/index.php?title=Render_Block_TESR_/_OBJ-1.12
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("volume")) {
            volume = compound.getFloat("volume");
        }
        if(compound.hasKey("vox")) {
            voxName = compound.getString("vox");
        }
        if(compound.hasKey("message")) {
            messages.add(compound.getString("message"));
        }
        if(compound.hasKey("pitch")) {
            pitch = compound.getFloat("pitch");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setFloat("volume", volume);
        compound.setFloat("pitch", pitch);
        compound.setString("vox", voxName);

        // We will only send a message down if there is one to send
        if(!messages.isEmpty()) {
            compound.setString("message", messages.remove());
        }

        return compound;
    }

    private void setVolume(int distance) {
        volume = distance;
    }

    private void setPitch(float newPitch) {
        pitch = newPitch;
    }

    private boolean setVOX(String newVOXName) {
        if(OCAnnouncer.getVOXSoundLists().containsKey(newVOXName)) {
            voxName = newVOXName;
            return true;
        }

        return false;
    }

    @Override
    public void update() {
        if(node() != null && node().network() == null) {
            Network.joinOrCreateNetwork(this);
        }

        announcer.setWorld(getWorld());
        announcer.setPos(getPos());
        announcer.setVOX(voxName);
        announcer.setVolume(volume);
        announcer.setPitch(pitch);

        if(getWorld().isRemote) {
            if(!messages.isEmpty() && !announcer.isAnnouncing()) {
                announcer.makeAnnouncement(messages.remove());
            }
            announcer.update();
        }
    }
}
