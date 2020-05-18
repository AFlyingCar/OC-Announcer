package com.aflyingcar.oc_announcer.common.block;

import com.aflyingcar.oc_announcer.OCAnnouncer;
import com.aflyingcar.oc_announcer.common.tileentity.TileEntityAnnouncer;
import li.cil.oc.api.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAnnouncer extends Block implements ITileEntityProvider {
    public static final PropertyDirection PROPERTY_FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static String NAME = "announcer";

    public BlockAnnouncer() {
        super(Material.IRON);
        setUnlocalizedName("oc_announcer." + NAME);
        setRegistryName(OCAnnouncer.MODID, NAME);
        setHardness(0.5f);
        setCreativeTab(CreativeTab.instance);
        setDefaultState(this.blockState.getBaseState().withProperty(PROPERTY_FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta);
        return getDefaultState().withProperty(PROPERTY_FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY_FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROPERTY_FACING);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(PROPERTY_FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityAnnouncer();
    }
}
