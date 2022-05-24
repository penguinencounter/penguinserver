package org.penguinencounter.penguinserver.fplib;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.penguinencounter.penguinserver.Penguinserver;

import java.util.ArrayList;
import java.util.List;

public class ListFlasher {
    public ArrayList<ServerPlayerEntity> fakes = new ArrayList<>();
    public static long DELAY = 5;  // 1/4th of a second
    private long currentDelayCounter = -1;
    public static ArrayList<ListFlasher> REGISTRY = new ArrayList<>();
    private ClientConnection connector;

    public static void tickAll(MinecraftServer _S) {
        // clone registry to avoid concurrent modification
        ArrayList<ListFlasher> rc = new ArrayList<>(REGISTRY);
        for (ListFlasher lf : rc) {
            lf.tick();
        }
    }
    public void tick() {
        if (currentDelayCounter >= 0) {
            currentDelayCounter++;
            if (currentDelayCounter == DELAY) {
                // do things
                unFlash();
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean flashNow(List<ServerPlayerEntity> new_fakes, ClientConnection connector) {
        ListFlasher flasher = new ListFlasher();
        flasher.fakes.addAll(new_fakes);
        flasher.connector = connector;
        Penguinserver.LOGGER.info("Started flashing list of " + flasher.fakes.size() + " players");
        connector.send(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new_fakes));
        // set timer
        flasher.currentDelayCounter = 0;
        REGISTRY.add(flasher);
        return true;
    }

    private void unFlash() {
        // do un-flashing
        Penguinserver.LOGGER.info("Done flashing list of " + fakes.size() + " players");
        connector.send(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, fakes));
        fakes.clear();
        REGISTRY.remove(this);
    }
}
