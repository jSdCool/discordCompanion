package com.discordcomp;


import net.jsdcool.discompnet.CDiscordMessageData;
import net.jsdcool.discompnet.CompanionData;
import net.minecraft.network.MessageType;
import net.minecraft.text.BaseText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import java.io.IOException;

public class CompanionConnection extends Thread{

    public void run(){
        try{
            while(Main.companionConnection.isConnected()&&!Main.companionConnection.isClosed()){
                CompanionData dataIn = (CompanionData) Main.input.readObject();
                processInputData(dataIn);
                CompanionData send = Main.dataToSendToCompanion;
                Main.dataToSendToCompanion=new CompanionData();
                Main.output.writeObject(send);
                Main.output.flush();
                Main.output.reset();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            Main.companionConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.connected=false;
    }

    void processInputData(CompanionData data){
        for(int i=0;i<data.data.size();i++){
            if(data.data.get(i) instanceof CDiscordMessageData msg){
                BaseText chatMessage=new LiteralText(""),discordText =new LiteralText("Discord ");
                discordText.setStyle(chatMessage.getStyle().withColor(5592575));
                chatMessage.append(discordText);
                BaseText discordName=new LiteralText("["+msg.displayName+"] ");
                discordName.setStyle(discordName.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new LiteralText("Discord name: "+msg.name+"\nid: "+msg.id))).withColor(msg.nameColor));
                chatMessage.append(discordName);
                chatMessage.append(msg.message);
                Main.pm.broadcast(chatMessage, MessageType.SYSTEM, Util.NIL_UUID);
            }
        }
    }

}
