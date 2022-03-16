package com.discordcomp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import net.jsdcool.discompnet.*;

import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer, ServerTickEvents.EndTick{
    public static final Logger LOGGER = LogManager.getLogger("discord companion");
    static String ip;
    static int port;
    public static Socket companionConnection;
    public static ObjectOutputStream output;
    public static ObjectInputStream input;
    public static CompanionData dataToSendToCompanion;
    public static boolean connected = false;
    static CompanionConnection connection;

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

        CompanionData d= new CompanionData();
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
        //the part where it actually connects to the companion
        try{
            companionConnection =new Socket(ip, port);
            output=new ObjectOutputStream(companionConnection.getOutputStream());
            input=new ObjectInputStream(companionConnection.getInputStream());
            dataToSendToCompanion=new CompanionData();
            connection=new CompanionConnection();
            connection.start();
            return true;
        }catch(Throwable e){
        e.printStackTrace();
        return false;
        }

    }

    @Override
    public void onEndTick(MinecraftServer server) {

        if(connected) {
            if(!connection.isAlive()){
                connected=false;
                try{
                    companionConnection.close();
                }catch (IOException i){ }
            }
        }

    }

    public static void sendMessage(String message){
        if(connected) {
            dataToSendToCompanion.data.add(new CMinecraftMessageData(message));
        }

    }

    public static void sendInfo(String message){
        if(connected){
            dataToSendToCompanion.data.add(new CMinecraftMessageData(message));
        }

    }
}
