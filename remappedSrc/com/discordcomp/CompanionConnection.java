package com.discordcomp;


import net.jsdcool.discompnet.*;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralTextContent;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.message.MessageType;
import net.minecraft.world.GameMode;

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
        System.out.println("disconnected from companion");
    }

    void processInputData(CompanionData data){
        for(int i=0;i<data.data.size();i++){
            if(data.data.get(i) instanceof CDiscordMessageData msg){
                MutableText chatMessage=MutableText.of(new LiteralTextContent("")) ,discordText =MutableText.of(new LiteralTextContent("Discord "));
                discordText.setStyle(chatMessage.getStyle().withColor(5592575));
                chatMessage.append(discordText);
                MutableText discordName=MutableText.of(new LiteralTextContent(("["+msg.displayName+"] ")));
                discordName.setStyle(discordName.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,MutableText.of(new LiteralTextContent(("Discord name: "+msg.name+"\nid: "+msg.id))))).withColor(msg.nameColor));
                chatMessage.append(discordName);
                chatMessage.append(msg.message);
                Main.pm.broadcast(chatMessage, false);
            }
            if(data.data.get(i) instanceof CComandList){
                List<ServerPlayerEntity> players =Main.pm.getPlayerList();
                if(players.size()==0){
                    Main.sendMessage("no players are online right now");
                }else{
                    String playerList="";
                    for(int j=0;j<players.size();j++){
                        playerList+=players.get(j).getName().getString()+"\n";
                    }
                    Main.sendMessage(playerList);
                }
            }

            if(data.data.get(i) instanceof CAuthResponce response){
                CommandContext<ServerCommandSource> context =Main.commandReqs.get(response.reqnum);
                if(response.success){
                    context.getSource().sendFeedback(MutableText.of(new LiteralTextContent("success")), true);
                }else{
                    context.getSource().sendError(MutableText.of(new LiteralTextContent(("fail: "+response.reason))));
                }
                Main.commandReqs.remove(response.reqnum);
            }
            if(data.data.get(i) instanceof CTeleportCommand tp){

                if(Main.pm.getPlayerNames().length>0&&hasPlayer(tp.name)) {
                    ServerPlayerEntity player = Main.pm.getPlayer(tp.name);
                    player.setPos(tp.x, tp.y, tp.z);
                    Main.sendMessage("teleported player");
                }else{
                    Main.sendMessage("player not found");
                }
            }
            if(data.data.get(i) instanceof  CPlayerPositionCommand pos){
                if(Main.pm.getPlayerNames().length>0&&hasPlayer(pos.name)) {
                    ServerPlayerEntity player = Main.pm.getPlayer(pos.name);
                    Vec3d cords = player.getPos();
                    Main.sendMessage(pos.name+": "+cords.x+" "+cords.y+" "+cords.z);
                }else{
                    Main.sendMessage("player not found");
                }
            }

            if(data.data.get(i) instanceof  CKickCommand kick){
                if(Main.pm.getPlayerNames().length>0&&hasPlayer(kick.name)) {
                    ServerPlayerEntity player = Main.pm.getPlayer(kick.name);
                    if(kick.reason.equals(""))
                        kick.reason="kicked by an operator from discord";
                    player.networkHandler.disconnect(MutableText.of(new LiteralTextContent((kick.reason))));
                    Main.sendMessage("kicked "+kick.name+": "+kick.reason);
                    System.out.println("kicked "+kick.name+": "+kick.reason);
                }else{
                    Main.sendMessage("player not found");
                }
            }

            if(data.data.get(i) instanceof  CGamemodeCommand gamemode){
                if(Main.pm.getPlayerNames().length>0&&hasPlayer(gamemode.name)) {
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
                    player.sendMessage(MutableText.of(new LiteralTextContent("gamemode updated")),false);
                }else{
                    Main.sendMessage("player not found");
                }
            }

            if(data.data.get(i) instanceof  CVersionCommand){
                Main.sendMessage("mod version: "+Main.version+"\ngame version: "+ MinecraftVersion.CURRENT.getName());
            }

        }
    }

    boolean hasPlayer(String name){
        List<ServerPlayerEntity> players =Main.pm.getPlayerList();
        for(int i=0;i<players.size();i++){
            if(players.get(i).getName().getString().equals(name)){
                return true;
            }
        }
        return false;
    }

}
