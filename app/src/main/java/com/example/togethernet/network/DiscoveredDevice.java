package com.example.togethernet.network;

import android.bluetooth.BluetoothDevice;

public class DiscoveredDevice{
    private final String mac;
    private final String nodeID;
    private final BluetoothDevice gattConnectionObj;

    DiscoveredDevice(String mac_, String nodeID_, BluetoothDevice gattConnectionObj_){
        mac = mac_;
        nodeID = nodeID_;
        gattConnectionObj = gattConnectionObj_;
    }
    public String getMac() {
        return mac;
    }

    public String getNodeID() {
        return nodeID;
    }

    public BluetoothDevice getGattConnectionObj() {
        return gattConnectionObj;
    }
}
