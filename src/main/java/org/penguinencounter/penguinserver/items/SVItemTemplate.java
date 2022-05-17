package org.penguinencounter.penguinserver.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class SVItemTemplate {
    public boolean matches(ItemStack is) {
        return false;
    }
    public ItemStack fabricate(Item base) {
        return base.getDefaultStack();
    }
}
