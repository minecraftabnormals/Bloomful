package com.pugz.bloomful.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class WisteriaLeavesBlock extends Block implements net.minecraftforge.common.IShearable {

    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 9);

    public WisteriaLeavesBlock(Block.Properties properties) {
        super(properties);
        setDefaultState(stateContainer.getBaseState().with(DISTANCE, 9));
    }

    public boolean ticksRandomly(BlockState state) {
        return state.get(DISTANCE) == 9;
    }

    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (state.get(DISTANCE) == 9) {
            spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }
    }

    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        worldIn.setBlockState(pos, updateDistance(state, worldIn, pos), 3);
    }

    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1;
    }

    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || stateIn.get(DISTANCE) != i) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }
        return stateIn;
    }

    private static BlockState updateDistance(BlockState state, IWorld world, BlockPos pos) {
        int i = 8;
        try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
            for(Direction direction : Direction.values()) {
                blockpos$pooledmutableblockpos.setPos(pos).move(direction);
                i = Math.min(i, getDistance(world.getBlockState(blockpos$pooledmutableblockpos)) + 1);
                if (i == 1) {
                    break;
                }
            }
        }
        return state.with(DISTANCE, i);
    }

    private static int getDistance(BlockState neighbor) {
        if (BlockTags.LOGS.contains(neighbor.getBlock())) {
            return 0;
        } else {
            return neighbor.getBlock() instanceof WisteriaLeavesBlock ? neighbor.get(DISTANCE) : 9;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up())) {
            if (rand.nextInt(15) == 1) {
                BlockPos blockpos = pos.down();
                BlockState blockstate = worldIn.getBlockState(blockpos);
                if (!blockstate.isSolid() || !blockstate.func_224755_d(worldIn, blockpos, Direction.UP)) {
                    double d0 = (double)((float)pos.getX() + rand.nextFloat());
                    double d1 = (double)pos.getY() - 0.05D;
                    double d2 = (double)((float)pos.getZ() + rand.nextFloat());
                    worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public boolean isSolid(BlockState state) {
        return false;
    }

    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE);
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return updateDistance(getDefaultState(), context.getWorld(), context.getPos());
    }
}