package dev.tim.villagerstaff;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class VillagerStaffItem extends Item {
    public static final double RANGE = 48;
    public static final double RADIUS = 10;
    public static final float DAMAGE = 16;
    public static final int COOLDOWN_TICKS = 900;

    // Vanilla explosion power is not a geometric radius. Bound terrain damage
    // explicitly, and retain the staff's separate, fixed-radius entity damage.
    private static final ExplosionDamageCalculator TERRAIN_EXPLOSION = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos,
                BlockState state, float power) {
            return Vec3.atCenterOf(pos).distanceToSqr(explosion.center()) <= RADIUS * RADIUS;
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) { return false; }

        @Override
        public float getKnockbackMultiplier(Entity entity) { return 0; }
    };

    public VillagerStaffItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        Vec3 eye = player.getEyePosition();
        var hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(RANGE)),
            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.villagerstaff.aim"), true);
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            Vec3 center = hit.getLocation().add(0, 0.15, 0);
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(RADIUS),
                    entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player))) {
                if (target.getBoundingBox().distanceToSqr(center) > RADIUS * RADIUS) continue;
                // Respect server PvP rules, creative invulnerability and damage hooks.
                if (target instanceof Player other && !player.canHarmPlayer(other)) continue;
                target.hurtServer(server, server.damageSources().indirectMagic(player, player), DAMAGE);
            }
            server.explode(player, server.damageSources().explosion(player, player), TERRAIN_EXPLOSION,
                center, (float) RADIUS, false, Level.ExplosionInteraction.BLOCK);
            for (double y = 0; y <= 40; y += 0.5) {
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y + y, center.z, 3, 0.10, 0.15, 0.10, 0.01);
            }
            for (int i = 0; i < 48; i++) {
                double angle = 2 * Math.PI * i / 48;
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    center.x + Math.cos(angle) * RADIUS, center.y + 0.1, center.z + Math.sin(angle) * RADIUS,
                    1, 0, 0.1, 0, 0);
            }
            server.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1f, 0.85f, 0.2f),
                center.x, center.y, center.z, 1, 0, 0, 0, 0);
            server.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.3f);
        }
        return InteractionResult.SUCCESS;
    }
}
