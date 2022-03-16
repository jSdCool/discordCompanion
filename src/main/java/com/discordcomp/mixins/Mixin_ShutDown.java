package com.discordcomp.mixins;


import com.discordcomp.Main;
import net.jsdcool.discompnet.CShutdownData;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftDedicatedServer.class)
public class Mixin_ShutDown {
    @Inject(method = "shutdown", at =@At("HEAD"),remap = true )
    private void shutdown(CallbackInfo ci){
        Main.dataToSendToCompanion.data.add(new CShutdownData());
        try {
            Main.companionConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
