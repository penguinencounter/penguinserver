package org.penguinencounter.penguinserver;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import org.penguinencounter.penguinserver.items.SemiVanillaItem;
import org.penguinencounter.penguinserver.items.custom.AOTE;
import org.penguinencounter.penguinserver.items.custom.LauncherAction;
import org.penguinencounter.penguinserver.items.custom.LauncherTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Penguinserver implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("PenguinServer");

    @Override
    public void onInitialize() {
        SemiVanillaItem launcher = new SemiVanillaItem(Items.FIREWORK_ROCKET, new LauncherTemplate());
        launcher.addAction(new LauncherAction());
        SemiVanillaItem.REGISTRY.put(new Identifier("penguinserver", "launcher"), launcher);

        SemiVanillaItem aote = new SemiVanillaItem(Items.DIAMOND_SWORD, new AOTE.AOTETemplate());
        aote.addAction(new AOTE.AOTEAction());
        SemiVanillaItem.REGISTRY.put(new Identifier("penguinserver", "aote"), aote);

        // hooks + command registration
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(SemiVanillaItem.registerCommand(CommandManager.literal("ci")))));
        ServerTickEvents.END_SERVER_TICK.register(SemiVanillaItem::tickAllActions);
    }
}
