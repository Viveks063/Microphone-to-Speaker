package com.example.client;

import javax.sound.sampled.*;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.nio.ByteBuffer;

public class AudioClient {

    private static final String SERVER_URI = "ws://localhost:8080/voice/room1";
    private volatile boolean isRunning = true;
    private Session session;

    public static void main(String[] args) {
        new AudioClient().runClient();
    }

    public void runClient() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(AudioClientEndpoint.class, URI.create(SERVER_URI));
            
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(targetInfo);
            microphone.open(format);
            microphone.start();
            System.out.println("Microphone is open. Start speaking...");

            new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (isRunning) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        try {
                            session.getBasicRemote().sendBinary(ByteBuffer.wrap(buffer, 0, bytesRead));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            System.out.println("Connected to voice room. Press Enter to disconnect.");
            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}