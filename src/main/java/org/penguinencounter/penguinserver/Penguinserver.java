package org.penguinencounter.penguinserver;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.penguinencounter.penguinserver.fplib.FakePlayer;
import org.penguinencounter.penguinserver.fplib.FakePlayerUtil;
import org.penguinencounter.penguinserver.fplib.ListFlasher;
import org.penguinencounter.penguinserver.items.SemiVanillaItem;
import org.penguinencounter.penguinserver.items.custom.AOTE;
import org.penguinencounter.penguinserver.items.custom.LauncherAction;
import org.penguinencounter.penguinserver.items.custom.LauncherTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Penguinserver implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("PenguinServer");
    public static MinecraftServer server;
    private static final HashMap<String, String> name_to_addr = new HashMap<>();
    public static FakePlayerUtil fpu;


    @Override
    public void onInitialize() {
        SemiVanillaItem launcher = new SemiVanillaItem(Items.FIREWORK_ROCKET, new LauncherTemplate());
        launcher.addAction(new LauncherAction());
        SemiVanillaItem.REGISTRY.put(new Identifier("penguinserver", "launcher"), launcher);

        SemiVanillaItem aote = new SemiVanillaItem(Items.DIAMOND_SWORD, new AOTE.AOTETemplate());
        aote.addAction(new AOTE.AOTEAction());
        SemiVanillaItem.REGISTRY.put(new Identifier("penguinserver", "aote"), aote);

        fpu = new FakePlayerUtil();

        // hooks + command registration
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(SemiVanillaItem.registerCommand(CommandManager.literal("ci")))));
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> dispatcher.register(
            CommandManager.literal("fpu")
                .then(CommandManager.literal("time").then(
                    CommandManager.argument("ticks", IntegerArgumentType.integer()).executes(
                        ctx -> {
                            ListFlasher.DELAY = ctx.getArgument("ticks", Integer.class);
                            ctx.getSource().getPlayer().sendMessage(new LiteralText("Flash time set to " + ListFlasher.DELAY + " ticks").formatted(Formatting.GREEN), false);
                            return 1;
                        }
                    )
                ))
                .then(CommandManager.literal("reload").executes(
                    ctx -> {
                        ctx.getSource().getPlayer().sendMessage(new LiteralText("Reloading...").formatted(Formatting.RED), false);
                        fpu.refreshAll();
                        return 1;
                    }
                ))
                .then(CommandManager.literal("softreload").executes(
                    ctx -> {
                        ctx.getSource().getPlayer().sendMessage(new LiteralText("Soft reload...").formatted(Formatting.RED), false);
                        fpu.fakes.forEach(fp -> {
                            fp.updatePositionAll();
                            fp.updateSkinLayersAll();
                        });
                        return 1;
                    }
                ))
                .then(CommandManager.literal("reset").executes(
                    ctx -> {
                        fpu.destroyAll();
                        fpu.clearFakes();
                        createFPs();
                        return 1;
                    }
                ))
        )));
        ServerTickEvents.END_SERVER_TICK.register(SemiVanillaItem::tickAllActions);
        ServerTickEvents.START_SERVER_TICK.register(ListFlasher::tickAll);
        ServerTickEvents.START_SERVER_TICK.register(s -> fpu.autorun());
        ServerTickEvents.START_SERVER_TICK.register(s -> {
        });

        ServerLifecycleEvents.SERVER_STARTING.register((server1 -> server = server1));
        ServerLifecycleEvents.SERVER_STARTED.register(s -> createFPs());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            LOGGER.info(handler.getPlayer().getName().asString() + " = " + handler.getConnection().getAddress());
            if (name_to_addr.containsKey(handler.getConnection().getAddress().toString())) {
                name_to_addr.replace(handler.getConnection().getAddress().toString(), handler.getPlayer().getName().asString());
            } else {
                name_to_addr.put(handler.getConnection().getAddress().toString(), handler.getPlayer().getName().asString());
            }
            // schedule a reload 20t afterward
            fpu.reloadSchedule.put(handler.getPlayer().getUuid(), 20);
        });
    }

    private static void createFPs() {
        FakePlayer fp1 = fpu.join(UUID.randomUUID(), "", null, new Vec3d(0.5, 56.5, 16.5), 0, -90);
        FakePlayerUtil.setSkinProfileFromFile(fp1.profile, "1b08145e-856d-4364-8bec-dbff32600609-0.txt");
        FakePlayer fp2 = fpu.join(UUID.randomUUID(), "", null, new Vec3d(0.5, 56.5, 19.5), 0, -90);
        FakePlayerUtil.setSkinProfileFromFile(fp2.profile, "1b08145e-856d-4364-8bec-dbff32600609-1.txt");
    }

    public static void pfl(Packet<?> packet, String boundTo) {
        String name = packet.getClass().getName();
        if (name.contains("S2C")) {  // paranoia
            final List<String> ignorable = List.of(
                "net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket",
                "net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket",
                "Packet sent: net.minecraft.network.packet.s2c.play.EntityS2CPacket$MoveRelative"
            );
            if (packet instanceof PlayerSpawnS2CPacket psp) {
                Penguinserver.LOGGER.info("INSPECTION on SPAWN packet:");
                Penguinserver.LOGGER.info("  Player UUID: " + psp.getPlayerUuid());
                Penguinserver.LOGGER.info("  ID: " + psp.getId());
                Penguinserver.LOGGER.info("  To: " + name_to_addr.get(boundTo));
            } else if (packet instanceof PlayerSpawnPositionS2CPacket pspp) {
                Penguinserver.LOGGER.info("INSPECTION on SPAWNPOS packet:");
                Penguinserver.LOGGER.info("  POS: " + pspp.getPos());
                Penguinserver.LOGGER.info("  ANGLE: " + pspp.getAngle());
                Penguinserver.LOGGER.info("  To: " + name_to_addr.get(boundTo));
            } else if (packet instanceof EntityTrackerUpdateS2CPacket etup) {
                Penguinserver.LOGGER.info("Entity tracker update packet");
                if (etup.getTrackedValues() == null) return;
                for (DataTracker.Entry<?> entry : etup.getTrackedValues()) {
                    TrackedDataHandler<?> type = entry.getData().getType();
                    LOGGER.info("  " + entry.get() + " (" + entry.getData().getId() + "):");
                    if (type == TrackedDataHandlerRegistry.BYTE) LOGGER.info("    type BYTE");
                    else if (type == TrackedDataHandlerRegistry.INTEGER) LOGGER.info("    type INTEGER");
                    else if (type == TrackedDataHandlerRegistry.FLOAT) LOGGER.info("    type FLOAT");
                    else if (type == TrackedDataHandlerRegistry.STRING) LOGGER.info("    type STRING");
                    else if (type == TrackedDataHandlerRegistry.TEXT_COMPONENT) LOGGER.info("    type TEXT_COMPONENT");
                    else if (type == TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT)
                        LOGGER.info("    type OPTIONAL_TEXT_COMPONENT");
                    else if (type == TrackedDataHandlerRegistry.ITEM_STACK) LOGGER.info("    type ITEM_STACK");
                    else if (type == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE)
                        LOGGER.info("    type OPTIONAL_BLOCK_STATE");
                    else if (type == TrackedDataHandlerRegistry.BOOLEAN) LOGGER.info("    type BOOLEAN");
                    else if (type == TrackedDataHandlerRegistry.PARTICLE) LOGGER.info("    type PARTICLE");
                    else if (type == TrackedDataHandlerRegistry.ROTATION) LOGGER.info("    type ROTATION");
                    else if (type == TrackedDataHandlerRegistry.BLOCK_POS) LOGGER.info("    type BLOCK_POS");
                    else if (type == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS)
                        LOGGER.info("    type OPTIONAL_BLOCK_POS");
                    else if (type == TrackedDataHandlerRegistry.FACING) LOGGER.info("    type FACING");
                    else if (type == TrackedDataHandlerRegistry.OPTIONAL_UUID) LOGGER.info("    type OPTIONAL_UUID");
                    else if (type == TrackedDataHandlerRegistry.NBT_COMPOUND) LOGGER.info("    type NBT_COMPOUND");
                    else if (type == TrackedDataHandlerRegistry.VILLAGER_DATA) LOGGER.info("    type VILLAGER_DATA");
                    else if (type == TrackedDataHandlerRegistry.FIREWORK_DATA) LOGGER.info("    type FIREWORK_DATA");
                    else if (type == TrackedDataHandlerRegistry.ENTITY_POSE) LOGGER.info("    type ENTITY_POSE");
                }
            } else if (packet instanceof PlayerPositionLookS2CPacket pplp) {
                Penguinserver.LOGGER.info("PPLP packet: ");
                Penguinserver.LOGGER.info("  Pos: " + pplp.getX() + ", " + pplp.getY() + ", " + pplp.getZ());
                Penguinserver.LOGGER.info("  TPID: " + pplp.getTeleportId());
                Penguinserver.LOGGER.info("  To: " + name_to_addr.get(boundTo));
            } else if (packet instanceof EntityS2CPacket.MoveRelative mr) {
                if (mr.isPositionChanged() && mr.getDeltaX() + mr.getDeltaY() + mr.getDeltaZ() != 0) {
                    Penguinserver.LOGGER.info("MoveRelative packet: ");
                    Penguinserver.LOGGER.info("  X: " + mr.getDeltaX());
                    Penguinserver.LOGGER.info("  Y: " + mr.getDeltaY());
                    Penguinserver.LOGGER.info("  Z: " + mr.getDeltaZ());
                    Penguinserver.LOGGER.info("  Yaw: " + mr.getYaw());
                    Penguinserver.LOGGER.info("  Pitch: " + mr.getPitch());
                    Penguinserver.LOGGER.info("  To: " + name_to_addr.get(boundTo));
                }
            } else if (!ignorable.contains(name) && (name.contains("Player") || name.contains("Entity"))) {
                Penguinserver.LOGGER.info("" + name);
                Penguinserver.LOGGER.info("  To: " + name_to_addr.get(boundTo));
            }
        }
    }

    public static void pfl2(Packet<?> packet) {
        String name = packet.getClass().getName();
        final List<String> ignorable = List.of(
            "net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround"
        );
        if (packet instanceof PlayerInteractEntityC2SPacket piep) {
            LOGGER.info("PlayerInteractEntityC2SPacket");
            LOGGER.info("  Entity: " + piep);
        }  //            LOGGER.info("from client: " + name);

    }
}
