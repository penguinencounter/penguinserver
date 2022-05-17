package org.penguinencounter.penguinserver.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ItemLoreUtilities {
    public static class LoreLine {
        public ArrayList<String> texts;
        public ArrayList<Style> styles;
        public static LoreLine EMPTY = new LoreLine();
        public LoreLine() {
            texts = new ArrayList<>();
            styles = new ArrayList<>();
        }
        public LoreLine(String text, Style style) {
            this();
            texts.add(text);
            styles.add(style);
        }

        public LoreLine addText(String text, Style style) {
            texts.add(text);
            styles.add(style);
            return this;
        }

        public Text compile() {
            if (texts.size() == 0) {
                return new LiteralText("");
            }
            MutableText builder = null;
            for (int i = 0; i < texts.size(); i++) {
                String t = texts.get(i);
                Style s = styles.get(i);
                if (builder == null) {
                    builder = new LiteralText(t).setStyle(s);
                } else {
                    builder.append(new LiteralText(t).setStyle(s));
                }
            }
            return builder;
        }
    }
    public boolean enchantGlint = false;
    public Text itemNameResult;
    public String itemName;
    public Style itemNameStyle;
    public List<Text> loreResult;
    public ArrayList<LoreLine> lore;
    public ItemLoreUtilities() {
        itemName = null;
        itemNameStyle = null;
        lore = new ArrayList<>();
        generate();
    }

    public void generate() {
        if (itemName == null) {
            itemNameResult = null;
        } else if (itemNameStyle != null) {
            itemNameResult = new LiteralText(itemName).setStyle(itemNameStyle);
        } else {
            itemNameResult = new LiteralText(itemName);
        }
        ArrayList<Text> building = new ArrayList<>();
        int i = 0;
        for (;i<lore.size();i++) {
            building.add(lore.get(i).compile());
        }
        loreResult = building.stream().toList();
    }

    public void replaceName(String text, Style style) {
        itemName = text;
        itemNameStyle = style;
    }

    public void clearLore() {
        lore.clear();
    }

    public void addLore(LoreLine line) {
        lore.add(line);
    }

    public void setEnchantGlint(boolean state) {
        enchantGlint = state;
    }

    public void applyTo(ItemStack is) {
        generate();
        is.setCustomName(itemNameResult);
        NbtCompound nbtCompound = is.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);
        if (loreResult.size() == 0) {
            nbtCompound.remove(ItemStack.LORE_KEY);
        } else {
            NbtList parts = new NbtList();
            for (Text t : loreResult) {
                parts.add(NbtString.of(Text.Serializer.toJson(t)));
            }
            nbtCompound.put(ItemStack.LORE_KEY, parts);
        }

        if (enchantGlint) {
            if (is.getNbt() != null && !is.getNbt().contains(ItemStack.ENCHANTMENTS_KEY)) {
                // if the list has > 0 elements, then it'll have the glint
                NbtList filler = new NbtList();
                filler.add(new NbtCompound());
                is.getNbt().put(ItemStack.ENCHANTMENTS_KEY, filler);
            }
        }
    }
}
