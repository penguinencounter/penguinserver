package org.penguinencounter.penguinserver;

import io.netty.util.internal.logging.InternalLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientAssist implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("PenguinServerClient (???)");

    @Override
    public void onInitializeClient() {

    }

    public static void pfl2(Packet<?> packet) {
//        if (packet instanceof DataTracker)
//        LOGGER.info("Packet intercepted");
//        if (type == TrackedDataHandlerRegistry.BYTE) LOGGER.info("    type BYTE");
//        else if (type == TrackedDataHandlerRegistry.INTEGER) LOGGER.info("    type INTEGER");
//        else if (type == TrackedDataHandlerRegistry.FLOAT) LOGGER.info("    type FLOAT");
//        else if (type == TrackedDataHandlerRegistry.STRING) LOGGER.info("    type STRING");
//        else if (type == TrackedDataHandlerRegistry.TEXT_COMPONENT) LOGGER.info("    type TEXT_COMPONENT");
//        else if (type == TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) LOGGER.info("    type OPTIONAL_TEXT_COMPONENT");
//        else if (type == TrackedDataHandlerRegistry.ITEM_STACK) LOGGER.info("    type ITEM_STACK");
//        else if (type == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE) LOGGER.info("    type OPTIONAL_BLOCK_STATE");
//        else if (type == TrackedDataHandlerRegistry.BOOLEAN) LOGGER.info("    type BOOLEAN");
//        else if (type == TrackedDataHandlerRegistry.PARTICLE) LOGGER.info("    type PARTICLE");
//        else if (type == TrackedDataHandlerRegistry.ROTATION) LOGGER.info("    type ROTATION");
//        else if (type == TrackedDataHandlerRegistry.BLOCK_POS) LOGGER.info("    type BLOCK_POS");
//        else if (type == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS) LOGGER.info("    type OPTIONAL_BLOCK_POS");
//        else if (type == TrackedDataHandlerRegistry.FACING) LOGGER.info("    type FACING");
//        else if (type == TrackedDataHandlerRegistry.OPTIONAL_UUID) LOGGER.info("    type OPTIONAL_UUID");
//        else if (type == TrackedDataHandlerRegistry.NBT_COMPOUND) LOGGER.info("    type NBT_COMPOUND");
//        else if (type == TrackedDataHandlerRegistry.VILLAGER_DATA) LOGGER.info("    type VILLAGER_DATA");
//        else if (type == TrackedDataHandlerRegistry.FIREWORK_DATA) LOGGER.info("    type FIREWORK_DATA");
//        else if (type == TrackedDataHandlerRegistry.ENTITY_POSE) LOGGER.info("    type ENTITY_POSE");
    }
}
