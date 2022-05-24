package org.penguinencounter.penguinserver.mixin;

import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.resource.ResourceManager;
import org.penguinencounter.penguinserver.ClientAssist;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSkinTexture.class)
public class SkinLoaderIntercept {
    @Final
    @Shadow
    private String url;
    // CLIENT
    @Inject(
            method = "load",
            at = @At("HEAD")
    )
    public void load(ResourceManager manager, CallbackInfo ci) {
        ClientAssist.LOGGER.info("SKIN GET URL : " + url);
    }
}
