package com.example.watergun;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class WaterGunItem extends Item {
    private static final double RANGE = 12.0;

    public WaterGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d start = player.getEyePos();
            Vec3d dir = player.getRotationVec(1.0F);
            Vec3d end = start.add(dir.multiply(RANGE));

            BlockHitResult bhit = world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
            double maxDist = bhit.getType() == HitResult.Type.MISS ? RANGE : bhit.getPos().distanceTo(start);

            Box box = player.getBoundingBox().stretch(dir.multiply(maxDist)).expand(1.0);
            EntityHitResult ehit = ProjectileUtil.raycast(player, start, start.add(dir.multiply(maxDist)),
                    box, e -> !e.isSpectator() && e.canHit(), maxDist * maxDist);

            if (ehit != null) {
                maxDist = ehit.getPos().distanceTo(start);
                Entity target = ehit.getEntity();
                target.extinguish();
                target.addVelocity(dir.x * 0.3, 0.1, dir.z * 0.3);
                target.velocityModified = true;
                if (target instanceof BlazeEntity || target instanceof EndermanEntity) {
                    target.damage(sw.getDamageSources().generic(), 1.0F);
                }
                if (target instanceof LivingEntity) {
                    sw.spawnParticles(ParticleTypes.SPLASH, ehit.getPos().x, ehit.getPos().y, ehit.getPos().z,
                            15, 0.2, 0.2, 0.2, 0.1);
                }
            } else if (bhit.getType() == HitResult.Type.BLOCK) {
                BlockPos firePos = bhit.getBlockPos().offset(bhit.getSide());
                if (world.getBlockState(firePos).isIn(BlockTags.FIRE)) {
                    world.removeBlock(firePos, false);
                    world.playSound(null, firePos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.7F, 1.0F);
                }
                sw.spawnParticles(ParticleTypes.SPLASH, bhit.getPos().x, bhit.getPos().y, bhit.getPos().z,
                        10, 0.15, 0.15, 0.15, 0.05);
            }

            for (double d = 0.8; d < maxDist; d += 0.4) {
                Vec3d p = start.add(dir.multiply(d)).add(0, -0.15, 0);
                sw.spawnParticles(ParticleTypes.FALLING_WATER, p.x, p.y, p.z, 2, 0.04, 0.04, 0.04, 0.0);
                sw.spawnParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.PLAYERS, 0.4F, 1.6F);
        }

        player.getItemCooldownManager().set(this, 4);
        player.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }
}
