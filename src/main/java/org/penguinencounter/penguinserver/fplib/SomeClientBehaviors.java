package org.penguinencounter.penguinserver.fplib;

import com.google.common.hash.Hashing;
import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.penguinencounter.penguinserver.Penguinserver;

import java.util.UUID;
import java.util.function.Consumer;

public class SomeClientBehaviors {
    /**
     * Mojank
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean getUUIDFromPlayerNameWCallback(String name, ProfileLookupCallback plc) {
        if (Penguinserver.server.getSessionService() instanceof YggdrasilMinecraftSessionService ymss) {
            ymss.getAuthenticationService().createProfileRepository().findProfilesByNames(new String[]{name}, Agent.MINECRAFT, plc);
            return true;
        } else {
            return false;
        }
    }

    public static class JustGiveMeTheUUID implements ProfileLookupCallback {
        public UUID theUUID;
        public Consumer<UUID> callback;

        public JustGiveMeTheUUID(Consumer<UUID> callback) {
            this.callback = callback;
        }

        @Override
        public void onProfileLookupSucceeded(GameProfile profile) {
            theUUID = profile.getId();
            callback.accept(theUUID);
        }

        @Override
        public void onProfileLookupFailed(GameProfile profile, Exception exception) {
            theUUID = null;
        }
    }
}
