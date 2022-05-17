package org.penguinencounter.penguinserver.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.penguinencounter.penguinserver.Penguinserver;
import org.penguinencounter.penguinserver.items.SemiVanillaItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ItemBindMixins {
    @Mixin(Item.class)
    public static class SVItemBinder {
        @Inject(
                method = "use",
                at = @At("HEAD"),
                cancellable = true
        )
        void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
            SemiVanillaItem.TARFindResult fr = SemiVanillaItem.useAll(user.getStackInHand(hand), world, user, hand);
            if (fr.ok) {
                Penguinserver.LOGGER.info("clicked - matched");
                cir.setReturnValue(fr.result);
            } else {
                Penguinserver.LOGGER.info("clicked - did not match");
            }
        }

        @Inject(
                method = "useOnBlock",
                at = @At("HEAD"),
                cancellable = true
        )
        void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
            SemiVanillaItem.ARFindResult fr = SemiVanillaItem.useOnBlockAll(context.getStack(), context);
            if (fr.ok) {
                Penguinserver.LOGGER.info("clicked [on block] - matched");
                cir.setReturnValue(fr.result);
            } else {
                Penguinserver.LOGGER.info("clicked [on block] - did not match");
            }
        }

        @Inject(
                method = "postHit",
                at = @At("HEAD"),
                cancellable = true
        )
        void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
            SemiVanillaItem.BFindResult fr = SemiVanillaItem.postHitAll(stack, stack, target, attacker);
            if (fr.ok) {
                cir.setReturnValue(fr.result);
            }
        }
    }
    @Mixin(FireworkRocketItem.class)
    public static class SVItemBinder2 {
        @Inject(
                method = "use",
                at = @At("HEAD"),
                cancellable = true
        )
        void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
            SemiVanillaItem.TARFindResult fr = SemiVanillaItem.useAll(user.getStackInHand(hand), world, user, hand);
            if (fr.ok) {
                Penguinserver.LOGGER.info("clicked - matched");
                cir.setReturnValue(fr.result);
            } else {
                Penguinserver.LOGGER.info("clicked - did not match");
            }
        }

        @Inject(
                method = "useOnBlock",
                at = @At("HEAD"),
                cancellable = true
        )
        void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
            SemiVanillaItem.ARFindResult fr = SemiVanillaItem.useOnBlockAll(context.getStack(), context);
            if (fr.ok) {
                Penguinserver.LOGGER.info("clicked [on block] - matched");
                cir.setReturnValue(fr.result);
            } else {
                Penguinserver.LOGGER.info("clicked [on block] - did not match");
            }
        }
    }

    @Mixin(SwordItem.class)
    public static class SVItemBinder3 {
        @Inject(
                method = "postHit",
                at = @At("HEAD"),
                cancellable = true
        )
        void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
            SemiVanillaItem.BFindResult fr = SemiVanillaItem.postHitAll(stack, stack, target, attacker);
            if (fr.ok) {
                cir.setReturnValue(fr.result);
            }
        }
    }
}
