package com.discordcomp;


import net.jsdcool.discompnet.CAuthResponce;
import net.jsdcool.discompnet.CComandList;
import net.jsdcool.discompnet.CDiscordMessageData;
import net.jsdcool.discompnet.CompanionData;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import com.mojang.brigadier.context.CommandContext;

import java.io.IOException;
import java.util.List;

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
            //e.printStackTrace();
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
            if(data.data.get(i) instanceof CComandList){
                List<ServerPlayerEntity> players =Main.pm.getPlayerList();
                if(players.size()==0){
                    Main.sendMessage("no players are online right now");
                }else{
                    String playerList="";
                    for(int j=0;j<players.size();j++){
                        playerList+=players.get(i).getName().asString()+"\n";
                    }
                    Main.sendMessage(playerList);
                }
            }

            if(data.data.get(i) instanceof CAuthResponce response){
                CommandContext<ServerCommandSource> context =Main.commandReqs.get(response.reqnum);
                if(response.success){
                    context.getSource().sendFeedback(new LiteralText("success"), true);
                }else{
                    context.getSource().sendError(new LiteralText("fail: "+response.reason));
                }
                Main.commandReqs.remove(response.reqnum);
            }
        }
    }

}
