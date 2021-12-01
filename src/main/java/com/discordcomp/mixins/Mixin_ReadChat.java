package com.discordcomp.mixins;

import com.discordcomp.Main;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.UUID;



@Mixin(PlayerManager.class)
public class Mixin_ReadChat {

   @Inject(method = "broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V",at =@At("HEAD"),remap = true )
    private void broadcastChatMessage(Text text, MessageType tp, UUID uid, CallbackInfo info){
       //System.out.println("detected by mixin");

           if (text.getString().length() > 12 && text.getString().substring(0, 12).equals("§9Discord §r"))
               return;
            //System.out.println("sending");
       Main.sendMessage(text.getString());



    }
    //PlayerManager#broadcastChatMessage    inject here

}
