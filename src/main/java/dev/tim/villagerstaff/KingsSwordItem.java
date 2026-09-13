package dev.tim.villagerstaff;

import java.util.UUID;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public final class KingsSwordItem extends Item {
    public static final int MARK_TICKS = 40 * 20;
    public static final int MARK_COOLDOWN = 35 * 20;
    public static final int TELEPORT_COOLDOWN = 50 * 20;
    public static final double RANGE = 32;
    private static final String TARGET = "villagerstaff.king_target";
    private static final String OWNER = "villagerstaff.king_owner";
    private static final String MARK_READY = "villagerstaff.mark_ready";
    private static final String TP_READY = "villagerstaff.tp_ready";

    public KingsSwordItem(Properties properties) {
        // Netherite adds 4 and a player has 1 base damage: 6 + 4 + 1 = 11.
        super(properties.sword(ToolMaterial.NETHERITE, 6, -2.4f)
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ServerLevel server = (ServerLevel) level;
        long now = server.getServer().overworld().getGameTime();
        boolean teleport = player.isShiftKeyDown();
        String key = teleport ? TP_READY : MARK_READY;
        long remaining = player.getPersistentData().getLongOr(key, 0) - now;
        if (remaining > 0) {
            player.displayClientMessage(Component.translatable("message.villagerstaff.sword_cooldown",
                (remaining + 19) / 20), true);
            return InteractionResult.FAIL;
        }
        if (teleport) {
            LivingEntity target = ownedTarget(server, player);
            if (target == null || !target.hasEffect(VillagerStaffMod.KINGS_TARGET.getHolder().orElseThrow())) {
                message(player, "no_target"); return InteractionResult.FAIL;
            }
            Vec3 spot = safeSpot(server, serverPlayer, target);
            if (spot == null || player.isPassenger()) {
                message(player, "no_safe_spot"); return InteractionResult.FAIL;
            }
            if (!serverPlayer.teleportTo(server, spot.x, spot.y, spot.z, Set.of(), player.getYRot(), player.getXRot(), true))
                return InteractionResult.FAIL;
            player.resetFallDistance();
            player.setDeltaMovement(Vec3.ZERO);
            player.getPersistentData().putLong(key, now + TELEPORT_COOLDOWN);
            server.sendParticles(ParticleTypes.PORTAL, spot.x, spot.y + 1, spot.z, 40, .4, .7, .4, .1);
            return InteractionResult.SUCCESS;
        }
        LivingEntity target = aimedTarget(server, player);
        if (target == null) { message(player, "aim_target"); return InteractionResult.FAIL; }
        var effect = VillagerStaffMod.KINGS_TARGET.getHolder().orElseThrow();
        if (!target.addEffect(new MobEffectInstance(effect, MARK_TICKS, 0, false, true, true), player)) {
            message(player, "mark_failed"); return InteractionResult.FAIL;
        }
        LivingEntity old = ownedTarget(server, player);
        if (old != null && old != target) old.removeEffect(effect);
        target.getPersistentData().putString(OWNER, player.getUUID().toString());
        player.getPersistentData().putString(TARGET, target.getUUID().toString());
        player.getPersistentData().putLong(key, now + MARK_COOLDOWN);
        server.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1, target.getZ(), 35, .4, .7, .4, .3);
        message(player, "marked");
        return InteractionResult.SUCCESS;
    }

    private static LivingEntity ownedTarget(ServerLevel level, Player player) {
        try {
            var entity = level.getEntity(UUID.fromString(player.getPersistentData().getStringOr(TARGET, "")));
            if (entity instanceof LivingEntity living && living.isAlive()
                && living.getPersistentData().getStringOr(OWNER, "").equals(player.getUUID().toString())) return living;
        } catch (IllegalArgumentException ignored) { }
        return null;
    }

    private static LivingEntity aimedTarget(ServerLevel level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        double closest = player.pick(RANGE, 1, false).getLocation().distanceToSqr(start);
        LivingEntity result = null;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                e -> e != player && e.isAlive() && !e.isSpectator())) {
            if (target instanceof Player other && !player.canHarmPlayer(other)) continue;
            var hit = target.getBoundingBox().inflate(.15).clip(start, end);
            if (hit.isPresent() && hit.get().distanceToSqr(start) < closest) {
                closest = hit.get().distanceToSqr(start); result = target;
            }
        }
        return result;
    }

    private static Vec3 safeSpot(ServerLevel level, Player player, LivingEntity target) {
        for (int dy : new int[]{0, 1, -1, 2, -2}) {
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8;
                double distance = target.getBbWidth() / 2 + 1.5;
                Vec3 spot = new Vec3(target.getX() + Math.cos(angle) * distance,
                    target.getY() + dy, target.getZ() + Math.sin(angle) * distance);
                BlockPos feet = BlockPos.containing(spot);
                var box = player.getBoundingBox().move(spot.subtract(player.position()));
                if (level.hasChunkAt(feet) && level.getWorldBorder().isWithinBounds(box)
                    && level.noCollision(player, box) && !level.containsAnyLiquid(box)
                    && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
                    && !level.getBlockState(feet).is(net.minecraft.tags.BlockTags.FIRE)
                    && !level.getBlockState(feet.below()).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)) return spot;
            }
        }
        return null;
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message.villagerstaff." + key), true);
    }
}
