package com.discordcomp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer, ServerTickEvents.EndTick{
    public static final Logger LOGGER = LogManager.getLogger("discord companion");
    static String ip;
    static int port;
    static Socket companionConnection;
    static DataOutputStream dos;
    static BufferedReader br;
    static boolean connected = false;

    @Override
    public void onInitialize() {
        LOGGER.info("initializing");
        connected = connectToCompanion();
        if(connected){
            LOGGER.info("companion connected");
        }else{
            LOGGER.info("companion failed to connect");
        }


        ServerTickEvents.END_SERVER_TICK.register( this);

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("discordCompanion").requires(source -> source.hasPermissionLevel(3))
                    .then(literal("reconnect").executes(context -> {
                        //if(!connected) {
                            context.getSource().sendFeedback(new LiteralText("reconnecting"), true);
                            System.out.println("reconnecting to companion");
                            if(connectToCompanion()) {
                                context.getSource().sendFeedback(new LiteralText("connected"), true);
                                connected=true;
                            }else{
                                context.getSource().sendError(new LiteralText("connection failed"));
                            }
                        //}else{
                           // context.getSource().sendError(new LiteralText("companion already connected"));
                        //}

                        return 1;
                    }))

            );
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            ms=server;
            pm = ms.getPlayerManager();

        });
    }

    static MinecraftServer ms;
    static PlayerManager pm;

    boolean connectToCompanion() {
        File config;
        Scanner cfs;
        try {
            config = new File("config\\discorncompanion.cfg");
            cfs = new Scanner(config);
        }catch(Throwable e){
            try {
                FileWriter mr = new FileWriter("config\\discorncompanion.cfg");
                mr.write("#companion ip=\n#companion port=15643");
                mr.close();
                System.out.println("config file created.");

            } catch (IOException ee) {
                System.out.println("\n\n\nAn error occurred while creating config file. you may need to make the config folder if it does not already exist\n\n\n");
                ee.printStackTrace();
                connected=false;
                return false;
            }
            connected =false;
            System.out.println("\n\n\nconfig file created. populate the fields and then restart this server.\n\n\n");
            return false;
        }
        while (cfs.hasNextLine()) {
            String line=cfs.nextLine();
            if(line.indexOf("#")==0){
                String pt1=line.substring(1,line.indexOf("="));
                String data=line.substring(line.indexOf("=")+1);
                if(pt1.equals("companion ip")){
                    ip=data;
                }
                if(pt1.equals("companion port")){
                    port=Integer.parseInt(data);
                }

            }
        }
        try{
            companionConnection =new Socket(ip, port);
            dos = new DataOutputStream(companionConnection.getOutputStream());
            br = new BufferedReader(new InputStreamReader(companionConnection.getInputStream()));
            return true;
        }catch(Throwable e){
        e.printStackTrace();
        return false;
        }

    }

    @Override
    public void onEndTick(MinecraftServer server) {

        if(connected) {
            try {
                String str1 = null;
                if (br.ready())
                    str1 = br.readLine();

                if (str1 != null) {
                    //LOGGER.info(str1);
                    String[] contents=str1.split("\\\\`");
                    String message="",name="";
                    //System.out.println(contents[0]+" "+contents[1]+" "+contents[2]+" "+contents[3]);
                    for(int i=0;i<contents.length;i++){
                        if(contents[i].equals("<name>")){
                            if(++i<contents.length)
                            name=contents[i];
                            continue;
                        }
                        if(contents[i].equals("<message>")){
                            if(++i<contents.length)
                            message=contents[i];
                            continue;
                        }
                    }
                    System.out.println(message);
                    String []chunks=message.split("\\\\n");
                    String ready="";
                    for(int i=0;i< chunks.length;i++){
                        ready+=chunks[i]+"\n";
                    }
                    ready=ready.substring(0,ready.length()-1);
                    if(ready.equals("/list")){
                        List<ServerPlayerEntity> players = pm.getPlayerList();
                        if(players.size()==0){
                            sendMessage("there are no players online");
                            return;
                        }
                        String playersOut="";
                        for (ServerPlayerEntity player : players) {
                            playersOut += player.getName().asString() + " \\\\n";
                        }
                        sendMessage(playersOut);
                        return;
                    }
                    Main.pm.broadcast(new LiteralText("§9Discord §r["+name+"] "+ready), MessageType.SYSTEM, Util.NIL_UUID);
                }



            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public static void sendMessage(String message){
        if(connected)
        try {

            dos.writeBytes("<message>§"+message+"\n");
        }catch (SocketException e){
            connected=false;
            Main.pm.broadcast(new LiteralText("§4disconnected from discord"), MessageType.SYSTEM, Util.NIL_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendInfo(String message){
        if(connected)
        try {
            //System.out.println(message);
            dos.writeBytes("<info>§"+message+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
