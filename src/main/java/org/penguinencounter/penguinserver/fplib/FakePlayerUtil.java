package org.penguinencounter.penguinserver.fplib;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.loader.api.FabricLoader.getInstance;
import static org.penguinencounter.penguinserver.Penguinserver.LOGGER;
import static org.penguinencounter.penguinserver.Penguinserver.server;

public class FakePlayerUtil {
    public ArrayList<FakePlayer> fakes = new ArrayList<>();
    public HashMap<UUID, Integer> reloadSchedule = new HashMap<>();
    public void autorun() {
        for (FakePlayer f: fakes) {
            f.tick();
        }
        for (Map.Entry<UUID, Integer> entry : reloadSchedule.entrySet()) {
            if (entry.getValue() <= 0) {
                reloadSchedule.remove(entry.getKey());
                ServerPlayerEntity spe = server.getPlayerManager().getPlayer(entry.getKey());
                if (spe != null) {
                    refresh(spe);
                }
            } else {
                reloadSchedule.replace(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    public void refreshAll() {
        List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity spe : allPlayers) {
            refresh(spe);
        }
    }
    public void destroyAll() {
        List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity spe : allPlayers) {
            ClientConnection cc = spe.networkHandler.getConnection();
            destroyOnClient(cc);
        }
    }

    public void refresh(ServerPlayerEntity player) {
        ClientConnection cc = player.networkHandler.getConnection();
        // remove players manually from client
        destroyOnClient(cc);
        // ... then, add the players again
        // step 1: flash list
        ListFlasher.flashNow(fakes.stream().map(f -> f.player).toList(), cc);
        // step 2: reconnect players
        rebuildAllFakes(cc);
    }

    public void rebuildAllFakes(ClientConnection cc) {
        for (FakePlayer f : fakes) {
            cc.send(new PlayerSpawnS2CPacket(f.player));
            cc.send(new PlayerSpawnPositionS2CPacket(server.getOverworld().getSpawnPos(), 0));
            cc.send(new EntityPositionS2CPacket(moveEntityTo(f.player, f.pos, f.pitch, f.yaw)));
            byte byaw = (byte) (f.yaw / 360 * 256);
            cc.send(new EntitySetHeadYawS2CPacket(f.player, byaw));
            f.updateSkinLayersAll();
        }
    }

    public void destroyOnClient(ClientConnection cc) {
        ArrayList<Integer> ints = new ArrayList<>();
        for (FakePlayer f : fakes) {
            ints.add(f.player.getId());
        }
        Integer[] wrapperArr = ints.toArray(new Integer[0]);
        int[] ints1 = ArrayUtils.toPrimitive(wrapperArr);
        cc.send(new EntitiesDestroyS2CPacket(ints1));
    }

    public void asyncJoin(String name) {
        // get uuid
        SomeClientBehaviors.getUUIDFromPlayerNameWCallback(name,
                new SomeClientBehaviors.JustGiveMeTheUUID(uuid -> join(uuid, name))
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    public FakePlayer join(UUID uuid, String name) {
        FakePlayer pv = new FakePlayer(uuid, name, null, new Vec3d(0.5, 80, 0.5), 0, 0);
        fakes.add(pv);
        return pv;
    }
    public FakePlayer join(UUID uuid, String name, @Nullable Set<UUID> targetPlayers, Vec3d at, float pitch, float yaw) {
        FakePlayer pv = new FakePlayer(uuid, name, targetPlayers, at, pitch, yaw);
        fakes.add(pv);
        return pv;
    }

    public void clearFakes() {
        fakes.clear();
    }

    public static PacketByteBuf moveEntityTo(ServerPlayerEntity spe, Vec3d at, float pitch, float yaw) {
        byte bpitch = (byte) (pitch / 360 * 256);
        byte byaw = (byte) (yaw / 360 * 256);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(spe.getId());
        buf.writeDouble(at.getX());
        buf.writeDouble(at.getY());
        buf.writeDouble(at.getZ());
        buf.writeByte(byaw);
        buf.writeByte(bpitch);
        buf.writeBoolean(false);
        return buf;
    }

    public static void setSkinProfile(GameProfile profile, UUID uuid, @Nullable Runnable then) {
        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false";
        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
                huc.setDoInput(true);
                huc.setDoOutput(false);
                huc.connect();
                if (huc.getResponseCode() != 200) {
                    return;
                }
                byte[] result = huc.getInputStream().readAllBytes();  // blocking
                String signedData = new String(result, StandardCharsets.UTF_8);
                // Can't be bothered to use JSON
                Pattern p = Pattern.compile("\"value\"\\s?:\\s?\"(.+?)\",\\s+\"signature\"\\s?:\\s?\"(.+?)\"");
                Matcher m = p.matcher(signedData);
                if (!m.find()) return;
                String texture = m.group(1);
                String signature = m.group(2);
                saveSkinProfileData(uuid, texture, signature);
                setSkinProfile(profile, texture, signature);
                if (then != null) {
                    then.run();
                }
            } catch (Exception ignored) {}
        });
    }

    public static void setSkinProfile(GameProfile profile, String textureData, String signature) {
        PropertyMap props = profile.getProperties();
        Property texProp = new Property("textures", textureData, signature);
        props.put("textures", texProp);
    }

    public static void setSkinProfileFromFile(GameProfile profile, String fn) {
        final String dirLoc = FabricLoader.getInstance().getGameDir().toString() + File.separator + "skin_data";
        try {
            File f = new File(dirLoc, fn);
            if (!f.exists()) {
                return;
            }
            // read in file
            FileReader fr = new FileReader(f);
            TexSigPair data = new Gson().fromJson(fr, TexSigPair.class);
            setSkinProfile(profile, data.texture, data.signature);
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSkinProfileData(UUID name, String texture, String signature) {
        String dirLoc = FabricLoader.getInstance().getGameDir().toString() + File.separator + "skin_data";
        File dir = new File(dirLoc);
        String dtexture = new String(Base64.getDecoder().decode(texture.getBytes()), StandardCharsets.UTF_8);
        final Pattern p = Pattern.compile("\"SKIN\"\\s?:\\s?\\{\\s*\"url\"\\s?:\\s?\"(.+?)\"\\s*}");
        Matcher m = p.matcher(dtexture);
        String texURI;
        if (m.find()) {
            texURI = m.group(1);
        } else {
            texURI = null;
        }
        LOGGER.info("New: " + texURI);
        // Create the directory if it doesn't exist. Don't care if it doesn't work.
        // noinspection ResultOfMethodCallIgnored
        dir.mkdir();
        String targetPath = dirLoc + File.separator + name.toString() + "-0.json";
        Gson gson = new Gson();
        int i = 0;
        while (new File(targetPath).exists()) {
            // read in the file
            File wipFile = new File(targetPath);
            try {
                FileReader fr = new FileReader(wipFile);
                TexSigPair data = new Gson().fromJson(fr, TexSigPair.class);
                // read texture data
                String test = data.texture;
                String test2 = new String(Base64.getDecoder().decode(test), StandardCharsets.UTF_8);
                // read in uri
                Matcher m2 = p.matcher(test2);
                if (m2.find()) {
                    String ftexURI = m2.group(1);
                    LOGGER.info("In file: " + ftexURI);
                    if (ftexURI.equals(texURI)) {
                        // same uri, same texture.

                        LOGGER.info("Skipping duplicate skin data for " + name);
                        return;
                    }
                }
                fr.close();
            } catch (IOException ignored) {}
            i++;
            targetPath = dirLoc + File.separator + name + "-" + i + ".json";
        }
        try {
            File f = new File(targetPath);
            if (!f.createNewFile()) return;
            FileWriter fw = new FileWriter(f);
            gson.toJson(new TexSigPair(texture, signature), fw);
            LOGGER.info("New skin data saved for " + name);
            fw.close();
        } catch (IOException ignored) {}
    }
}

