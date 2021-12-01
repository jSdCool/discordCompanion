package com.discordcomp.mixins;


import com.discordcomp.Main;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class Mixin_ReadPlayerChat {
    @Shadow public ServerPlayerEntity player;



    @Inject(method = "onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V",at =@At("HEAD"),remap = true )
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci){
        //System.out.println("detected by mixin2");
        if(packet.getChatMessage().substring(0,1).equals("/")){
            return;
        }
        Main.sendMessage("<"+player.getDisplayName().getString()+"> "+packet.getChatMessage());

    }
    //PlayerManager#broadcastChatMessage    inject here
}
