package org.penguinencounter.penguinserver.items;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SemiVanillaItem {
    public static HashMap<Identifier, SemiVanillaItem> REGISTRY = new HashMap<>();
    private final SVItemTemplate template;

    public Item base;
    public ArrayList<SVItemAction> actions = new ArrayList<>();

    public SemiVanillaItem(Item base, SVItemTemplate svItemTemplate) {
        this.base = base;
        this.template = svItemTemplate;
    }

    public void addAction(SVItemAction svia) {
        actions.add(svia);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> tar = TypedActionResult.pass(user.getStackInHand(hand));
        for (SVItemAction action : actions) {
            tar = action.use(world, user, hand);
            if (tar.getResult() != ActionResult.PASS) {
                return tar;
            }
        }
        return tar;
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult ar = ActionResult.PASS;
        for (SVItemAction action : actions) {
            ar = action.useOnBlock(context);
            if (ar != ActionResult.PASS) {
                return ar;
            }
        }
        return ar;
    }
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        for (SVItemAction action : actions) {
            boolean br = action.postHit(stack, target, attacker);
            if (br) {
                return true;
            }
        }
        return false;
    }

    public void tickActions(MinecraftServer server) {
        for (SVItemAction action : actions) {
            action.tick(server);
        }
    }
    public static void tickAllActions(MinecraftServer server) {
        for (Map.Entry<Identifier, SemiVanillaItem> entry : REGISTRY.entrySet()) {
            SemiVanillaItem cItem = entry.getValue();
            cItem.tickActions(server);
        }
    }


    public static class TARFindResult {
        public boolean ok;
        public TypedActionResult<ItemStack> result;
        public TARFindResult(boolean ok, TypedActionResult<ItemStack> result) {
            this.ok = ok;
            this.result = result;
        }
        public TARFindResult(boolean ok) {
            this.ok = ok;
            this.result = null;
        }
    }
    public static class ARFindResult {
        public boolean ok;
        public ActionResult result;
        public ARFindResult(boolean ok, ActionResult result) {
            this.ok = ok;
            this.result = result;
        }
        public ARFindResult(boolean ok) {
            this.ok = ok;
            this.result = null;
        }
    }
    public static class BFindResult {
        public boolean ok;
        public boolean result;
        public BFindResult(boolean ok, boolean result) {
            this.ok = ok;
            this.result = result;
        }
        public BFindResult(boolean ok) {
            this.ok = ok;
            this.result = false;
        }
    }

    /**
     * sort through registry and apply the appropriate "use" method
     * @param base ItemStack in use
     * @return Result of use
     */
    public static TARFindResult useAll(ItemStack base, World world, PlayerEntity user, Hand hand) {
        for (Map.Entry<Identifier, SemiVanillaItem> entry : REGISTRY.entrySet()) {
            SemiVanillaItem cItem = entry.getValue();
            if (cItem.base == base.getItem() && cItem.template.matches(base)) {
                return new TARFindResult(true, cItem.use(world, user, hand));
            }
        }
        return new TARFindResult(false);
    }

    public static ARFindResult useOnBlockAll(ItemStack base, ItemUsageContext context) {
        for (Map.Entry<Identifier, SemiVanillaItem> entry : REGISTRY.entrySet()) {
            SemiVanillaItem cItem = entry.getValue();
            if (cItem.base == base.getItem() && cItem.template.matches(base)) {
                return new ARFindResult(true, cItem.useOnBlock(context));
            }
        }
        return new ARFindResult(false);
    }

    public static BFindResult postHitAll(ItemStack base, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        for (Map.Entry<Identifier, SemiVanillaItem> entry : REGISTRY.entrySet()) {
            SemiVanillaItem cItem = entry.getValue();
            if (cItem.base == base.getItem() && cItem.template.matches(base)) {
                return new BFindResult(true, cItem.postHit(stack, target, attacker));
            }
        }
        return new BFindResult(false);
    }

    public ItemStack fabricate() {
        return template.fabricate(base);
    }
    public void giveItem(PlayerEntity player) {
        player.giveItemStack(fabricate());
    }
    public static LiteralArgumentBuilder<ServerCommandSource> registerCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        for (Map.Entry<Identifier, SemiVanillaItem> entry : REGISTRY.entrySet()) {
            Identifier id = entry.getKey();
            SemiVanillaItem item = entry.getValue();
            builder.then(CommandManager.literal(id.toString()).executes(ctx -> {
                item.giveItem(ctx.getSource().getPlayer());
                return 1;
            }));
        }
        return builder;
    }
}
