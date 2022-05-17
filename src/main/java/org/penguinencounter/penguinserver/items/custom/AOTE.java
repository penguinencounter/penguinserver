package org.penguinencounter.penguinserver.items.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.penguinencounter.penguinserver.Penguinserver;
import org.penguinencounter.penguinserver.items.ItemLoreUtilities;
import org.penguinencounter.penguinserver.items.SVItemAction;
import org.penguinencounter.penguinserver.items.SVItemTemplate;

import java.util.ArrayList;
import java.util.UUID;

public class AOTE {
    public static class AOTEAction extends SVItemAction {
        ArrayList<UUID> usedThisTick = new ArrayList<>();
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            if (!usedThisTick.contains(user.getUuid())) {
                HitResult raycast = user.raycast(9, 0, false);
                Vec3d result = raycast.getPos();
                if (raycast.getPos().squaredDistanceTo(user.getPos()) < 1) {
                    user.sendMessage(new LiteralText("There are blocks in the way!").formatted(Formatting.RED), false);
                } else {
                    if (raycast.getType() == HitResult.Type.BLOCK) {
                        user.sendMessage(new LiteralText("There are blocks in the way!").formatted(Formatting.GOLD), false);
                    }
                    user.teleport(result.x, result.y, result.z);
                    user.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                usedThisTick.add(user.getUuid());
                return TypedActionResult.success(user.getStackInHand(hand));
            } else {
                Penguinserver.LOGGER.warn("User " + user.getName().asString() + " tried to use AOTE multiple times in a tick!");
                return TypedActionResult.pass(user.getStackInHand(hand));
            }
        }

        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
            if (context.getPlayer() == null) {
                return ActionResult.PASS;
            }
            return use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
        }

        @Override
        public void tick(MinecraftServer server) {
            usedThisTick.clear();
        }
    }

    public static class AOTETemplate extends SVItemTemplate {
        @Override
        public boolean matches(ItemStack is) {
            if (is.getNbt() != null) {
                return is.getNbt().contains("AOTE");
            } else return false;
        }

        @Override
        public ItemStack fabricate(Item base) {
            NbtCompound customData = new NbtCompound();
            customData.putByte("AOTE", (byte) 1);
            ItemStack result = base.getDefaultStack();
            result.setNbt(customData);

            ItemLoreUtilities ilu = new ItemLoreUtilities();
            ilu.replaceName("Aspect of the End", Style.EMPTY.withItalic(false).withColor(Formatting.BLUE));
            ilu.addLore(
                    new ItemLoreUtilities.LoreLine("Ability: Instant Transmission ", Style.EMPTY.withColor(Formatting.GOLD).withBold(false).withItalic(false))
                            .addText("RIGHT CLICK ", Style.EMPTY.withColor(Formatting.YELLOW).withBold(true).withItalic(false))
            );
            ilu.addLore(
                    new ItemLoreUtilities.LoreLine("WIP", Style.EMPTY.withColor(Formatting.RED).withBold(false).withItalic(false))
            );
            ilu.addLore(
                    ItemLoreUtilities.LoreLine.EMPTY
            );
            ilu.addLore(
                    new ItemLoreUtilities.LoreLine("RARE", Style.EMPTY.withColor(Formatting.BLUE).withBold(false).withItalic(false))
            );

            ilu.setEnchantGlint(false);
            ilu.applyTo(result);
            return result;
        }
    }
}
