package com.discordcomp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer, ServerTickEvents.EndTick{
    public static final Logger LOGGER = LogManager.getLogger("discord companion");
    static String ip="localhost";
    static int port=15643;
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
                            name=contents[++i];
                            continue;
                        }
                        if(contents[i].equals("<message>")){
                            message=contents[++i];
                            continue;
                        }
                    }
                    //System.out.println(message);
                    Main.pm.broadcastChatMessage(new LiteralText("§9Discord §r["+name+"] "+message), MessageType.SYSTEM, Util.NIL_UUID);
                }



            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public static void sendMessage(String message){
        try {
            //System.out.println(message);
            dos.writeBytes("<message>§"+message+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendInfo(String message){
        try {
            //System.out.println(message);
            dos.writeBytes("<info>§"+message+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
