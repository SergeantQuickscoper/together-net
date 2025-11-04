package com.example.togethernet.network;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GattServerManager{

    private static final String TAG = "GattServerManager";
    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic chatCharacteristic;
    private final Context context;
    private final BluetoothManager btManager;

    private static final UUID CHAT_SERVICE_UUID = UUID.fromString("0000ABC0-0000-1000-8000-00805F9B34FB");
    private static final UUID CHAT_MESSAGE_UUID = UUID.fromString("0000ABC1-0000-1000-8000-00805F9B34FB");



    public interface MessageListener{
        void onMessageReceived(String fromMac, String message);
    }

    private MessageListener messageListener;

    public void setMessageListener(MessageListener listener){
        this.messageListener = listener;
    }

    public GattServerManager(Context ctx){
        this.context = ctx;
        this.btManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void startServer(){
        try{
            gattServer = btManager.openGattServer(context, gattServerCallback);
        }
        catch(SecurityException e){
            // lol
            Log.e(TAG, "Failed to start server cuz perms " + e);
            return;
        }

        BluetoothGattService service = new BluetoothGattService(CHAT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        chatCharacteristic = new BluetoothGattCharacteristic(CHAT_MESSAGE_UUID,BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        );

        service.addCharacteristic(chatCharacteristic);
        try{
            gattServer.addService(service);
        }
        catch(SecurityException e){
            // lol
            Log.e(TAG, "Failed to add service cuz perms " + e);
            return;
        }


        Log.i(TAG, "started the GATT Server");
    }

    public void stopServer(){
        if (gattServer != null){

            try{
                gattServer.close();
            }
            catch(SecurityException e){
                // lol
                Log.e(TAG, "Failed to close server cuz perms " + e);
                return;
            }
            Log.i(TAG, "GATT Server stopped");
        }
    }

    public void notifyDevice(BluetoothDevice device, String msg){
        if(gattServer == null) return;

        chatCharacteristic.setValue(msg.getBytes(StandardCharsets.UTF_8));
        try{
            gattServer.notifyCharacteristicChanged(device, chatCharacteristic, false);
        }
        catch(SecurityException e){
            // lol
            Log.e(TAG, "Failed to notify device cuz perms " + e);
            return;
        }

    }

    private final BluetoothGattServerCallback gattServerCallback =
            new BluetoothGattServerCallback(){
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState){
                    if(newState == BluetoothProfile.STATE_CONNECTED) Log.i(TAG, "Device connected: " + device.getAddress());
                }

                @Override
                public void onCharacteristicWriteRequest(BluetoothDevice device,int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                                                         byte[] value){
                    if(CHAT_MESSAGE_UUID.equals(characteristic.getUuid())){
                        String msg = new String(value, StandardCharsets.UTF_8);
                        Log.i(TAG, "Received: " + msg + " from " + device.getAddress());
                        if(messageListener != null) messageListener.onMessageReceived(device.getAddress(), msg);
                        if(responseNeeded){
                            try{
                                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);;
                            }
                            catch(SecurityException e){
                                // lol
                                Log.e(TAG, "Failed to send server response cuz perms " + e);
                                return;
                            }

                        }
                    }
                }
            };
}

