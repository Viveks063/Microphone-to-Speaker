package com.example.client;

import javax.sound.sampled.*;
import javax.websocket.*;
import java.nio.ByteBuffer;

@ClientEndpoint
public class AudioClientEndpoint {

    private SourceDataLine speakers;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server: " + session.getId());
        try {
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            speakers.open(format);
            speakers.start();
        } catch (LineUnavailableException e) {
            System.err.println("Speakers not available: " + e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(ByteBuffer audioData) {
        byte[] buffer = new byte[audioData.remaining()];
        audioData.get(buffer);
        speakers.write(buffer, 0, buffer.length);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Disconnected from server: " + closeReason);
        if (speakers != null) {
            speakers.stop();
            speakers.close();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}