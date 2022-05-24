package org.penguinencounter.penguinserver.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.penguinencounter.penguinserver.ClientAssist;
import org.penguinencounter.penguinserver.Penguinserver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class S2CPacketIntercept {
    @Inject(
            method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD")
    )
    private void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        ClientConnection connector = (ClientConnection) (Object) this;
        Penguinserver.pfl(packet, connector.getAddress().toString());
    }

    @Inject(
            method = "handlePacket",
            at = @At("HEAD")
    )
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        // intercept
        Penguinserver.pfl2(packet);
    }
}
