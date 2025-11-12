package com.example.togethernet.network;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

public class MessageManager {
    private static MessageManager instance;
    private final GattClientManager clientManager;
    private final GattServerManager serverManager;
    private static final String TAG = "MessageManager";
    private BluetoothDevice currentDevice; // last connected device
    private String currentMac;

    public interface ChatListener{
        void onMessageReceived(String fromMac, String message);
        void onMessageSent(String toMac, String message); 
    }

    private ChatListener chatListener;

    private MessageManager(Context context, GattClientManager client, GattServerManager server){
        this.clientManager = client;
        this.serverManager = server;
        setupCallbacks();
    }

    public static void initialize(Context context, GattClientManager client, GattServerManager server){
        if (instance == null) {
            instance = new MessageManager(context, client, server);
        }
    }

    public static MessageManager getInstance(){
        if (instance == null) throw new IllegalStateException("MessageManager not initialized");
        return instance;
    }

    public void setChatListener(ChatListener listener){
        this.chatListener = listener;
    }

    private void setupCallbacks() {
        if (clientManager != null) {
            clientManager.setMessageListener((mac, msg) -> {
                if (chatListener != null) chatListener.onMessageReceived(mac, msg);
                Log.i(TAG, "MessageManager received via client: " + msg);
            });
        }
        if (serverManager != null) {
            serverManager.setMessageListener((mac, msg) -> {
                if (chatListener != null) chatListener.onMessageReceived(mac, msg);
                Log.i(TAG, "MessageManager received via server: " + msg);
            });
        }
    }

    public void connectToDevice(BluetoothDevice device){
        this.currentDevice = device;
        this.currentMac = device.getAddress();
        clientManager.connect(device);
    }

    public void sendMessage(String msg){
        if (clientManager != null) clientManager.sendMessage(msg);
        if (chatListener != null && currentMac != null) chatListener.onMessageSent(currentMac, msg);
    }

    public BluetoothDevice getCurrentDevice(){
        return currentDevice;
    }
}
