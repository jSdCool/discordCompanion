package com.discordcomp;


import net.jsdcool.discompnet.*;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        System.out.println("disconnected from companion");
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
            if(data.data.get(i) instanceof CTeleportCommand tp){

                if(Main.pm.getPlayerNames().length>0&&Arrays.binarySearch(Main.pm.getPlayerNames(),tp.name)>-1) {
                    ServerPlayerEntity player = Main.pm.getPlayer(tp.name);
                    player.setPos(tp.x, tp.y, tp.z);
                    Main.sendMessage("teleported player");
                }else{
                    Main.sendMessage("player not found");
                }
            }
            if(data.data.get(i) instanceof  CPlayerPositionCommand pos){
                if(Main.pm.getPlayerNames().length>0&&Arrays.binarySearch(Main.pm.getPlayerNames(),pos.name)>-1) {
                    ServerPlayerEntity player = Main.pm.getPlayer(pos.name);
                    Vec3d cords = player.getPos();
                    Main.sendMessage(pos.name+": "+cords.x+" "+cords.y+" "+cords.z);
                }else{
                    Main.sendMessage("player not found");
                }
            }

            if(data.data.get(i) instanceof  CKickCommand kick){
                if(Main.pm.getPlayerNames().length>0&&Arrays.binarySearch(Main.pm.getPlayerNames(),kick.name)>-1) {
                    ServerPlayerEntity player = Main.pm.getPlayer(kick.name);
                    if(kick.reason.equals(""))
                        kick.reason="kicked by an operator from discord";
                    player.networkHandler.disconnect(new LiteralText(kick.reason));
                    Main.sendMessage("kicked "+kick.name+": "+kick.reason);
                    System.out.println("kicked "+kick.name+": "+kick.reason);
                }else{
                    Main.sendMessage("player not found");
                }
            }

            if(data.data.get(i) instanceof  CGamemodeCommand gamemode){
                if(Main.pm.getPlayerNames().length>0&&Arrays.binarySearch(Main.pm.getPlayerNames(),gamemode.name)>-1) {
                    ServerPlayerEntity player = Main.pm.getPlayer(gamemode.name);
                    GameMode mode=GameMode.DEFAULT;
                    switch(gamemode.gameMode){
                        case "creative":
                            mode=GameMode.CREATIVE;
                            break;
                        case "survival":
                            mode=GameMode.SURVIVAL;
                            break;
                        case "adventure":
                            mode=GameMode.ADVENTURE;
                            break;
                        case "spectator":
                            mode=GameMode.SPECTATOR;
                            break;
                        default:
                            Main.sendMessage("invalid gamemode");
                            continue;
                    }
                    player.changeGameMode(mode);
                    Main.sendMessage("gamemode updated");
                    player.sendMessage(new LiteralText("gamemode updated"),MessageType.GAME_INFO, Util.NIL_UUID);
                }else{
                    Main.sendMessage("player not found");
                }
            }

        }
    }

}
