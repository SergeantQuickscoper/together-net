package com.example.togethernet.network;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.UUID;

public class GattClientManager {

    private static final String TAG = "GattClientManager";

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic messageCharacteristic;

    private static final UUID CHAT_SERVICE_UUID = UUID.fromString("0000ABC0-0000-1000-8000-00805F9B34FB");
    private static final UUID CHAT_MESSAGE_UUID = UUID.fromString("0000ABC1-0000-1000-8000-00805F9B34FB");

    private final Context context;

    @FunctionalInterface
    public interface OnConnectListener {
        void onConnected(String mac);
    }

    @FunctionalInterface
    public interface OnDisconnectListener {
        void onDisconnected(String mac);
    }


    public interface MessageListener{
        void onMessageReceived(String mac, String msg);
    }

    private MessageListener messageListener;
    private OnConnectListener onConnectListener;
    private OnDisconnectListener onDisconnectListener;

    public void setOnConnectListener(OnConnectListener listener){
        this.onConnectListener = listener;
    }

    public void setOnDisconnectListener(OnDisconnectListener listener){
        this.onDisconnectListener = listener;
    }

    public void setMessageListener(MessageListener listener){
        this.messageListener = listener;
    }

    public GattClientManager(Context ctx){
        this.context = ctx;
    }

    public void connect(BluetoothDevice device){
        Log.i(TAG, "Connecting via gatt client to " + device.getAddress());
        try{
            bluetoothGatt = device.connectGatt(context, false, gattClientCallback);
        }
        catch(SecurityException e){
            Log.e(TAG, "failed to connect client cuz perms " + e);
            return;
        }

    }

    public void disconnect(){
        if(bluetoothGatt != null){
            try{
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            }
            catch(SecurityException e){
                Log.e(TAG, "failed to disconnect client cuz perms " + e);
                return;
            }

            bluetoothGatt = null;
            Log.i(TAG, "GATT client disconnected");
        }
    }

    public void sendMessage(String msg){
        if(bluetoothGatt == null || messageCharacteristic == null){
            Log.e(TAG, "Failed send message, GATT not ready");
            return;
        }
        messageCharacteristic.setValue(msg.getBytes(StandardCharsets.UTF_8));
        try{
            boolean success = bluetoothGatt.writeCharacteristic(messageCharacteristic);
            Log.i(TAG, "Sent frm client:" + msg + " (success=" + success + ")");
        }
        catch(SecurityException e){
            Log.e(TAG, "failed to send message from client cuz perms " + e);
            return;
        }

    }

    private void enableNotifications(){
        try{
            bluetoothGatt.setCharacteristicNotification(messageCharacteristic, true);
            BluetoothGattDescriptor descriptor = messageCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")); // std CCC descriptor

            if(descriptor != null){
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
            else{
                Log.e(TAG, "Descriptor error");
            }
        }
        catch(SecurityException e){
            Log.e(TAG, "failed to set characteristic notification cuz perms " + e);
            return;
        }

    }

    private final BluetoothGattCallback gattClientCallback = new BluetoothGattCallback(){

        @Override
        public void onConnectionStateChange(@NonNull BluetoothGatt gatt, int status, int newState){
            String mac = gatt.getDevice().getAddress();
            try{
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    Log.i(TAG, "Connected to GATT server: " + mac);
                    if(onConnectListener != null) onConnectListener.onConnected(mac);
                    gatt.discoverServices();
                }
                else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    Log.i(TAG, "Disconnected from GATT server: " + mac);
                    if(onDisconnectListener != null) onDisconnectListener.onDisconnected(mac);
                    disconnect();
                }
            }
            catch(SecurityException e){
                Log.e(TAG, "failed to connect to server cuz perms " + e);
                return;
            }

        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothGatt gatt, int status){
            BluetoothGattService service = gatt.getService(CHAT_SERVICE_UUID);
            if(service == null){
                Log.e(TAG, "Chat service not found on device");
                return;
            }

            messageCharacteristic = service.getCharacteristic(CHAT_MESSAGE_UUID);

            if(messageCharacteristic == null){
                Log.e(TAG, "Message characteristic not found");
                return;
            }
            Log.i(TAG, "GATT client ready: message characteristic available");
            enableNotifications();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            if (CHAT_MESSAGE_UUID.equals(characteristic.getUuid())){
                String msg = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                String mac = gatt.getDevice().getAddress();
                Log.i(TAG, "msg from server: " + msg);
                if(messageListener != null) messageListener.onMessageReceived(mac, msg);
            }
        }
    };
}

