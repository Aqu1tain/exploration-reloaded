package com.akitain.explorationreloaded.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (context.isAbove(VoxelShapes.fullCube(), pos, true) && !context.isDescending()) {
                return super.getCollisionShape(state, world, pos, context);
            }
            if (entity != null && (entity.hasControllingPassenger() || entity instanceof MobEntity mob && mob.isLeashed())) {
                return VoxelShapes.empty();
            }
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null && entity.getControllingVehicle() != null) {
                return VoxelShapes.empty();
            }
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean notify) {
        if (entity.hasControllingPassenger()) {
            entity.slowMovement(state, new Vec3d(0.85, 1, 0.85));
        }
    }
}
