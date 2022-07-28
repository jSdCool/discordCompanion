package com.discordcomp.mixins;

import com.discordcomp.Main;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(PlayerManager.class)
public class
Mixin_ReadChat {

   @Inject(method = "broadcast(Lnet/minecraft/text/Text;Z)V",at =@At("HEAD"),remap = true )
    private void broadcastChatMessage(Text text, boolean overlay, CallbackInfo ci){

           if (text.getString().length() > 9 && text.getString().startsWith("Discord ["))
               return;

       Main.sendMessage(text.getString());



    }
    //PlayerManager#broadcastChatMessage    inject here

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V",at=@At("HEAD"),remap = true)
    private void broadcastChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci){

        String msg=message.getContent().getString();
        Main.sendMessage("<"+sender.getName().getString()+"> "+message.getContent().getString());
    }

}
