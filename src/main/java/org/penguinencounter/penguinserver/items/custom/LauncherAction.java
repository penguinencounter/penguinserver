package org.penguinencounter.penguinserver.items.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.penguinencounter.penguinserver.Penguinserver;
import org.penguinencounter.penguinserver.items.SVItemAction;

public class LauncherAction extends SVItemAction {
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return super.use(world, user, hand);
        Penguinserver.LOGGER.info("Got hops");
//        if (Penguinserver.modVelOnNextTick.replace(user.getUuid(), new Vec3d(0, 1, 0)) == null) { // new cheesy way to do it
//            Penguinserver.modVelOnNextTick.put(user.getUuid(), new Vec3d(0, 1, 0));
//        }
        if (user.isOnGround()) {
            user.addVelocity(0, 2, 0);
            user.velocityModified = true;
            user.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 10, 1);
            int resultingCount = user.getStackInHand(hand).getCount() - 1;
            user.getStackInHand(hand).setCount(resultingCount);
            return TypedActionResult.consume(user.getStackInHand(hand));
        } else {
            final int noteNum = 0;
            float f = (float)Math.pow(2.0, (double)(noteNum - 12) / 12.0);
            user.playSound(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 10, f);
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
    }
}
