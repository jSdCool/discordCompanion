package com.discordcomp.mixins;

import com.discordcomp.Main;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.UUID;
import java.util.function.Function;


@Mixin(PlayerManager.class)
public class
Mixin_ReadChat {

   @Inject(method = "broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/util/registry/RegistryKey;)V",at =@At("HEAD"),remap = true )
    private void broadcastChatMessage(Text text, RegistryKey<MessageType> typeKey, CallbackInfo info){
       //System.out.println("message detected by 1st inject in read chat message: "+text.getString());

           if (text.getString().length() > 9 && text.getString().startsWith("Discord ["))
               return;
            //System.out.println("sending");
       Main.sendMessage(text.getString());



    }
    //PlayerManager#broadcastChatMessage    inject here

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Function;Lnet/minecraft/network/message/MessageSender;Lnet/minecraft/util/registry/RegistryKey;)V",at=@At("HEAD"),remap = true)
    private void broadcastChatMessage (SignedMessage message, Function<ServerPlayerEntity,SignedMessage> playerMessageFactory, MessageSender sender, RegistryKey<MessageType> typeKey, CallbackInfo info){
     String msg=message.getContent().getString();
     //System.out.println("message detected by 2nd inject in read chat message: "+msg);
        Main.sendMessage(message.getContent().getString());
    }

}
