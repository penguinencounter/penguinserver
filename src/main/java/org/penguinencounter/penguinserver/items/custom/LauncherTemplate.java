package org.penguinencounter.penguinserver.items.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.penguinencounter.penguinserver.items.ItemLoreUtilities;
import org.penguinencounter.penguinserver.items.SVItemTemplate;
import org.penguinencounter.penguinserver.items.SemiVanillaItem;

public class LauncherTemplate extends SVItemTemplate {
    @Override
    public boolean matches(ItemStack is) {
        if (is.getNbt() != null) {
            return is.getNbt().contains("ElytraLauncher");
        } else return false;
    }

    @Override
    public ItemStack fabricate(Item base) {
        NbtCompound customData = new NbtCompound();
        customData.putByte("ElytraLauncher", (byte) 1);
        ItemStack result = base.getDefaultStack();
        result.setNbt(customData);

        ItemLoreUtilities ilu = new ItemLoreUtilities();
        ilu.replaceName("Elyta Launcher", Style.EMPTY.withItalic(false).withColor(Formatting.GREEN));
        ilu.addLore(
                new ItemLoreUtilities.LoreLine("Ability: Launch ", Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(false))
                .addText("RIGHT CLICK", Style.EMPTY.withItalic(true).withColor(Formatting.YELLOW).withBold(true))
        );
        ilu.addLore(
                new ItemLoreUtilities.LoreLine("Launches the player into the air.", Style.EMPTY.withItalic(false).withColor(Formatting.WHITE))
        );
        ilu.addLore(
                new ItemLoreUtilities.LoreLine("Item is consumed", Style.EMPTY.withItalic(false).withColor(Formatting.DARK_GRAY))
        );
        ilu.addLore(
                new ItemLoreUtilities.LoreLine("Only usable while grounded", Style.EMPTY.withItalic(false).withColor(Formatting.DARK_GRAY))
        );
        ilu.addLore(ItemLoreUtilities.LoreLine.EMPTY);
        ilu.addLore(
                new ItemLoreUtilities.LoreLine("UNCOMMON", Style.EMPTY.withColor(Formatting.GREEN).withBold(true).withItalic(false))
        );
        ilu.setEnchantGlint(true);
        ilu.applyTo(result);
        return result;
    }
}
