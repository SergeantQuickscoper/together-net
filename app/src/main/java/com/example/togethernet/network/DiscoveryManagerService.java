package com.example.togethernet.network;

import com.example.togethernet.database.dao.NodeDAO;
import com.example.togethernet.database.model.Nodes;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.w3c.dom.Node;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.HashSet;
// MAJOR TODO: actually add a check to see if bluetooth is on on the device (probably in the try block)
public class DiscoveryManagerService extends Service {
    // channel settings
    private static final String TAG = "BLEService";
    private static final String TOGETHER_NET_UUID = "b5f5c630-60e1-4fd7-9c92-c8db13a2f090"; // hard coded uuid for the app service
    private static final String CHANNEL_ID = "TogetherNetBLE";
    private static final int SCAN_DURATION_MS = 5000;  // scan for 4 seconds
    private static final int SCAN_PAUSE_MS = 2000; // wait for 6 seconds
    //TODO: add params for advertisor too







    private final android.os.Handler scanHandler = new android.os.Handler();
    private boolean isScanning = false;

    public static final HashSet<String> discoveredDevices = new HashSet<>(); // rn use mac address but move to nodeID once thats part of payload
    private BluetoothLeAdvertiser advertiser = null;
    private BluetoothLeScanner scanner = null;

    // network stats (GOING STATIC VARS for now, in the future look into Binding/Broadcasting)
    public static int totalDiscoveredNodes = 0;

    private final Runnable scanPulse = new Runnable(){
        @Override
        public void run(){
            if(isScanning){
                stopScanPulse();
            }
            else{
                startScanPulse();
            }
        }
    };

    @Override
    public void onCreate(){
        makeForeground();
        super.onCreate();
        BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        if(adapter != null && adapter.isEnabled()){
            advertiser = adapter.getBluetoothLeAdvertiser();
            scanner = adapter.getBluetoothLeScanner();
            startAdvertising();
            scanHandler.post(scanPulse);
        } 
        else{
            Log.e(TAG, "Bluetooth not available or not enabled");
            stopSelf();
        }
    }

    private void makeForeground(){
        // notifs are just so user knows the foreground service is INDEED running
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, "TogetherNet BLE Service", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(chan);
        }
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("TogetherNet BLE").setContentText("Advertising and scanning for nodes")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build();
        startForeground(1, notif);
    }

    private void startAdvertising(){
        if(advertiser == null) return;
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(false)
                .build();

        NodeDAO dao = new NodeDAO(this);
        Nodes thisNode = dao.getNode();
        String nodeID = thisNode.getNodeId();
        byte[] nodeIDbytes = nodeID.getBytes(StandardCharsets.UTF_8);
        UUID serviceUuid = UUID.fromString(TOGETHER_NET_UUID);
        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(serviceUuid))
                .addServiceData(new ParcelUuid(serviceUuid), nodeIDbytes)
                .setIncludeDeviceName(false)
                .build();

        try{
            advertiser.startAdvertising(settings, data, advertiseCallback);
        }
        catch(SecurityException e){
            Log.d(TAG, "No perms to advertise");
            return;
        }

        Log.i(TAG, "Advertising has begun");
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback(){
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect){
            Log.i(TAG, "Advertising started successfully");
        }
        @Override
        public void onStartFailure(int errorCode){
            Log.e(TAG, "Advertising failed: " + errorCode);
        }
    };

    private void startScanning() {
        if(scanner == null) return;
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(TOGETHER_NET_UUID)))
                .build();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        try{
            scanner.startScan(Collections.singletonList(filter), settings, scanCallback);
        }
        catch(SecurityException e){
            Log.d(TAG, "No perms to scan");
            return;
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String mac = result.getDevice().getAddress();
            byte[] serviceData;
            try{
                serviceData = result.getScanRecord().getServiceData(new ParcelUuid(UUID.fromString(TOGETHER_NET_UUID)));
            }
            catch(NullPointerException e){
                Log.e(TAG, "Null service data");
                return;
            }

            String nodeId = new String(serviceData, StandardCharsets.UTF_8);

            if (!discoveredDevices.contains(mac)){
                discoveredDevices.add(mac);
                totalDiscoveredNodes = discoveredDevices.size();
                Log.i(TAG, "New device discovered: " + nodeId);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            if(advertiser != null) advertiser.stopAdvertising(advertiseCallback);
            if(scanner != null) scanner.stopScan(scanCallback);
        }
        catch(SecurityException e){
            Log.d(TAG, "No perms to scan or advertise");
            return;
        }


        Log.i(TAG, "BLEService stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        // not used ignore
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startScanPulse(){
        Log.i(TAG, "Pulse: STARTING");
        discoveredDevices.clear();
        totalDiscoveredNodes = 0;
        startScanning();
        isScanning = true;
        scanHandler.postDelayed(scanPulse, SCAN_DURATION_MS);
    }

    private void stopScanPulse(){
        Log.i(TAG, "Pulse: STOPPING");
        try{
            scanner.stopScan(scanCallback);
        }
        catch(Exception e){
            // lol
        }
        isScanning = false;
        scanHandler.postDelayed(scanPulse, SCAN_PAUSE_MS);
    }
    // Sad gone :(
//    public int getTotalDiscoveredNodes(){
//        return totalDiscoveredNodes;
//    }
}
