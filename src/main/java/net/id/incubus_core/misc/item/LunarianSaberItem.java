package net.id.incubus_core.misc.item;

import net.id.incubus_core.misc.*;
import net.minecraft.client.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.particle.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class LunarianSaberItem extends SwordItem {

    public LunarianSaberItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        World world = target.getWorld();
        Random random = target.getRandom();

        if(!WorthinessChecker.isPlayerWorthy(attacker.getUuid(), attacker instanceof PlayerEntity player ? Optional.of(player) : Optional.empty())) {
            WorthinessChecker.smite(attacker);
        }

        if(target.isUndead()) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1), attacker);
        }
        else {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1), attacker);
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1), attacker);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2), attacker);

        Box bounds = target.getBoundingBox(target.getPose());

        if(attacker.getRandom().nextFloat() <= (target.getHealth() / target.getMaxHealth()) / 2) {
            StatusEffectInstance effect = attacker.getStatusEffect(StatusEffects.HEALTH_BOOST);
            if(effect != null) {
                if(effect.getAmplifier() < 4) {
                    attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, effect.getAmplifier() + 1), attacker);
                }
            }
            else {
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 0), attacker);
            }

            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 4));

            target.damage(target.getDamageSources().generic(), target.getMaxHealth() / 10 + 1);
            world.playSoundFromEntity(null, attacker, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 2F, 2F);

            if(!world.isClient()) {
                for (int i = 0; i < Math.pow(bounds.getAverageSideLength() * 2, 2); i++) {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, target.getX() + (random.nextDouble() * bounds.getXLength() - bounds.getXLength() / 2), target.getY() + (random.nextDouble() * bounds.getYLength()), target.getZ() + (random.nextDouble() * bounds.getZLength() - bounds.getZLength() / 2), random.nextInt(4), 0, 0, 0, 0.085);
                }
            }
        }

        target.timeUntilRegen = 0;

        return true;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand) {
        Box bounds = target.getBoundingBox(target.getPose());
        Random random = target.getRandom();
        World world = target.getWorld();

        target.clearStatusEffects();

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));

        if(!world.isClient()) {
            for (int i = 0; i < Math.pow(bounds.getAverageSideLength() * 2, 1.5); i++) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + bounds.getYLength() / 2, target.getZ(), random.nextInt(4) + 2, 0, 0, 0, 0.145);
            }
        }

        world.playSoundFromEntity(null, target, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.5F, 2F);

        return ActionResult.success(user.getWorld().isClient());
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§f§oFor The Worthy"));
        tooltip.add(Text.literal("§b§olook to la luna"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
