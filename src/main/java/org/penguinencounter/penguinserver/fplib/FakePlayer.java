package org.penguinencounter.penguinserver.fplib;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.penguinencounter.penguinserver.Penguinserver;
import org.penguinencounter.penguinserver.fpactions.FakePlayerAction;

import javax.annotation.Nullable;
import java.util.*;

import static org.penguinencounter.penguinserver.Penguinserver.LOGGER;
import static org.penguinencounter.penguinserver.Penguinserver.server;

public class FakePlayer {
    public final UUID uuid;
    public final String name;
    public Set<UUID> targetPlayers;
    public HashSet<UUID> syncedPlayers = new HashSet<>();
    public float pitch;
    public float yaw;
    public Vec3d pos;
    private float sendPitch;
    private float sendYaw;
    private Vec3d sendPos;

    public SkinLayers skinLayers;
    private SkinLayers sendSkinLayers;

    public GameProfile profile;
    public ServerPlayerEntity player;

    public ArrayList<FakePlayerAction> actions = new ArrayList<>();
    public FakePlayer(UUID uuid, String name, @Nullable Set<UUID> targetPlayers, Vec3d at, float pitch, float yaw) {
        // save data
        this.uuid = uuid;
        this.name = name;
        this.targetPlayers = targetPlayers;
        this.pos = at;
        this.pitch = pitch;
        this.yaw = yaw;

        this.skinLayers = new SkinLayers(true, true, true, true, true, true, true);
        this.sendSkinLayers = new SkinLayers(false, false, false, false, false, false, false);

        // then, create ServerPlayerEntity
        // construct game profile
        this.profile = new GameProfile(uuid, name);
        this.player = new ServerPlayerEntity(server, server.getOverworld(), profile);
    }


    public void destroyOn(ServerPlayerEntity spe) {
        spe.networkHandler.getConnection().send(new EntitiesDestroyS2CPacket(this.player.getId()));
        syncedPlayers.remove(spe.getUuid());
    }


    public void createOn(ServerPlayerEntity spe) {
        ClientConnection cc = spe.networkHandler.getConnection();
        ListFlasher.flashNow(List.of(player), cc);
        cc.send(new PlayerSpawnS2CPacket(this.player));
        cc.send(new PlayerSpawnPositionS2CPacket(server.getOverworld().getSpawnPos(), 0));
        cc.send(new EntityPositionS2CPacket(FakePlayerUtil.moveEntityTo(this.player, this.pos, this.pitch, this.yaw)));
        byte byaw = (byte) (yaw / 360 * 256);
        cc.send(new EntitySetHeadYawS2CPacket(this.player, byaw));
        syncedPlayers.add(spe.getUuid());
        updateSkinLayers(cc);
    }

    public void updatePosition(ClientConnection cc) {
        cc.send(new EntityPositionS2CPacket(FakePlayerUtil.moveEntityTo(this.player, this.pos, this.pitch, this.yaw)));
        byte byaw = (byte) (yaw / 360 * 256);
        cc.send(new EntitySetHeadYawS2CPacket(this.player, byaw));
    }

    public void updatePositionAll() {
        for (UUID uuid : syncedPlayers) {
            ServerPlayerEntity spe = server.getPlayerManager().getPlayer(uuid);
            if (spe != null) {
                ClientConnection cc = spe.networkHandler.getConnection();
                updatePosition(cc);
            }
        }
        sendPitch = pitch;
        sendYaw = yaw;
        sendPos = pos;
    }

    public void updateSkinLayers(ClientConnection cc) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(player.getId());
        DataTracker.entriesToPacket(List.of(EntityTrackerMapping.buildSkin(skinLayers)), buf);
        cc.send(new EntityTrackerUpdateS2CPacket(buf));
    }
    public void updateSkinLayersAll() {
        for (UUID uuid : syncedPlayers) {
            ServerPlayerEntity spe = server.getPlayerManager().getPlayer(uuid);
            if (spe != null) {
                ClientConnection cc = spe.networkHandler.getConnection();
                updateSkinLayers(cc);
            }
        }
        sendSkinLayers = skinLayers.copy();
    }

    public void runActions() {
        for (FakePlayerAction action : actions) {
            action.tick();
        }
    }

    public void tick() {
        limitSyncedToOnline();
        autoSync();
        if (sendPitch != pitch || sendYaw != yaw || sendPos != pos) {
            updatePositionAll();
        }
        if (!sendSkinLayers.equals(skinLayers)) {
            updateSkinLayersAll();
        }
        runActions();
    }

    public void autoSync() {
        // get server view distance from properties
        int viewDistance;
        if (server instanceof DedicatedServer ds) {
            viewDistance = ds.getProperties().viewDistance;
        } else if (server instanceof IntegratedServer) {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                // get view distance from settings
                viewDistance = client.options.viewDistance;
            } catch (Exception e) {
                LOGGER.error("Error while syncing fake player on integrated server:", e);
                viewDistance = 32;
            }
        } else {
            Penguinserver.LOGGER.error("Unknown server type - could not determine view distance");
            viewDistance = 32;
        }
        long viewBlocks = viewDistance * 32L + 16;  // 16 extra just in case
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity p : players) {
            if (syncedPlayers.contains(p.getUuid())) {
                if (p.squaredDistanceTo(pos) > viewBlocks*viewBlocks) {
                    destroyOn(p);
                }
            } else {
                if (p.squaredDistanceTo(pos) < viewBlocks*viewBlocks && (targetPlayers == null || targetPlayers.contains(p.getUuid()))) {
                    createOn(p);
                }
            }
        }
    }

    /**
     * Players that aren't connected aren't synced, duh!
     */
    public void limitSyncedToOnline() {
        List<UUID> online = server.getPlayerManager().getPlayerList().stream().map(Entity::getUuid).toList();
        syncedPlayers.retainAll(online);
    }
}

