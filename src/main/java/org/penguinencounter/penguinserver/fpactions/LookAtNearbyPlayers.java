package org.penguinencounter.penguinserver.fpactions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.penguinencounter.penguinserver.Penguinserver;
import org.penguinencounter.penguinserver.fplib.FakePlayer;

import java.util.List;

public class LookAtNearbyPlayers implements FakePlayerAction {
    public FakePlayer fakePlayer;
    public int threshold = 6;
    private final float defaultPitch;
    private final float defaultYaw;
    public LookAtNearbyPlayers(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
        this.defaultPitch = fakePlayer.pitch;
        this.defaultYaw = fakePlayer.yaw;
    }

    public void tick() {
        MinecraftServer server = Penguinserver.server;
        // GET ALL PLAYERS
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.size() == 0) return;
        // Sort players by distance to fakePlayer
        players.sort((p1, p2) -> {
            double d1 = p1.squaredDistanceTo(fakePlayer.pos);
            double d2 = p2.squaredDistanceTo(fakePlayer.pos);
            return Double.compare(d1, d2);
        });
        // Look at closest player, if under the THRESHOLD
        ServerPlayerEntity closest = players.get(0);
        if (closest.squaredDistanceTo(fakePlayer.pos) < threshold * threshold) {
            double d = closest.getPos().x - fakePlayer.pos.x;
            double e = closest.getPos().y - fakePlayer.pos.y;
            double f = closest.getPos().z - fakePlayer.pos.z;
            double g = Math.sqrt(d * d + f * f);

            fakePlayer.yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f);
            fakePlayer.pitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875)));
        } else if (fakePlayer.pitch != defaultPitch || fakePlayer.yaw != defaultYaw) {
            fakePlayer.yaw = defaultYaw;
            fakePlayer.pitch = defaultPitch;
        }
    }
}
